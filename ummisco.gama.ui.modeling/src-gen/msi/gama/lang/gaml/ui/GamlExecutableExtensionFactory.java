/*
 * generated by Xtext
 */
package msi.gama.lang.gaml.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

import msi.gama.lang.gaml.ui.internal.GamlActivator;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class GamlExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return GamlActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return GamlActivator.getInstance().getInjector(GamlActivator.MSI_GAMA_LANG_GAML_GAML);
	}
	
}
