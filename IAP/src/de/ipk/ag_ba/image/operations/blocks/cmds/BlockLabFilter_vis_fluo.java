/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Entzian, Pape, Klukas
 */
public class BlockLabFilter_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null
				|| getInput().getImages().getVis() == null)
			return null;
		else {
			// FlexibleImageStack fis = new FlexibleImageStack();
			int dilate;
			FlexibleImage result;
			FlexibleImage mask = getInput().getMasks().getVis();
			FlexibleImage orig = getInput().getImages().getVis();
			// fis.addImage("mask", mask.copy());
			boolean side = options.getCameraPosition() == CameraPosition.SIDE;
			if (options.isMaize()) {
				dilate = 3;
				result = labFilterVis(side, mask, orig, dilate, debug);
				return result.getIO().filterGray(220, 15, 15).getImage()
						.print("Gray filtered", debug);
			} else {
				dilate = 0;
				result = labFilterVis(side, mask, orig, dilate, debug);
				// fis.addImage("color_filtered", result);
				// for (int a = -80; a <= 0; a += 20)
				// for (int b = -40; b <= 20; b += 10) {
				// FlexibleImage toBeFiltered = result.getIO().thresholdLAB(
				// 26, 226, // all L
				// 127 + a, 500,
				// 0, 127 + b,
				// options.getBackground(), CameraPosition.SIDE, true).getImage();
				// FlexibleImage resultT = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				// fis.addImage("blue0_filtered a=" +
				// a + ", b=" + b, resultT);
				// }
				FlexibleImage toBeFiltered = result.getIO().thresholdLAB(
						10, 240,
						127 - 80, 500,
						0, 127 - 10,
						options.getBackground(), CameraPosition.SIDE, true).erode().dilate(2).getImage();
				result = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				
				// filter white pot
				int an = 5, bn = 5;
				toBeFiltered = result.getIO().thresholdLAB(
						120, 255,
						127 - an, 127 + an,
						127 - bn, 127 + bn,
						options.getBackground(), CameraPosition.SIDE, true).erode().dilate(2).getImage();
				result = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				
				toBeFiltered = result.getIO().thresholdLAB(
						40, 230,
						100, 500,
						0, 100,
						options.getBackground(), CameraPosition.SIDE, true).dilate(8).print("ABC", false).getImage();
				int w = toBeFiltered.getWidth();
				int h = toBeFiltered.getHeight();
				toBeFiltered = toBeFiltered.getIO().getCanvas().fillRect((int) (w * 0.2), 0, (int) (w * 0.6), h, options.getBackground(), 0d).getImage();
				result = result.copy().getIO().applyMaskInversed_ResizeMaskIfNeeded(toBeFiltered, options.getBackground()).getImage();
				// fis.addImage("blue1_filtered", result.copy());
				result = result.getIO().filterGray(220, 15, 15).getImage()
						.print("Gray filtered", debug);
				// fis.addImage("gray filtered", result);
				// fis.print("lab filter vis");
				return result;
			}
		}
	}
	
	private FlexibleImage labFilterVis(boolean sideImage, FlexibleImage mask,
			FlexibleImage orig, int dilate, boolean debug) {
		FlexibleImageStack fis = new FlexibleImageStack();
		
		if (!sideImage && !options.isHighResMaize()) {
			options.clearAndAddIntSetting(Setting.LAB_MIN_L_VALUE_FLUO, 10);
			options.clearAndAddIntSetting(Setting.LAB_MAX_L_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_A_VALUE_FLUO, 80); // 98
			// //
			// 130
			// gerste
			// wegen
			// topf
			options.clearAndAddIntSetting(Setting.LAB_MAX_A_VALUE_FLUO, 255);
			options.clearAndAddIntSetting(Setting.LAB_MIN_B_VALUE_FLUO, 120);// 125
			options.clearAndAddIntSetting(Setting.LAB_MAX_B_VALUE_FLUO, 255);
		}
		
		if (sideImage && options.isBarleyInBarleySystem())
			dilate = 0;
		
		FlexibleImage labResult = labFilter(
				// getInput().getMasks().getVis().getIO().dilate(3,
				// getInput().getImages().getVis()).blur(2).getImage(),
				mask.copy().getIO().blur((dilate > 0 ? 3 : 0)).getImage(),
				orig.copy(),
				options.getIntSetting(Setting.LAB_MIN_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_L_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_A_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MIN_B_VALUE_VIS),
				options.getIntSetting(Setting.LAB_MAX_B_VALUE_VIS),
				options.getCameraPosition(), options.isMaize(), false, true,
				options.getBackground()).getIO().erode(dilate > 0 ? 2 : 0)
				.print("before dilate, after lab", debug)
				.dilate(dilate > 0 ? 3 : 0).getImage()
				.print("after lab", debug).getIO().erode(2).getImage();
		
		if (debug) {
			fis.addImage("mask", mask.copy());
			fis.addImage("labresult", labResult.copy());
		}
		
		FlexibleImage result = mask
				.copy()
				.getIO()
				.removePixel(labResult.copy(), options.getBackground(), 1, 105,
						120).getImage();
		
		if (debug)
			fis.addImage("without blue parts", result);
		
		if (!sideImage || !options.isHighResMaize()) {
			if (options.isHighResMaize()) {
				// remove black matts inside the holes
				return result
						.getIO()
						.thresholdLAB(0, 150, 110, 150, 100, 150,
								ImageOperation.BACKGROUND_COLORint,
								CameraPosition.TOP, false, true).getImage();
			} else
				return result;
		}
		
		FlexibleImage potFiltered = labFilter(
				// options.isMaize() ?
				result.copy(), getInput().getImages().getVis().copy(),
				100, // filter anything that is very dark
				255,
				0, // 127 - 10,
				255, // 127 + 10,
				0, // 127 - 10,
				255, // 127 + 10,
				options.getCameraPosition(), options.isMaize(), false, true,
				options.getBackground())
				.getIO()
				.clearImageAbove(mask.getHeight() * 0.6,
						options.getBackground()).erode(1)
				.print("A: " + dilate, debug)
				.threshold(127, 1, ImageOperation.BACKGROUND_COLORint)
				.print("B", debug).dilate(11).dilateHor(Integer.MAX_VALUE)
				.print("C", debug).debug(fis, "before hor dilate", debug)
				.dilateHor(2 + dilate * 3)
				.debug(fis, "after hor dilate", debug).// blur(4).
				getImage();// : null;
		
		// // move down the pot filtering by a number of pixels, in order not
		// // delete leafs
		// // that are visible within the pot / before the pot background
		// if (!options.isMaize()) {
		// potFiltered = labFilter(result.copy(),
		// getInput().getImages().getVis().copy(),
		// 100, // filter anything that is very dark
		// 255,
		// 0, // 127 - 10,
		// 255, // 127 + 10,
		// 0, // 127 - 10,
		// 255, // 127 + 10,
		// options.getCameraPosition(), options.isMaize(), false,
		// true, options.getBackground())
		// .getIO()
		// .clearImageAbove(mask.getHeight() * 0.6,
		// options.getBackground()).erode(1)
		// .print("A: " + dilate, debug)
		// .threshold(127, 1, ImageOperation.BACKGROUND_COLORint)
		// .getImage();
		// }
		
		if (debug) {
			fis.addImage("removed black", potFiltered);
			fis.print("debug lab filter");
		}
		
		return result = result
				.getIO()
				.removePixel(
						potFiltered.print(
								"black parts removed from blue parts removal",
								false), options.getBackground())// , 50, 110, 1)
				.getImage();
	}
	
	// @Override
	// protected FlexibleImage processFLUOmask() {
	// if (getInput().getMasks().getFluo() == null &&
	// getInput().getImages().getFluo() == null)
	// return null;
	//
	// if (options.getCameraPosition() == CameraPosition.SIDE)
	// return getInput().getMasks().getFluo();
	// else
	// return labFilter(getInput().getMasks().getFluo(),
	// getInput().getImages().getFluo(),
	// options.getIntSetting(Setting.LAB_MIN_L_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_L_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MIN_A_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_A_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MIN_B_VALUE_FLUO),
	// options.getIntSetting(Setting.LAB_MAX_B_VALUE_FLUO),
	// options.getCameraPosition(),
	// options.isMaize(), false, false, options.getBackground());
	// }
	
	static FlexibleImage labFilter(FlexibleImage workMask,
			FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL,
			int lowerValueOfA, int upperValueOfA, int lowerValueOfB,
			int upperValueOfB, CameraPosition typ, boolean maize,
			boolean blueStick, boolean blueBasket, int back) {
		if (workMask == null)
			return null;
		int[] workMask1D = workMask.getAs1A();
		// int[] result = new int[workMask1D.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		FlexibleImage mod = ImageOperation.thresholdLAB3(width, height,
				workMask1D, workMask1D, lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA, lowerValueOfB, upperValueOfB,
				back, typ, maize, blueStick, originalImage.getAs2A(),
				blueBasket);
		
		return new ImageOperation(mod).applyMask_ResizeSourceIfNeeded(
				workMask1D, width, height, back).getImage();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages,
			FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		// processedImages.setVis(mod);
	}
}
