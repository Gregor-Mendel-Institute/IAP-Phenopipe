/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import javax.swing.JLabel;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.gwt.SnapshotDataIAP;
import de.ipk.ag_ba.server.pdf_report.PdfCreator;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ActionNumericDataReportComplete extends AbstractNavigationAction implements SpecialCommandLineSupport {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	private static final String separator = ";";// "\t";// ";";// "\t";
	private final boolean exportIndividualAngles;
	private final String[] variant;
	private final boolean xlsx;
	
	private File targetDirectoryOrTargetFile = null;
	
	public ActionNumericDataReportComplete(String tooltip, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		super(tooltip);
		this.exportIndividualAngles = exportIndividualAngles;
		this.variant = variant;
		this.xlsx = xlsx;
	}
	
	public ActionNumericDataReportComplete(MongoDB m, ExperimentReference experimentReference, boolean exportIndividualAngles, String[] variant, boolean xlsx) {
		this("Create report" +
				(exportIndividualAngles ? (xlsx ? " XLSX" : " CSV")
						: " PDF (" + StringManipulationTools.getStringList(variant, ", ") + ")"),
				exportIndividualAngles,
				variant, xlsx);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		if (exportIndividualAngles)
			return "Save " + (xlsx ? "XLSX" : "CSV") + " Data Table";
		if (SystemAnalysis.isHeadless()) {
			return "Create Report" + (xlsx ? " (XLSX)" : "")
					+ (exportIndividualAngles ? " (side angles)" : " (avg) (" + StringManipulationTools.getStringList(variant, ", ") + ")");
		} else {
			String filter = StringManipulationTools.getStringList(variant, ", ");
			if (filter.endsWith(", TRUE"))
				filter = filter.substring(0, filter.length() - ", TRUE".length());
			if (filter.endsWith(", FALSE"))
				filter = filter.substring(0, filter.length() - ", FALSE".length());
			if (filter.endsWith(", none"))
				filter = filter.substring(0, filter.length() - ", none".length());
			filter = StringManipulationTools.stringReplace(filter, ", ", " and ");
			if (variant[2].equals("TRUE"))
				return "<html><center>Create full PDF report<br>"
						+ (exportIndividualAngles ? " (side angles)" : " (" + filter + ")");
			else
				return "<html><center>Create short PDF report<br>(" + filter + ")";
		}
	}
	
	@Override
	public String getDefaultImage() {
		if (exportIndividualAngles)
			return IAPimages.getDownloadIcon();
		else
			return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ExperimentInterface experiment = experimentReference.getData(m);
		if (SystemAnalysis.isHeadless() && !(targetDirectoryOrTargetFile != null)) {
			
		} else {
			ArrayList<SnapshotDataIAP> snapshots;
			StringBuilder csv = new StringBuilder();
			boolean water = false;
			String csvHeader = getCSVheader();
			if (status != null)
				status.setCurrentStatusText2("Create snapshots");
			System.out.println(SystemAnalysis.getCurrentTime() + ">Create snapshot data set");
			if (!water) {
				HashMap<String, Integer> indexInfo = new HashMap<String, Integer>();
				snapshots = IAPservice.getSnapshotsFromExperiment(
						null, experiment, indexInfo, false,
						exportIndividualAngles);
				TreeMap<Integer, String> cola = new TreeMap<Integer, String>();
				for (String val : indexInfo.keySet())
					cola.put(indexInfo.get(val), val);
				StringBuilder indexHeader = new StringBuilder();
				for (String val : cola.values())
					indexHeader.append(separator + val);
				csvHeader = StringManipulationTools.stringReplace(csvHeader, "\r\n", "");
				csvHeader = StringManipulationTools.stringReplace(csvHeader, "\n", "");
				csv.append(csvHeader + indexHeader.toString() + "\r\n");
			} else {
				snapshots = IAPservice.getSnapshotsFromExperiment(
						null, experiment, null, false, exportIndividualAngles);
				csv.append(csvHeader);
			}
			System.out.println(SystemAnalysis.getCurrentTime() + ">Snapshot data set has been created");
			Workbook wb = xlsx ? new XSSFWorkbook() : null;
			Sheet sheet = xlsx ? wb.createSheet(replaceInvalidChars(experimentReference.getExperimentName())) : null;
			if (sheet != null) {
				Row row = sheet.createRow(0);
				int col = 0;
				String c = csv.toString().trim();
				c = StringManipulationTools.stringReplace(c, "\r\n", "");
				c = StringManipulationTools.stringReplace(c, "\n", "");
				for (String h : c.split(separator))
					row.createCell(col++).setCellValue(h);
			}
			
			PdfCreator p = new PdfCreator(targetDirectoryOrTargetFile);
			if (xlsx) {
				experiment = null;
				if (status != null)
					status.setCurrentStatusText2("Fill workbook");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Fill workbook");
				Queue<SnapshotDataIAP> todo = new LinkedList<SnapshotDataIAP>(snapshots);
				snapshots = null;
				int rowNum = 1;
				Runtime r = Runtime.getRuntime();
				while (!todo.isEmpty()) {
					SnapshotDataIAP s = todo.poll();
					if (status != null)
						status.setCurrentStatusText1("Rows remaining: " + todo.size());
					if (status != null)
						status.setCurrentStatusText2("Memory status: "
								+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
								+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB");
					System.out.println(SystemAnalysis.getCurrentTime() + ">Filling workbook, todo: " + todo.size() + " "
							+ r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024 / 1024
							+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB");
					for (ArrayList<DateDoubleString> valueRow : s.getCSVobjects()) {
						Row row = sheet.createRow(rowNum++);
						int colNum = 0;
						for (DateDoubleString o : valueRow) {
							if (o.getString() != null && !o.getString().isEmpty())
								row.createCell(colNum++).setCellValue(o.getString());
							else
								if (o.getDouble() != null)
									row.createCell(colNum++).setCellValue(o.getDouble());
								else
									if (o.getDate() != null)
										row.createCell(colNum++).setCellValue(o.getDate());
									else
										colNum++;
						}
					}
				}
				System.out.println(SystemAnalysis.getCurrentTime() + ">Workbook is filled");
			} else
				if (exportIndividualAngles) {
					for (SnapshotDataIAP s : snapshots) {
						boolean germanLanguage = false;
						csv.append(s.getCSVvalue(germanLanguage, separator));
					}
				} else {
					for (SnapshotDataIAP s : snapshots) {
						boolean germanLanguage = false;
						csv.append(s.getCSVvalue(germanLanguage, separator));
					}
				}
			if (xlsx) {
				if (status != null)
					status.setCurrentStatusText2("Save to file");
				System.out.println(SystemAnalysis.getCurrentTime() + ">Save to file");
				p.prepareTempDirectory();
				if (targetDirectoryOrTargetFile == null)
					wb.write(new FileOutputStream(p.getSaveFile(xlsx), xlsx));
				else
					wb.write(new FileOutputStream(targetDirectoryOrTargetFile, xlsx));
				System.out.println(SystemAnalysis.getCurrentTime() + ">File is saved");
				if (status != null)
					status.setCurrentStatusText2("File saved");
			}
			else {
				byte[] result = csv.toString().getBytes();
				
				p.prepareTempDirectory();
				p.saveReportCSV(result, xlsx);
			}
			
			// p.saveScripts(new String[] {
			// "diagramForReportPDF.r",
			// "diagramIAP.cmd",
			// "diagramIAP.bat",
			// "initLinux.r",
			// "report2.tex", "createDiagramFromValuesLinux.r"
			// });
			if (!xlsx)
				p.saveScripts(new String[] {
						"createDiagramOneFile.r",
						"diagramIAP.cmd",
						"diagramIAP.bat",
						"initLinux.r",
						"report2.tex"
				});
			
			if (!exportIndividualAngles && !xlsx) {
				p.executeRstat(variant, experiment);
				p.getOutput();
				boolean ok = p.hasPDFcontent();
				if (ok)
					AttributeHelper.showInBrowser(p.getPDFurl());
				else
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: No output file available");
				
				// p.deleteDirectory();
			} else {
				p.openTargetDirectory();
			}
		}
	}
	
	private String replaceInvalidChars(String experimentName) {
		String res = StringManipulationTools.stringReplace(experimentName, ":", "_");
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (SystemAnalysis.isHeadless())
			return new MainPanelComponent(new JLabel());
		else
			return new MainPanelComponent("The generated PDF report will be opened automatically in a moment.");
	}
	
	public ExperimentReference getExperimentReference() {
		return experimentReference;
	}
	
	public MongoDB getMongoInstance() {
		return m;
	}
	
	public String getCSVheader() {
		return "Angle" + separator + "Plant ID" + separator + "Condition" + separator + "Species" + separator + "Genotype" + separator + "Variety" + separator
				+ "GrowthCondition"
				+ separator + "Treatment" + separator + "Sequence" + separator + "Day" + separator + "Time" + separator + "Day (Int)"
				+ separator + "Weight A (g)" + separator + "Weight B (g)" + separator +
				"Water (weight-diff)" +
				separator + "Water (pumped)" + separator + "RGB" + separator + "FLUO" + separator + "NIR" + separator + "OTHER" +
				"\r\n";
	}
	
	long startTime;
	File ff;
	
	@Override
	public boolean prepareCommandLineExecution() throws Exception {
		targetDirectoryOrTargetFile = null;
		if (xlsx)
			return prepareCommandLineExecutionFile();
		else
			return prepareCommandLineExecutionDirectory();
	}
	
	@Override
	public void postProcessCommandLineExecution() {
		if (xlsx)
			postProcessCommandLineExecutionFile();
		else
			postProcessCommandLineExecutionDirectory();
	}
	
	public boolean prepareCommandLineExecutionFile() throws Exception {
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an output file name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If no path is specified, the file will be placed in the current directory.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER FILENAME (ENTER NOTHING TO CANCEL OPERATION):");
		String fileName = SystemAnalysis.getCommandLineInput();
		if (fileName == null || fileName.trim().isEmpty())
			return false;
		else {
			File f = new File(fileName);
			if (f.exists()) {
				System.out.println(SystemAnalysis.getCurrentTime() + "WARNING: File exists (" + f.getAbsolutePath() + ")");
				System.out.println(SystemAnalysis.getCurrentTime() + "READY: Enter \"yes\" to overwrite, otherwise operation will be cancelled");
				String confirm = SystemAnalysis.getCommandLineInput();
				if (confirm != null && confirm.toUpperCase().indexOf("Y") >= 0)
					; // OK
				else
					return false;
			}
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
			// if (!f.canWrite()) {
			// System.out.println(SystemAnalysis.getCurrentTime() + "ERROR: Can't write to file (" + f.getAbsolutePath() + ")");
			// return false;
			// }
			targetDirectoryOrTargetFile = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	public void postProcessCommandLineExecutionFile() {
		long fs = ff.length();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
				"File size " + fs / 1024 / 1024 + " MB, " +
				"t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000));
	}
	
	public boolean prepareCommandLineExecutionDirectory() throws Exception {
		System.out.println();
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Current directory is " + (new File("").getAbsolutePath()));
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Command requires specification of an empty output directory name.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: If a part of the specified path is not existing, it will be created.");
		System.out.println(SystemAnalysis.getCurrentTime() + ">READY: PLEASE ENTER DIRECTORY STRUCTURE (ENTER NOTHING TO CANCEL OPERATION):");
		String outputDir = SystemAnalysis.getCommandLineInput();
		if (outputDir == null || outputDir.trim().isEmpty())
			return false;
		else {
			File f = new File(outputDir);
			if (!f.exists()) {
				if (!f.mkdirs()) {
					System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Could not create directory structure (" + f.getAbsolutePath() + ")");
					System.out.println();
					return false;
				}
			}
			if (!f.isDirectory()) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Output specifies a file instead of a directory (" + f.getAbsolutePath() + ")");
				System.out.println();
				return false;
			}
			String[] fl = f.list();
			if (fl.length > 0) {
				System.out.print(SystemAnalysis.getCurrentTime() + ">ERROR: Output directory contains " + fl.length + " files. It needs to be empty.");
				System.out.println();
				return false;
			}
			
			System.out.print(SystemAnalysis.getCurrentTime() + ">INFO: Output to " + f.getAbsolutePath());
			// if (!f.canWrite()) {
			// System.out.println(SystemAnalysis.getCurrentTime() + "ERROR: Can't write to file (" + f.getAbsolutePath() + ")");
			// return false;
			// }
			targetDirectoryOrTargetFile = f;
			startTime = System.currentTimeMillis();
			ff = f;
			return true;
		}
	}
	
	public void postProcessCommandLineExecutionDirectory() {
		// long fs = written.getLong();
		// double mbps = fs / 1024d / 1024d / ((System.currentTimeMillis() - startTime) / 1000d);
		// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: " +
		// "Overall size of files is " + fs / 1024 / 1024 + " MB, " +
		// "t=" + SystemAnalysis.getWaitTimeShort(System.currentTimeMillis() - startTime - 1000) + ", " +
		// "speed=" + StringManipulationTools.formatNumber(mbps, "#.#") + " MB/s");
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Data processing complete, target directory contains "
				+ targetDirectoryOrTargetFile.list().length + " files.");
	}
	
}
