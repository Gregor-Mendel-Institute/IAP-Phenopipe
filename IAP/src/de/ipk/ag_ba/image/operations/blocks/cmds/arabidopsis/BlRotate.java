/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Klukas
 */
public class BlRotate extends AbstractBlock {
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		double r = getDouble("Rotate Main Image " + image.getType(), image.getType() == FlexibleImageType.IR ? -90d : 0d);
		if (image != null && Math.abs(r) > 0.001) {
			image = image.io().rotate(r, false).getImage();
		}
		return image;
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		double r = getDouble("Rotate Mask Image " + mask.getType(), mask.getType() == FlexibleImageType.IR ? -90d : 0d);
		if (mask != null && Math.abs(r) > 0.001) {
			mask = mask.io().rotate(r, false).getImage();
		}
		return mask;
	}
	
}
