/*******************************************************************************************************
 *
 * gaml.expressions.UserLocationUnitExpression.java, in plugin gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.constants;

import gama.metamodel.shape.GamaPoint;
import gama.runtime.scope.IScope;
import gaml.types.Types;

public class UserLocationUnitExpression extends UnitConstantExpression<GamaPoint> {

	public UserLocationUnitExpression(final String doc) {
		super(new GamaPoint(), Types.POINT, "user_location", doc, null);
	}

	@Override
	public GamaPoint _value(final IScope scope) {
		return scope.getGui().getMouseLocationInModel();
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public boolean isContextIndependant() {
		return false;
	}

}