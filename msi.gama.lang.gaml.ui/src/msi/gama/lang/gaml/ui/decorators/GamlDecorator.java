/*********************************************************************************************
 * 
 * 
 * 'GamlDecorator.java', in plugin 'msi.gama.lang.gaml.ui', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.lang.gaml.ui.decorators;

/**
 * Copyright (c) 2011 Cloudsmith Inc. and other contributors, as listed below.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Cloudsmith
 * 
 */

import msi.gama.gui.swt.IGamaIcons;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * Simple decorator for error and warning (right now hacking/testing).
 * 
 */
public class GamlDecorator implements ILightweightLabelDecorator {

	private static String decoratorId = "msi.gama.light.decorator";

	private IResourceChangeListener listener = null;

	// private boolean useJDT;

	public GamlDecorator() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		listener = new IResourceChangeListener() {

			@Override
			public void resourceChanged(final IResourceChangeEvent event) {
				IMarkerDelta[] markerDeltas = event.findMarkerDeltas(IMarker.PROBLEM, true);
				if ( markerDeltas.length > 0 ) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							PlatformUI.getWorkbench().getDecoratorManager().update(decoratorId);

						}
					});
				}
			}

		};
		workspace.addResourceChangeListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	@Override
	public void addListener(final ILabelProviderListener listener) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
	 * org.eclipse.jface.viewers.IDecoration)
	 */
	@Override
	public void decorate(final Object element, final IDecoration decoration) {
		if ( element instanceof IResource == false ) { return; }

		// get the max severity from markers
		IResource resource = (IResource) element;
		if ( !resource.isAccessible() ) { return; }

		int severity = -1;
		try {
			severity = resource.findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		// decoration.addOverlay(id, IDecoration.TOP_LEFT);
		// if ( severity < IMarker.SEVERITY_WARNING ) {

		// return; }

		final ImageDescriptor[] overlay = new ImageDescriptor[1];
		if ( severity == IMarker.SEVERITY_ERROR ) {
			overlay[0] = getErrorImageDescriptor();
		} else if ( severity == IMarker.SEVERITY_WARNING ) {
			overlay[0] = getWarningImageDescriptor();
		} else if ( resource.getName().endsWith("gaml") ) {
			overlay[0] = IGamaIcons.OVERLAY_OK.descriptor();
		} else if ( resource instanceof IContainer ) {
			try {
				((IContainer) resource).accept(new IResourceVisitor() {

					@Override
					public boolean visit(final IResource resource) throws CoreException {
						if ( resource.getName().endsWith(".gaml") ) {
							overlay[0] = IGamaIcons.OVERLAY_OK.descriptor();
							return false;
						}
						return true;
					}
				});
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if ( overlay[0] != null ) {
			decoration.addOverlay(overlay[0], IDecoration.BOTTOM_LEFT);
		}

		// TODO Reimplement this for the next release. A way would be to get the experiment names directly from the
		// resource
		// if ( resource.getType() == IResource.FILE && resource.getFileExtension().equals("gaml") ) {
		// XtextResourceSet rs = new XtextResourceSet();
		// URI uri = URI.createPlatformResourceURI(resource.getFullPath().toString(), true);
		// GamlResource xr = (GamlResource) rs.getResource(uri, true);
		// if ( xr.getErrors().isEmpty() ) {
		// ISyntacticElement el = xr.getSyntacticContents();
		// for ( ISyntacticElement ch : el.getChildren() ) {
		// if ( ch.isExperiment() ) {
		// decoration.addOverlay(IGamaIcons.SMALL_RUN.descriptor(), IDecoration.TOP_RIGHT);
		// }
		// }
		// }
		//
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		if ( listener != null ) {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		}

	}

	private ImageDescriptor getErrorImageDescriptor() {
		ImageDescriptor result =
			PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
		// TODO: remove workaround see https://bugs.eclipse.org/bugs/show_bug.cgi?id=304397
		return result != null ? result : JFaceResources.getImageRegistry().getDescriptor(
			"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR");
	}

	private ImageDescriptor getWarningImageDescriptor() {
		ImageDescriptor result =
			PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
		// TODO: remove workaround see https://bugs.eclipse.org/bugs/show_bug.cgi?id=304397
		return result != null ? result : JFaceResources.getImageRegistry().getDescriptor(
			"org.eclipse.jface.fieldassist.IMG_DEC_FIELD_WARNING");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
	 * java.lang.String)
	 */
	@Override
	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.
	 * ILabelProviderListener)
	 */
	@Override
	public void removeListener(final ILabelProviderListener listener) {

	}

}