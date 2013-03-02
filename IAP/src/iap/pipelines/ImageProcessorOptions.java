/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package iap.pipelines;

import iap.blocks.data_structures.ImageAnalysisBlockFIS;

import java.awt.Color;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;

/**
 * @author klukas
 */
public class ImageProcessorOptions {
	
	public static final double DEFAULT_MARKER_DIST = 1150d;
	
	private CameraPosition cameraTyp = CameraPosition.UNKNOWN;
	private NeighbourhoodSetting neighbourhood = NeighbourhoodSetting.NB4;
	private final int nirBackground = new Color(180, 180, 180).getRGB();
	private int tray_idx;
	private int tray_cnt;
	private int unit_test_idx;
	private int unit_test_steps;
	private SystemOptions optSystemOptionStorage;
	
	public ImageProcessorOptions(SystemOptions options) {
		this.optSystemOptionStorage = options;
	}
	
	public enum CameraPosition {
		UNKNOWN, TOP, SIDE;
		
		@Override
		public String toString() {
			switch (this) {
				case TOP:
					return "top";
				case SIDE:
					return "side";
			}
			return "unknown";
		}
	}
	
	public void setCameraPosition(CameraPosition cameraTyp) {
		this.cameraTyp = cameraTyp;
		
	}
	
	public SystemOptions getOptSystemOptions() {
		return optSystemOptionStorage;
	}
	
	public CameraPosition getCameraPosition() {
		return cameraTyp;
	}
	
	public int getBackground() {
		return ImageOperation.BACKGROUND_COLORint;
	}
	
	public void setNeighbourhood(NeighbourhoodSetting neighbourhood) {
		this.neighbourhood = neighbourhood;
	}
	
	public NeighbourhoodSetting getNeighbourhood() {
		return neighbourhood;
	}
	
	public int getNirBackground() {
		return nirBackground;
	}
	
	public void setTrayCnt(int tray_idx, int tray_cnt) {
		this.tray_idx = tray_idx;
		this.tray_cnt = tray_cnt;
	}
	
	public int getTrayCnt() {
		return tray_cnt;
	}
	
	public int getTrayIdx() {
		return tray_idx;
	}
	
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		this.unit_test_idx = unit_test_idx;
		this.unit_test_steps = unit_test_steps;
	}
	
	public double getUnitTestIdx() {
		return unit_test_idx;
	}
	
	public double getUnitTestSteps() {
		return unit_test_steps;
	}
	
	public void setSystemOptionStorage(SystemOptions systemOptionStorage) {
		this.setOptSystemOptionStorage(systemOptionStorage);
	}
	
	public String getSystemOptionStorageGroup() {
		return "Block Properties - " + getCameraPosition();
	}
	
	private void setOptSystemOptionStorage(SystemOptions optSystemOptionStorage) {
		this.optSystemOptionStorage = optSystemOptionStorage;
	}
	
	public boolean getBooleanSetting(ImageAnalysisBlockFIS block, String title, boolean defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getBoolean(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	private String getSettingName(ImageAnalysisBlockFIS block, String title) {
		return block != null ?
				block.getClass().getCanonicalName() + "//" + title :
				title;
	}
	
	public double getDoubleSetting(ImageAnalysisBlockFIS block, String title, double defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getDouble(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public int getIntSetting(ImageAnalysisBlockFIS block, String title, int defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getInteger(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public String getStringSetting(ImageAnalysisBlockFIS block, String title, String defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getString(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	public Integer[] getIntArraySetting(ImageAnalysisBlockFIS block, String title, Integer[] defaultValue) {
		if (optSystemOptionStorage == null)
			return defaultValue;
		else
			return optSystemOptionStorage.getIntArray(
					getSystemOptionStorageGroup(), getSettingName(block, title), defaultValue);
	}
	
	Double calculatedBlueMarkerDistance = null;
	
	public Double getCalculatedBlueMarkerDistance() {
		return calculatedBlueMarkerDistance;
	}
	
	public void setCalculatedBlueMarkerDistance(double maxDist) {
		this.calculatedBlueMarkerDistance = maxDist;
	}
	
	public Double getREAL_MARKER_DISTANCE() {
		Double realDist = getDoubleSetting(null, "Real Blue Marker Distance", -1);
		if (realDist < 0)
			return null;
		else
			return realDist;
	}
	
}
