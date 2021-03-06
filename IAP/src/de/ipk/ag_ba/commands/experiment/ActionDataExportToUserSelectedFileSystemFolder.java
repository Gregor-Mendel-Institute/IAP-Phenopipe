package de.ipk.ag_ba.commands.experiment;

import java.io.File;
import java.util.ArrayList;

import org.OpenFileDialogService;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataExportToUserSelectedFileSystemFolder extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReferenceInterface> experimentReferences;
	private final MongoDB m;
	private final boolean ignoreOutliers;
	private final ArrayList<MainPanelComponent> results = new ArrayList<MainPanelComponent>();
	
	public ActionDataExportToUserSelectedFileSystemFolder(String tooltip, MongoDB m,
			ArrayList<ExperimentReferenceInterface> experimentReference, boolean ignoreOutliers) {
		super(tooltip);
		this.m = m;
		this.experimentReferences = experimentReference;
		this.ignoreOutliers = ignoreOutliers;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		results.clear();
		if (experimentReferences == null)
			return;
		File currentDirectory = null;
		if (!SystemAnalysis.simulateHeadless)
			currentDirectory = OpenFileDialogService.getDirectoryFromUser("Select Target Folder");
		else {
			System.out.println("Enter path ...");
			String inp = SystemAnalysis.getCommandLineInput();
			currentDirectory = new File(inp);
			System.out.println("Copy to " + currentDirectory.getAbsolutePath());
		}	
		
		if (currentDirectory != null) {
			VirtualFileSystemVFS2 vfs = new VirtualFileSystemVFS2(
					"user.dir." + System.currentTimeMillis(),
					VfsFileProtocol.LOCAL,
					"User Selected Directory",
					"File I/O", "",
					null,
					null,
					currentDirectory.getCanonicalPath(),
					false,
					false,
					null);
			for (ExperimentReferenceInterface er : experimentReferences) {
				results.add(vfs.saveExperiment(m, er, getStatusProvider(), ignoreOutliers));
			}
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> rl = new ArrayList<String>();
		if (results != null)
			for (MainPanelComponent m : results)
				rl.addAll(m.getHTML());
		return new MainPanelComponent(rl);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "To Local File System";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return new ArrayList<NavigationButton>();
	}
	
}
