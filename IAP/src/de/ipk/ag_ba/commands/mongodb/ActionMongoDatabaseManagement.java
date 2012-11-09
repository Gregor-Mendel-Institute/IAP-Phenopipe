package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import org.SystemOptions;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionAnalyzeAllExperiments;
import de.ipk.ag_ba.commands.ActionDeleteHistoryOfAllExperiments;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.task_management.DeleteCloudJobsAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class ActionMongoDatabaseManagement extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experimentList;
	
	public ActionMongoDatabaseManagement(String tooltip) {
		super(tooltip);
	}
	
	public ActionMongoDatabaseManagement(String tooltip, MongoDB m, ArrayList<ExperimentHeaderInterface> experimentList) {
		super(tooltip);
		this.m = m;
		this.experimentList = experimentList;
	}
	
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		
	}
	
	@Override
	public String getDefaultTitle() {
		return "Database Management & Tools";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getToolbox();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		// "Database Management",
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		result.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
				"Show server status information", m, "serverStatus", "Server Status"), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
				"Show database statistics", m, new BasicDBObject("dbstats", 1), "Database Statistics"), src.getGUIsetting()));
		if (SystemOptions.getInstance().getBoolean("IAP", "grid_remote_execution", true))
			result.add(new NavigationButton(new ActionAnalyzeAllExperiments(m, experimentList), src.getGUIsetting()));
		
		if (IAPmain.getRunMode() != IAPrunMode.WEB) {
			boolean showDeleteCloudJobsIcon = SystemOptions.getInstance().getBoolean("IAP", "Show Delete Cloud Jobs Icon", true);
			if (showDeleteCloudJobsIcon) {
				NavigationButton deleteCloudJobs = new NavigationButton(
						new DeleteCloudJobsAction(m), guiSetting);
				result.add(deleteCloudJobs);
			}
			
		}
		result.add(new NavigationButton(new ActionMongoDbReorganize(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionMongoDbCompact(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionDeleteHistoryOfAllExperiments(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionDataExportCSVfileList(m, experimentList), src.getGUIsetting()));
		
		return result;
	}
	
}
