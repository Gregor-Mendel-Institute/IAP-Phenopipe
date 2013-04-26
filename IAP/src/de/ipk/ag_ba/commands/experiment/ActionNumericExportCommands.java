package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionNumericDataReportCompleteFinishedStep3;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
final class ActionNumericExportCommands extends AbstractNavigationAction {
	private final ArrayList<ThreadSafeOptions> toggles;
	private final MongoDB m;
	private final ExperimentReference experiment;
	
	ActionNumericExportCommands(
			String tooltip, ArrayList<ThreadSafeOptions> toggles,
			MongoDB m, ExperimentReference experiment) {
		super(tooltip);
		this.toggles = toggles;
		this.m = m;
		this.experiment = experiment;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export Numeric Data";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/colorhistogram.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m,
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, "complete, high mem requ.", ExportSetting.ALL),
				guiSetting));
		res.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m,
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, "no histogram, no sections", ExportSetting.NO_HIST_NO_SECTIONS),
				guiSetting));
		res.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m,
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, "histograms", ExportSetting.ONLY_MAIN_HISTO),
				guiSetting));
		res.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m,
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, "sections", ExportSetting.ONLY_SECTIONS),
				guiSetting));
		res.add(new NavigationButton(
				new ActionNumericDataReportCompleteFinishedStep3(
						m,
						experiment,
						toggles,
						true,
						false,
						null, null, null, null, null),
				guiSetting));
		return res;
	}
}