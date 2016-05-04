/*********************************************************************************************
 *
 *
 * 'AdamsBashforthSolver.java', in plugin 'ummisco.gaml.extensions.maths', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gaml.extensions.maths.ode.utils.solver;

import java.util.List;

import org.apache.commons.math3.ode.nonstiff.AdamsBashforthIntegrator;
import org.apache.commons.math3.ode.sampling.StepHandler;

public class AdamsBashforthSolver extends Solver {

	public StepHandler stepHandler;

	public AdamsBashforthSolver(final int nSteps, final double minStep, final double maxStep,
			final double scalAbsoluteTolerance, final double scalRelativeTolerance, final int discretizing_step,
			final List<Double> integrated_time, final List<List<Double>> integrated_val) {
		super((minStep + maxStep) / 2,
				new AdamsBashforthIntegrator(nSteps, minStep, maxStep, scalAbsoluteTolerance, scalRelativeTolerance),
				discretizing_step, integrated_time, integrated_val);
	}

}