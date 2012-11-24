/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;

/**
 * @author Klukas
 */
public class BlRemoveBlackBelt_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	ImageOperation blackBeltMask = null;
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		
		blackBeltMask = null;
		if (input().masks().vis() != null && input().images().vis() != null) {
			ImageOperation vis = input().masks().vis().io();
			FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
			if (fis != null)
				fis.addImage("start", vis.getImage());
			
			if (options.getCameraPosition() == CameraPosition.TOP) {
				// detect black belt
				vis = vis.blur(getDouble("blur", 3))
						.filterRemoveLAB(
								getInt("belt-lab-l-min", 0), getInt("belt-lab-l-max", 130),
								getInt("belt-lab-a-min", 110), getInt("belt-lab-a-max", 130),
								getInt("belt-lab-b-min", 110), getInt("belt-lab-b-max", 140),
								options.getBackground(),
								false)
						.erode(getInt("erode-cnt", 10))
						.dilate(getInt("dilate-cnt", 23))
						.grayscale()
						.threshold(100, options.getBackground(), new Color(100, 100, 100).getRGB()); // filter out black belt
				
				vis = vis.canvas().fillCircle(
						getInt("small-circle-x", 325),
						getInt("small-circle-y", 703),
						getInt("small-circle-d", 30),
						options.getBackground(), 0d).io()
						.print("black belt region", debug);
				
				blackBeltMask = vis;
			}
		}
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (blackBeltMask == null || vis == null)
			return vis;
		vis = input().images().vis().io().applyMask(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().print("Black belt removed from vis", debug);
		return vis;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = input().masks().fluo();
		if (blackBeltMask == null || fluo == null)
			return fluo;
		fluo = input().images().fluo().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().print("Black belt removed from fluo", debug);
		return fluo;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nir = input().masks().nir();
		if (blackBeltMask == null || nir == null)
			return nir;
		nir = input().images().nir().io().applyMask_ResizeMaskIfNeeded(blackBeltMask.getImage().copy(),
				options.getBackground()).getImage().print("Black belt removed from nir", debug);
		return nir;
	}
}
