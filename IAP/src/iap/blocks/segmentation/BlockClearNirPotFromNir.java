/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author Klukas
 */
public class BlockClearNirPotFromNir extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		Image nir = processedImages.nir();
		Image nirMask = processedMasks.nir();
		
		if (nir == null || nir.getWidth() < 10 || nir.getHeight() < 10)
			return;
		
		if (nirMask == null || nirMask.getWidth() < 10 || nirMask.getHeight() < 10)
			return;
		
		int[][] nirArray = nir.getAs2A();
		int[][] nirArrayMask = nirMask.getAs2A();
		
		int w = nirArray.length;
		int h = nirArray[0].length;
		
		if (h > 22) {
			for (int y = h - 1; y >= 21; y--) {
				int maxContinousBlack = 0;
				int currentlyBlack = 0;
				int nSkipWhite = 0;
				for (int x = 0; x < w; x++) {
					int v = nirArray[x][y] & 0x0000ff;
					int vAbove = nirArray[x][y - 20] & 0x0000ff;
					if (v < 110 || nSkipWhite < 10 || vAbove < 110) {
						currentlyBlack++;
						if (v >= 100)
							nSkipWhite++;
					} else {
						nSkipWhite = 0;
						if (currentlyBlack > maxContinousBlack)
							maxContinousBlack = currentlyBlack;
						currentlyBlack = 0;
					}
				}
				int gray = ImageOperation.BACKGROUND_COLORint;
				// int gray = (0xFF << 24 | (g & 0xFF) << 16) | ((g & 0xFF) << 8) | ((g & 0xFF) << 0);
				double co = getDouble("co", 0.1);
				if (maxContinousBlack > co * w) {
					for (int x = 0; x < w; x++) {
						nirArray[x][y] = gray;
						nirArrayMask[x][y] = gray;
					}
				} else {
					int cut = y - 3;
					for (y = cut + 3; y >= cut; y--) {
						if (y > 0)
							for (int x = 0; x < w; x++) {
								nirArray[x][y] = gray;
								nirArrayMask[x][y] = gray;
							}
					}
					break;
				}
			}
			processedImages.setNir(new Image(nirArray));
			processedMasks.setNir(new Image(nirArrayMask));
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Remove Pot from NIR";
	}
	
	@Override
	public String getDescription() {
		return "Remove black pot from NIR image (scanns for continues large region).";
	}
}
