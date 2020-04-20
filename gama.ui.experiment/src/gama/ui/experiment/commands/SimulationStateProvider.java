/*********************************************************************************************
 *
 * 'SimulationStateProvider.java, in plugin gama.ui.experiment.experiment, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package gama.ui.experiment.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import gama.GAMA;
import gama.common.interfaces.experiment.IExperimentPlan;
import gama.common.interfaces.gui.IGui;
import gama.runtime.ISimulationStateProvider;

public class SimulationStateProvider extends AbstractSourceProvider implements ISimulationStateProvider {

	public final static String SIMULATION_RUNNING_STATE = "gama.ui.experiment.experiment.SimulationRunningState";
	public final static String SIMULATION_TYPE = "gama.ui.experiment.experiment.SimulationType";
	public final static String SIMULATION_STEPBACK = "gama.ui.experiment.experiment.SimulationStepBack";

	private final static Map<String, String> map = new HashMap<>(3);

	@Override
	public void dispose() {
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { SIMULATION_RUNNING_STATE, SIMULATION_TYPE, SIMULATION_STEPBACK };
	}

	@Override
	public Map<String, String> getCurrentState() {
		final String state = GAMA.getGui().getExperimentState("");
		final IExperimentPlan exp = GAMA.getExperiment();
		final String type = exp == null ? IGui.NONE
				: exp.isBatch() ? "BATCH" : exp.isMemorize() ? "MEMORIZE" : "REGULAR";
		
		String canStepBack = "CANNOT_STEP_BACK";
		if (exp != null) {
			if (exp.getAgent() != null) {
				canStepBack = exp.getAgent().canStepBack() ? "CAN_STEP_BACK" : "CANNOT_STEP_BACK";
			}
		}		
		
		map.put(SIMULATION_RUNNING_STATE, state);
		map.put(SIMULATION_TYPE, type);
		map.put(SIMULATION_STEPBACK, canStepBack);
		return map;
	}

	/**
	 * Change the UI state based on the state of the simulation (none, stopped,
	 * running or notready)
	 */
	@Override
	public void updateStateTo(final String state) {
		fireSourceChanged(ISources.WORKBENCH, SIMULATION_RUNNING_STATE, state);
		final IExperimentPlan exp = GAMA.getExperiment();
		final String type = exp == null ? "NONE" : exp.isBatch() ? "BATCH" : exp.isMemorize() ? "MEMORIZE" : "REGULAR";
		fireSourceChanged(ISources.WORKBENCH, SIMULATION_TYPE, type);

		String canStepBack = "CANNOT_STEP_BACK";

		if (exp != null) {
			if (exp.getAgent() != null) {
				canStepBack = exp.getAgent().canStepBack() ? "CAN_STEP_BACK" : "CANNOT_STEP_BACK";
			}
		}

		fireSourceChanged(ISources.WORKBENCH, SIMULATION_STEPBACK, canStepBack);

	}

}