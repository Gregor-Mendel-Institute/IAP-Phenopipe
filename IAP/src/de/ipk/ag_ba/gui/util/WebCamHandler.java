package de.ipk.ag_ba.gui.util;

import java.io.InputStream;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.AbstractResourceIOHandler;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOConfigObject;

public class WebCamHandler extends AbstractResourceIOHandler {
	
	public static final String PREFIX = "webcam";
	
	public WebCamHandler() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">WebCamHandler (I/O Module) has been loaded.");
	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	@Override
	public InputStream getInputStream(IOurl url) throws Exception {
		if (url.getFileName() != null && url.getFileName().startsWith("barley"))
			return IAPwebcam.BARLEY.getSnapshotJPGdata();
		if (url.getFileName() != null && url.getFileName().startsWith("maize"))
			return IAPwebcam.MAIZE.getSnapshotJPGdata();
		return null;
	}
	
	@Override
	public IOurl copyDataAndReplaceURLPrefix(InputStream is, String targetFilename, ResourceIOConfigObject config) throws Exception {
		throw new UnsupportedOperationException("Method not implemented");
	}
	
	@Override
	public Long getStreamLength(IOurl url) throws Exception {
		throw new UnsupportedOperationException("Method not implemented");
	}
}
