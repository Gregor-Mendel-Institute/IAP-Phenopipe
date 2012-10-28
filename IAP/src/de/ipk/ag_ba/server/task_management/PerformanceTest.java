package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.ReleaseInfo;
import org.junit.AfterClass;
import org.junit.Test;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.analysis.maize.MaizeAnalysisPipeline;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersOnFluo;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters_vis_fluo;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.server.task_management.PerformanceTestImages.ImageNames;

public class PerformanceTest {
	
	private HashMap<Integer, FlexibleMaskAndImageSet> res;
	private HashMap<Integer, FlexibleImageStack> debugStack;
	double scale = 1.0;
	static boolean sleep = false;
	
	@Test
	public void testPipeline() throws IOException, Exception {
		
		// calculate LAB cube here to not skew calculation of pipeline processing time
		if (ImageOperation.labCube == null)
			System.out.println("LAB cube could not be initialized (impossible internal error)");
		
		FlexibleImage imgVis = PerformanceTestImages.getImage(ImageNames.MAIZE_VIS_SIDE_BELONG_TO_REFERENCE_1386);
		FlexibleImage imgFluo = PerformanceTestImages.getImage(ImageNames.MAIZE_FLU_SIDE_BELONG_TO_REFERENCE_1386);
		FlexibleImage imgNir = PerformanceTestImages.getImage(ImageNames.MAIZE_NIR_SIDE_BELONG_TO_REFERENCE_1386);
		FlexibleImage imgIr = null;
		
		FlexibleImage imgVisRef = PerformanceTestImages.getImage(ImageNames.MAIZE_VIS_SIDE_REFERENCE_1386);
		FlexibleImage imgFluoRef = PerformanceTestImages.getImage(ImageNames.MAIZE_FLU_SIDE_REFERENCE_1386);
		FlexibleImage imgNirRef = PerformanceTestImages.getImage(ImageNames.MAIZE_NIR_SIDE_REFERENCE_1386);
		FlexibleImage imgIrRef = null;
		StopWatch school;
		boolean oldschool = false;
		if (oldschool) {
			BlockRemoveSmallClustersOnFluo.ngUse = false;
			BlockRemoveSmallClusters_vis_fluo.ngUse = false;
			FlexibleImage fc = imgFluo.copy();
			FlexibleImage nc = imgFluoRef.copy();
			school = new StopWatch("oldschool");
			testSide("oldschool", imgVis, imgVisRef, fc, nc, imgNir, imgNirRef, imgIr, imgIrRef, "1");
			school.printTime();
		}
		BlockRemoveSmallClustersOnFluo.ngUse = true;
		BlockRemoveSmallClusters_vis_fluo.ngUse = true;
		school = new StopWatch("newschool");
		testSide("newschool", imgVis, imgVisRef, imgFluo, imgFluoRef, imgNir, imgNirRef, imgIr, imgIrRef, "1");
		school.printTime();
	}
	
	public void testSide(String debugInfo, FlexibleImage imgVis, FlexibleImage imgVisRef, FlexibleImage imgFluo, FlexibleImage imgFluoRef,
			FlexibleImage imgNir, FlexibleImage imgNirRef,
			FlexibleImage imgIr, FlexibleImage imgIrRef,
			String name) throws IOException, Exception, InstantiationException, IllegalAccessException, InterruptedException,
			FileNotFoundException {
		
		System.out.println("\n" + "TestMaizePipline - Side");
		
		final FlexibleImageSet input = new FlexibleImageSet(imgVis, imgFluo, imgNir, imgIr);
		
		final FlexibleImageSet ref_input = new FlexibleImageSet(imgVisRef, imgFluoRef, imgNirRef, imgIrRef);
		
		ImageProcessorOptions options = new ImageProcessorOptions(scale);
		
		options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
		options.setCameraPosition(CameraPosition.SIDE);
		options.setIsMaize(true);
		// options.clearAndAddDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 0.5);
		MaizeAnalysisPipeline pipeline = new MaizeAnalysisPipeline("Maize Phenotyping");
		
		debugStack = null;// new FlexibleImageStack();
		
		IAPservice s = new IAPservice();
		if (s.hashCode() == 0)
			System.out.println("///");
		
		res = pipeline.pipeline(options, input, ref_input, 2, debugStack);
		
		if (debugStack != null) {
			for (Integer key : res.keySet()) {
				res.get(key).save(ReleaseInfo.getDesktopFolder() + File.separator + "testTestPipelineMaizeSide" + name + "_" + debugInfo + "_tray" + key + ".tiff");
				pipeline.getSettings().get(key).printAnalysisResults();
				debugStack.get(key).print("Result " + debugInfo + " tray " + key, getReRunCode(input, ref_input), "re_Run");
				debugStack.get(key).saveAsLayeredTif(
						new File(ReleaseInfo.getDesktopFolder() + File.separator + "maizeSide_debugstack" + name + "_" + debugInfo + "_tray" + key + ".tiff"));
			}
		}
	}
	
	private Runnable getReRunCode(final FlexibleImageSet input, final FlexibleImageSet ref_input) {
		Runnable reRun = new Runnable() {
			@Override
			public void run() {
				ImageProcessorOptions options = new ImageProcessorOptions(scale);
				options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
				options.setCameraPosition(CameraPosition.SIDE);
				MaizeAnalysisPipeline pipeline = new MaizeAnalysisPipeline("Maize Phenotyping");
				debugStack = new HashMap<Integer, FlexibleImageStack>();
				try {
					res = pipeline.pipeline(options, input, ref_input, 2, debugStack);
					for (Integer key : res.keySet()) {
						pipeline.getSettings().get(key).printAnalysisResults();
						if (debugStack != null)
							debugStack.get(key).print("Res Tray " + key);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		return reRun;
		
	}
	
	public void testTop(FlexibleImage imgVis, FlexibleImage imgVisRef, FlexibleImage imgFluo, FlexibleImage imgFluoRef,
			FlexibleImage imgNir,
			FlexibleImage imgNirRef,
			FlexibleImage imgIr,
			FlexibleImage imgIrRef
			) throws IOException, Exception, InstantiationException, IllegalAccessException, InterruptedException, FileNotFoundException {
		
		System.out.println("\n" + "TestMaizePipline - Top");
		
		FlexibleImageSet input = new FlexibleImageSet(imgVis, imgFluo, imgNir, imgIr);
		
		FlexibleImageSet ref_input = new FlexibleImageSet(imgVisRef, imgFluoRef, imgNirRef, imgIrRef);
		
		ImageProcessorOptions options = new ImageProcessorOptions(scale);
		
		options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
		options.setCameraPosition(CameraPosition.TOP);
		// options.clearAndAddDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 0.5);
		MaizeAnalysisPipeline maize = new MaizeAnalysisPipeline("Maize Phenotyping");
		
		debugStack = new HashMap<Integer, FlexibleImageStack>();
		
		res = maize.pipeline(options, input, ref_input, 2, debugStack);
		
		for (Integer tray : res.keySet()) {
			res.get(tray).save(ReleaseInfo.getDesktopFolder() + File.separator + "testTestPipelineMaizeTop_tray" + tray + ".tiff");
			
			if (debugStack != null)
				debugStack.get(tray).saveAsLayeredTif(new File(ReleaseInfo.getDesktopFolder() + File.separator + "maizeTop_debugstack_tray" + tray + ".tiff"));
		}
	}
	
	@AfterClass
	public static void setUpAfterClass() throws Exception {
		if (sleep)
			Thread.sleep(1000 * 60 * 10);
	}
}
