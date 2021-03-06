package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;
import iap.blocks.data_structures.RunnableOnImageSet;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.binarymask.ImageJOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.DoubleAndImageData;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.operations.skeleton.Limb;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * Calculates the skeleton to detect the leaves and the tassel.
 * Requires the FLUO image for bloom detection.
 * 
 * @author pape, klukas
 */
public class BlSkeletonizeVisFluo extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	
	private boolean debug = false;
	private boolean debug2 = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		debug2 = getBoolean("debug bloom detection", false);
	}
	
	@Override
	protected synchronized Image processVISmask() {
		Image vis = input().masks().vis();
		if (!getBoolean("skeletonize VIS", true))
			return vis;
		Image fluo = input().masks().fluo() != null ? input().masks().fluo().copy() : null;
		Image res = vis;
		if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE && vis != null && getResultSet() != null) {
			Image viswork = vis.show("orig", debug).copy().io()
					.border(5)
					.dilateHorizontal(getInt("Dilate-Cnt-Vis-Hor", 25)) // 10
					.bm().erode(getInt("Erode-Cnt-Vis", 5))
					.dilate(getInt("Dilate-Cnt-Vis", 10)).io()
					.blurImageJ(getDouble("Blur-Vis", 0))
					.getImage().show("vis preprocessed", debug);
			
			if (viswork != null) {
				viswork.setCameraType(vis.getCameraType());
				if (vis != null) {
					calcSkeleton(viswork, vis, fluo,
							getBoolean("Leaf Width Calculation Type A (VIS)", false),
							getBoolean("Leaf Width Calculation Type B (VIS)", false),
							getBoolean("draw_skeleton", true),
							CameraType.VIS, input().images().getVisInfo());
				}
			}
		}
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP && vis != null && getResultSet() != null) {
			Image viswork = vis.copy().io().bm()// .medianFilter32Bit()
					.erode(getInt("Erode-Cnt-Vis", 12))
					.dilate(getInt("Dilate-Cnt-Vis", 6)).io()
					.blurImageJ(getDouble("Blur-Vis", 3.0))
					.getImage().show("vis", debug);
			
			if (viswork != null) {
				viswork.setCameraType(vis.getCameraType());
				if (vis != null) {
					calcSkeleton(viswork, vis, fluo,
							getBoolean("Leaf Width Calculation Type A (VIS)", false),
							getBoolean("Leaf Width Calculation Type B (VIS)", false),
							getBoolean("draw_skeleton", true),
							CameraType.VIS, input().images().getVisInfo());
				}
			}
		}
		return res;
	}
	
	@Override
	protected synchronized Image processFLUOmask() {
		Image vis = input().masks().vis();
		if (!getBoolean("skeletonize FLUO", true))
			return input().masks().fluo();
		Image fluo = input().masks().fluo() != null ? input().masks().fluo().copy() : null;
		if (fluo == null)
			return fluo;
		if (optionsAndResults.getCameraPosition() == CameraPosition.SIDE && getResultSet() != null) {
			Image fluowork = fluo.copy().io()
					.dilateHorizontal(getInt("Dilate-Cnt-Fluo-Hor", 15)) // 10
					.bm()// .medianFilter32Bit()
					.erode(getInt("Erode-Cnt-Fluo", 5))
					.dilate(getInt("Dilate-Cnt-Fluo", 10)).io()
					.blurImageJ(getDouble("Blur-Fluo", 0.0))
					.getImage().show("fluo", debug);
			
			if (fluowork != null) {
				fluowork.setCameraType(fluo.getCameraType());
				calcSkeleton(fluowork, vis, fluo,
						getBoolean("Leaf Width Calculation Type A (FLUO)", false),
						getBoolean("Leaf Width Calculation Type B (FLUO)", false),
						getBoolean("draw_skeleton", true),
						CameraType.FLUO, input().images().getFluoInfo());
			}
		}
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP && getResultSet() != null) {
			Image fluowork = fluo.copy().io().bm()// .filterRGB(150, 255, 255)
					.erode(getInt("Erode-Cnt-Fluo", 0))
					.dilate(getInt("Dilate-Cnt-Fluo", 0)).io()
					.blurImageJ(getDouble("Blur-Fluo", 0.0))
					.getImage().show("fluo", debug);
			
			if (fluowork != null) {
				fluowork.setCameraType(fluo.getCameraType());
				calcSkeleton(fluowork, vis, fluo,
						getBoolean("Leaf Width Calculation Type A (FLUO)", false),
						getBoolean("Leaf Width Calculation Type B (FLUO)", false),
						getBoolean("draw_skeleton", true),
						CameraType.FLUO, input().images().getFluoInfo());
			}
		}
		return input().masks().fluo();
	}
	
	public synchronized Image calcSkeleton(Image inp, Image vis, Image fluo,
			boolean specialLeafWidthCalculations, boolean specialSkeletonBasedLeafWidthCalculation,
			boolean addPostProcessor, CameraType cameraType, ImageData imageRef) {
		Image inpFLUOunchanged = fluo != null ? fluo.copy() : null;
		// ***skeleton calculations***
		SkeletonProcessor2d skel2d = inp.io().skeletonize().replaceColor(Color.BLACK.getRGB(), Color.MAGENTA.getRGB()).
				show("out sk", false).skel2d();
		
		skel2d.markEndpointsAndBranches();
		skel2d.createEndpointsAndBranchesLists();
		skel2d.show("endpoints and branches (unfiltered)", debug);
		
		if (getBoolean("Join Skeleton Parts", true)) {
			skel2d.calculateEndlimbsRecursive();
			skel2d.connectSkeleton();
			skel2d.markEndpointsAndBranches();
			skel2d.createEndpointsAndBranchesLists();
		}
		
		skel2d.calculateEndlimbsRecursive();
		ArrayList<Limb> endlimbs_saved = (ArrayList<Limb>)skel2d.endlimbs.clone();
		int leafcount = skel2d.endpoints.size();
		int branchcount = skel2d.branches.size();
		if (debug)
			System.out.println("A Leaf count: " + leafcount);
		
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		int bloomLimbCount = 0;
		if (getBoolean("Detect Bloom", false) && fluo != null && vis != null) {
			double xf = fluo.getWidth() / (double) vis.getWidth();
			double yf = fluo.getHeight() / (double) vis.getHeight();
			Image proc = fluo;
			Image probablyBloomFluo = null;
			if (!getBoolean("consider fluo image for bloom detection", true)) {
				proc = vis.copy();
				probablyBloomFluo = proc.io().filterRemainHSV(
						getDouble("bloom hue min", 0), getDouble("bloom hue max", 1),
						getDouble("bloom sat min", 0), getDouble("bloom sat max", 1),
						getDouble("bloom val min", 0), getDouble("bloom val max", 1)
						).show("bloom filtered by HSV", getBoolean("debug bloom detection HSV filter", false))
						.bm()
						.erode(getInt("bloom-erode-cnt", 0))
						// .invert()
						// .removeSmallClusters(true, null).invert().invert()
						// .erode(getInt("bloom-dilate-cnt", 4))
						.getImage().show("bloom filtered by HSV and then cleaned", getBoolean("debug bloom detection HSV filter", false));
			} else {
				probablyBloomFluo = skel2d.calcProbablyBloomImage(
						proc.io().blurImageJ(getInt("bloom-blur", 10)).getImage().show("blur for bloom detection", debug),
						getDouble("bloom hue", 0.075f)).io().// blur(3).
						thresholdGrayClearLowerThan(getInt("bloom-max-brightness", 255), Color.BLACK.getRGB()).getImage();
				
				probablyBloomFluo = probablyBloomFluo.io().show("probably bloom area unfiltered", false).invertImageJ()
						.removeSmallClusters(true, null).bm().
						erode(getInt("bloom-erode-cnt", 4)).io().invertImageJ().
						getImage();
			}
			if (debug2) {
				ImageStack fis = new ImageStack();
				fis.addImage("probably bloom area", probablyBloomFluo);
				fis.addImage("input image", proc);
				fis.show("bloom area");
			}
			
			HashSet<Point> knownBloompoints = skel2d.detectBloom(vis, probablyBloomFluo, xf, yf);
			bloomLimbCount = knownBloompoints.size();
			skel2d.deleteShortEndLimbs(10, false, knownBloompoints);
			skel2d.detectBloom(vis, probablyBloomFluo, xf, yf);
			
			rt.addValue("fluo.bloom.area.size", probablyBloomFluo.io().show("BLOOM AREA", debug2).countFilledPixels());
		}
		skel2d.calculateEndlimbsRecursive();
		skel2d.deleteShortEndLimbs(getInt("delete limbs threshold", 20), false, new HashSet<Point>());
		
		if (debug) {
			leafcount = skel2d.endpoints.size();
			skel2d.show("endpoints and branches (filtered)", debug);
			System.out.println("B Leaf count: " + leafcount);
		}
		
		skel2d.calculateEndlimbsRecursive();
		leafcount = skel2d.endpoints.size();
		if (debug)
			System.out.println("C Leaf count: " + leafcount);
		Double leafWidthInPixels = null;
		if (specialLeafWidthCalculations) {
			ArrayList<Point> branchPoints = skel2d.getBranches();
			if (branchPoints.size() > 0) {
				int wid = inp.getWidth();
				int hei = inp.getHeight();
				int[][] tempImage = new int[wid][hei];
				int clear = ImageOperation.BACKGROUND_COLORint;
				for (int x = 0; x < wid; x++)
					for (int y = 0; y < hei; y++)
						tempImage[x][y] = clear;
				Image clearImage = new Image(tempImage).copy();
				int black = Color.BLACK.getRGB();
				for (Point p : branchPoints)
					if (p.x >= 0 && p.y >= 0 && p.x < wid && p.y < hei)
						tempImage[p.x][p.y] = Color.YELLOW.getRGB();
				Image temp = new Image(tempImage);
				temp = temp.io().hull().setCustomBackgroundImageForDrawing(clearImage).
						find(null, false, false, true, false, false, false, false, black, black, black, black, black, null, 0d).getImage();
				temp = temp.io().border().floodFillFromOutside(clear, Color.YELLOW.getRGB()).getImage().show("INNER HULL", debug);
				getResultSet().setImage(getBlockPosition(), "innerhull_" + inp.getCameraType(), new LoadedImage(imageRef, temp.getAsBufferedImage(true)), true);
				tempImage = temp.getAs2A();
				int[][] ttt = inpFLUOunchanged.getAs2A();
				int wf = inpFLUOunchanged.getWidth();
				int hf = inpFLUOunchanged.getHeight();
				for (int x = 0; x < ttt.length && x < tempImage.length; x++)
					for (int y = 0; y < ttt[0].length && y < tempImage[0].length; y++) { // [x]
						if (tempImage[x][y] != Color.YELLOW.getRGB())
							ttt[x][y] = clear;
					}
				
				for (Point p : branchPoints)
					if (p.x < wf && p.y < hf && p.x > 0 && p.y > 0)
						ttt[p.x][p.y] = clear;
				temp = new Image(ttt).io().show("FINAL", debug).getImage();
				
				leafWidthInPixels = 0d;
				int filled;
				ImageJOperation tio = temp.io().bm();
				temp = null;
				do {
					filled = tio.countFilledPixels();
					if (filled > 0)
						leafWidthInPixels++;
					tio.erode();
				} while (filled > 0);
			}
		}
		
		leafcount = skel2d.endpoints.size();
		branchcount = skel2d.branches.size();
		// System.out.println("C Leaf count: " + leafcount);
		Image skelres = skel2d.getAsImage();
		int leaflength = skelres.io().countFilledPixels(SkeletonProcessor2d.getDefaultBackground());
		leafcount -= bloomLimbCount;
		
		// claculate limb curvature
		if (getBoolean("Calculate leaf curvature", true)) {
//			skel2d.calculateEndlimbsRecursive();
			double[] endlimbCurvatureSUMAVG = skel2d.calculateEndLimbCurvature(endlimbs_saved);
		
			rt.addValue("leaf.curvature.sum", endlimbCurvatureSUMAVG[0]);
			rt.addValue("leaf.curvature.avg", endlimbCurvatureSUMAVG[1]);
		}
		
		// ***Out***
		// System.out.println("leafcount: " + leafcount + " leaflength: " + leaflength + " numofendpoints: " + skel2d.endpoints.size());
		Image result = MapOriginalOnSkelUseingMedian(skelres, inp, Color.BLACK.getRGB());
		result.show("res", false);
		Image result2 = skel2d.copyONOriginalImage(inp);
		result2.show("res2", false);
		
		// ***Saved***
		Double distHorizontal = optionsAndResults.getCalculatedBlueMarkerDistance();
		double normFactor = distHorizontal != null && optionsAndResults.getREAL_MARKER_DISTANCE() != null ? optionsAndResults.getREAL_MARKER_DISTANCE()
				/ distHorizontal : 1;
		
		if (specialSkeletonBasedLeafWidthCalculation) {
			Image inputImage = inpFLUOunchanged.copy().show("inp img 2", false);
			int clear = ImageOperation.BACKGROUND_COLORint;
			int[][] inp2d = inputImage.getAs2A();
			int wf = inputImage.getWidth();
			int hf = inputImage.getHeight();
			for (Point p : skel2d.branches)
				if (p.x < wf && p.y < hf && p.x > 0 && p.y > 0)
					inp2d[p.x][p.y] = clear;
			
			inputImage = new Image(inp2d);
			
			ImageCanvas canvas = inputImage.io().canvas();
			ArrayList<Point> branchPoints = skel2d.getBranches();
			int lw;
			if (leafWidthInPixels != null)
				lw = (int) Math.ceil(leafWidthInPixels) * 3;
			else
				lw = 1;
			for (Point p : branchPoints)
				canvas.fillRect(p.x - lw / 2, p.y - lw / 2, lw, lw, clear);
			inputImage = canvas.getImage().show("CLEARED (" + branchPoints.size() + ") lw=" + leafWidthInPixels, debug);
			
			// repeat erode operation until no filled pixel
			Double leafWidthInPixels2 = 0d;
			int filled;
			ImageStack fis = debug ? new ImageStack() : null;
			ImageJOperation ioo = inputImage.io().bm();
			do {
				filled = ioo.countFilledPixels();
				if (filled > 0)
					leafWidthInPixels2++;
				if (fis != null)
					fis.addImage("Leaf width 1: " + leafWidthInPixels + ", Leaf width 2: " + leafWidthInPixels2, inputImage.copy());
				ioo = ioo.erode();
			} while (filled > 0);
			if (fis != null) {
				fis.addImage("LW=" + leafWidthInPixels, inputImage);
				fis.show("SKEL2");
			}
			// number of repeats is 1/4 of maximum leaf width, but the actual number of repeats (not 4x) is stored
			if (leafWidthInPixels2 != null && leafWidthInPixels2 > 0 && !Double.isNaN(leafWidthInPixels2) && !Double.isInfinite(leafWidthInPixels2)) {
				if (distHorizontal != null)
					rt.addValue("leaf.width.whole.max.norm", leafWidthInPixels2 * normFactor, "mm");
				rt.addValue("leaf.width.whole.max", leafWidthInPixels2, "px");
			}
			// System.out.print("Leaf width: " + leafWidthInPixels + " // " + leafWidthInPixels2);
		}

		if (skelres != null) {
			skelres.show("Result Skeleton", false);
			getResultSet().setImage(getBlockPosition(), "skeleton_" + cameraType,
					new ImageAndImageData(skelres, input().images().getImageInfo(cameraType)), true);
			getResultSet().setImage(getBlockPosition(), "skeleton_workimage_" + cameraType,
					new ImageAndImageData(inp, input().images().getImageInfo(cameraType)),
					true);
		}
		
		if (getBoolean("Detect Bloom", false)) {
			rt.addValue("bloom.count", bloomLimbCount);
		}
		rt.addValue("leaf.count", leafcount);
		rt.addValue("branchpoint.count", branchcount);
		if (distHorizontal != null)
			rt.addValue("leaf.length.sum.norm", leaflength * normFactor, "mm");
		rt.addValue("leaf.length.sum", leaflength, "px");
		
		if (leafWidthInPixels != null && leafWidthInPixels > 0 && !Double.isNaN(leafWidthInPixels) && !Double.isInfinite(leafWidthInPixels)) {
			if (distHorizontal != null)
				rt.addValue("leaf.width.outer.max.norm", leafWidthInPixels * normFactor, "mm");
			rt.addValue("leaf.width.outer.max", leafWidthInPixels, "px");
		}
		
		if (getBoolean("Detect Bloom", false)) {
			if (bloomLimbCount > 0)
				rt.addValue("bloom", 1);
			else
				rt.addValue("bloom", 0);
		}
		
		if (leafcount > 0) {
			if (distHorizontal != null)
				rt.addValue("leaf.length.mean.norm", leaflength * normFactor / leafcount, "mm");
			rt.addValue("leaf.length.mean", leaflength / leafcount, "px");
		}
		
		if (leafcount > 0) {
			double filled = inp.io().countFilledPixels();
			if (distHorizontal != null)
				rt.addValue("leaf.width.mean.norm", (filled / leaflength) * normFactor, "mm");
			rt.addValue("leaf.width.mean", filled / leaflength, "px");
			// System.out.println(" // " + (int) (filled / leaflength));
		}
		
		if (rt != null)
			getResultSet().storeResults(
					optionsAndResults.getCameraPosition(), cameraType, TraitCategory.GEOMETRY, null, rt,
					getBlockPosition(), this, imageRef);
		
		if (addPostProcessor) {
			final Image skel2d_fin = skel2d.getAsImage();
			final CameraType cameraType_fin = cameraType;
			getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
				
				@Override
				public Image postProcessMask(Image mask) {
					return mask.io().drawSkeleton(skel2d_fin, true, ImageOperation.BACKGROUND_COLORint).getImage();
					// return skel2d_fin.io().or(mask).getImage();
				}
				
				@Override
				public Image postProcessImage(Image image) {
					return image;
				}
				
				@Override
				public CameraType getConfig() {
					return cameraType_fin;
				}
			});
		}
		
		return skel2d.getAsImage();
	}
	
	private synchronized Image MapOriginalOnSkelUseingMedian(Image skeleton, Image original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] img = skeleton.getAs1A().clone();
		int[] oi = original.getAs1A().clone();
		int last = img.length - w;
		int l2 = oi.length - w;
		if (l2 < last)
			last = l2;
		for (int i = 0; i < img.length && i < oi.length; i++) {
			if (i > w && i < last && img[i] != back) {
				int center = oi[i];
				int above = oi[i - w];
				int left = oi[i - 1];
				int right = oi[i + 1];
				int below = oi[i + w];
				img[i] = median(center, above, left, right, below);
			} else
				img[i] = img[i];
		}
		return new Image(w, h, img);
	}
	
	private int median(int a0, int a1, int a2, int a3, int a4) {
		// int center, int above, int left, int right, int below) {
		if (a0 < a1) {
			// swap(a, 0, 1); // a[0]>a[1]
			int t = a0;
			a0 = a1;
			a1 = t;
		}
		if (a2 < a3) {
			// swap(a, 2, 3); // a[2]>a[3]
			int t = a2;
			a2 = a3;
			a3 = t;
		}
		if (a0 < a2)
		{
			// swap(a, 0, 2);
			int t = a0;
			a0 = a2;
			a2 = t;
			// swap(a, 1, 3);
			t = a1;
			a1 = a3;
			a3 = t;
		}
		// we don't require a[0] any more.
		
		if (a1 < a4) {
			// swap(a, 1, 4);
			int t = a1;
			a1 = a4;
			a4 = t;
		}
		if (a1 > a2)
		{
			if (a2 > a4)
				return a2;
			else
				return a4;
		}
		else
		{
			if (a1 > a3)
				return a1;
			else
				return a3;
		}
		// int[] temp = { center, above, left, right, below };
		// java.util.Arrays.sort(temp);
		// return temp[2];
	}
	
	@Override
	public synchronized void postProcessResultsForAllTimesAndAngles(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2allResultsForSnapshot,
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> time2summaryResult,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			CalculatesProperties propertyCalculator) {
		
		for (CameraType ct : getCameraInputTypes())
			for (CameraPosition cp : CameraPosition.getSideAndTop()) {
				for (Long time : new ArrayList<Long>(time2allResultsForSnapshot.keySet())) {
					TreeMap<String, HashMap<String, BlockResultSet>> allResultsForSnapshot = time2allResultsForSnapshot.get(time);
					if (!time2summaryResult.containsKey(time))
						time2summaryResult.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
					for (String configName : time2summaryResult.get(time).keySet())
						for (String tray : time2summaryResult.get(time).get(configName).keySet()) {
							BlockResultSet summaryResult = time2summaryResult.get(time).get(configName).get(tray);
							DoubleAndImageData maxLeafcount = new DoubleAndImageData(-1d, null);
							DoubleAndImageData maxLeaflength = new DoubleAndImageData(-1d, null);
							DoubleAndImageData maxLeaflengthNorm = new DoubleAndImageData(-1d, null);
							ArrayList<DoubleAndImageData> lc = new ArrayList<DoubleAndImageData>();
							
							Integer a = null;
							searchLoop: for (String key : allResultsForSnapshot.keySet()) {
								BlockResultSet rt = allResultsForSnapshot.get(key).get(tray);
								if (rt != null)
									for (BlockResultValue v : rt.searchResults("main.axis.rotation")) {
										if (v.getValue() != null) {
											a = v.getValue().intValue();
											// System.out.println("main.axis.rotation: " + a);
											break searchLoop;
										}
									}
							}
							
							String bestAngle = null;
							if (a != null) {
								a = a % 180;
								Double bestDiff = Double.MAX_VALUE;
								for (String dc : allResultsForSnapshot.keySet()) {
									double d = Double.parseDouble(dc.substring(dc.indexOf(";") + ";".length()));
									if (d >= 0) {
										double dist = Math.abs(a - d);
										if (dist < bestDiff) {
											bestAngle = dc;
											bestDiff = dist;
										}
									}
								}
							}
							// System.out.println("ANGLES WITHIN SNAPSHOT: " + allResultsForSnapshot.size());
							if (cp == CameraPosition.SIDE)
								for (String keyC : allResultsForSnapshot.keySet()) {
									HashMap<String, BlockResultSet> rt = allResultsForSnapshot.get(keyC);
									if (rt.get(tray) == null)
										continue;
									if (bestAngle != null && keyC.equals(bestAngle)) {
										// System.out.println("Best side angle: " + bestAngle);
										Double cntAtBestAngle = null;
										NumericMeasurement3D refAtBestAngle = null;
										for (BlockResultValue v : rt.get(tray).searchResults(new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.count"))) {
											if (v.getValue() != null) {
												cntAtBestAngle = v.getValue();
												refAtBestAngle = v.getBinary();
											}
										}
										if (cntAtBestAngle != null && summaryResult != null) {
											summaryResult.setNumericResult(getBlockPosition(),
													new Trait(cp(), ct, TraitCategory.GEOMETRY, "leaf.count.best_angle"), cntAtBestAngle, this, refAtBestAngle);
										}
									}
									
									if (rt.get(tray) != null)
										for (BlockResultValue v : rt.get(tray).searchResults(new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.count"))) {
											if (v.getValue() != null) {
												if (v.getValue() > maxLeafcount.getValue())
													maxLeafcount = v.getDaV();
												lc.add(v.getDaV());
											}
										}
									for (BlockResultValue v : rt.get(tray).searchResults(new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum"))) {
										if (v.getValue() != null) {
											if (v.getValue() > maxLeaflength.getValue())
												maxLeaflength = v.getDaV();
										}
									}
									for (BlockResultValue v : rt.get(tray).searchResults(new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.norm"))) {
										if (v.getValue() != null) {
											if (v.getValue() > maxLeaflengthNorm.getValue())
												maxLeaflengthNorm = v.getDaV();
										}
									}
								}
							
							if (summaryResult != null && maxLeafcount != null && maxLeafcount.getValue() > 0) {
								summaryResult.setNumericResult(getBlockPosition(),
										new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.count.max"), maxLeafcount.getValue(), this, maxLeafcount.getImageData());
								// System.out.println("MAX leaf count: " + maxLeafcount);
								DoubleAndImageData[] lca = lc.toArray(new DoubleAndImageData[] {});
								Arrays.sort(lca);
								DoubleAndImageData median = lca[lca.length / 2];
								summaryResult.setNumericResult(getBlockPosition(),
										new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.count.median"), median.getValue(), this, median.getImageData());
							}
							if (maxLeaflength != null && maxLeaflength.getValue() > 0)
								summaryResult.setNumericResult(getBlockPosition(),
										new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.max"), maxLeaflength.getValue(), "px", this,
										maxLeafcount.getImageData());
							if (maxLeaflengthNorm != null && maxLeaflengthNorm.getValue() > 0)
								summaryResult.setNumericResult(getBlockPosition(),
										new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.norm.max"), maxLeaflengthNorm.getValue(), "mm", this,
										maxLeaflengthNorm.getImageData());
							
						}
				}
				calculateRelativeValues(
						time2allResultsForSnapshot,
						time2summaryResult,
						getBlockPosition(),
						new String[] {
								new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.max").toString(),
								new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.norm.max").toString(),
								new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum").toString(),
								new Trait(cp, ct, TraitCategory.GEOMETRY, "leaf.length.sum.norm").toString() }, this);
			}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Skeletonize";
	}
	
	@Override
	public String getDescription() {
		return "Skeletonize VIS and FLUO images and extract according skeleton features.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("bloom.area.size",
						"Detected probable bloom area in the fluo image."),
				new CalculatedProperty("leaf.width.whole.max",
						"Average of the maximum width of the whole leaf segments."),
				new CalculatedProperty("leaf.width.whole.max.norm",
						"Average of the maximum width of the whole leaf segments, normalized to real-world coordinates."),
				new CalculatedProperty("bloom.count",
						"Detect (maize) bloom skeleton endpoints."),
				new CalculatedProperty("leaf.count",
						"Leaf count (based on skeleton)."),
				new CalculatedProperty("branchpoint.count",
						"Branchpoint count (based on skeleton)."),
				new CalculatedProperty("leaf.length.sum",
						"Skeleton length."),
				new CalculatedProperty("leaf.length.sum.norm",
						"Skeleton length, , normalized to real-world coordinates."),
				new CalculatedProperty("leaf.width.outer.max",
						"Average of the maximum width of detected leaf-segments. Leaf-segments are shortened and cut, by calculating a "
								+ "convex hull around all skeleton branch points."),
				new CalculatedProperty("leaf.width.outer.max.norm",
						"Average of the maximum width (normalized to real-world coordinates) of detected leaf-segments. "
								+ "Leaf-segments are shortened and cut, by calculating a "
								+ "convex hull around all skeleton branch points."),
				new CalculatedProperty("bloom", "0/1, depending on if a (maize) bloom could be detected in the image."),
				new CalculatedProperty("leaf.length.mean",
						"Skeleton length dividied by the number of detected leaves."),
				new CalculatedProperty("leaf.length.mean.norm",
						"Skeleton length dividied by the number of detected leaves, normalized to real-world coordinates."),
				new CalculatedProperty("leaf.width.mean",
						"Projected plant area divided by skeleton length."),
				new CalculatedProperty("leaf.width.mean.norm",
						"Projected plant area divided by skeleton length, normalized to real-world coordinates."),
				new CalculatedProperty("leaf.count.best_angle",
						"Number of detected leaves from the 'best' side view, according to the main axis as observed from top.")
		};
	}
	
}
