package de.ipk.ag_ba.plugins;

import iap.blocks.data_structures.ImageAnalysisBlock;

import java.util.Collection;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.GenericPluginAdapter;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.datasources.DataSource;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;

/**
 * @author Christian Klukas
 */
public class AbstractIAPplugin extends GenericPluginAdapter implements IAPplugin {
	
	@Override
	public NavigationAction[] getHomeNavigationActions() {
		return new NavigationAction[] {};
	}
	
	@Override
	public DataSource[] getDataSources() {
		return new DataSource[] {};
	}
	
	@Override
	public ImageAnalysisBlock[] getImageAnalysisBlocks() {
		return new ImageAnalysisBlock[] {};
	}
	
	@Override
	public ImageIcon getIcon() {
		return getIAPicon();
	}
	
	public static ImageIcon getIAPicon() {
		try {
			return new ImageIcon(GravistoService.loadImage(IAPmain.class, "img/favicon.ico", 48, 48));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public ActionDataProcessing[] getDataProcessingActions(ExperimentReference er) {
		return new ActionDataProcessing[] {};
	}
	
	@Override
	public ActionDataProcessing[] getDataProcessingTools(ExperimentReference er) {
		return new ActionDataProcessing[] {};
	}
	
	@Override
	public Collection<String> getHelpForSettings(String iniFileName, String section, String setting) {
		return null;
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {};
	}
}
