/*********************************************************************************************
 *
 *
 * 'thisObject.java', in plugin 'msi.gama.jogl2', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/
package ummisco.gama.opengl.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.texture.Texture;
import com.vividsolutions.jts.geom.*;
import msi.gama.common.util.AbstractGui;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.util.GamaPair;
import msi.gama.util.file.GamaFile;
import ummisco.gama.opengl.JOGLRenderer;
import ummisco.gama.opengl.files.GLModel;

public class RessourceObject extends AbstractObject implements Cloneable {

	public GamaFile file;
	public IAgent agent;
	public double z_layer;
	public Color color;
	public Double alpha;
	public GamaPair<Double, GamaPoint> rotate3D = null;
	


	public RessourceObject(final GamaFile fileName, final IAgent agent, final Color color, Double alpha, final GamaPoint location,
			final GamaPoint dimensions, final GamaPair<Double, GamaPoint> rotate3D) {
		super(color, alpha);
        this.file = fileName;
		this.agent = agent;
		this.z_layer = z_layer;
		this.color= color;
		this.alpha = alpha;
		this.rotate3D = rotate3D;
	}

	@Override
	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace(System.err);
		}
		return o;
	}

	@Override
	public void unpick() {
		picked = false;
	}

	public void pick() {
		picked = true;
	}

	@Override
	public Color getColor() {
		if ( picked ) { return pickedColor; }
		return super.getColor();
	}

	@Override
	public void draw(final GL2 gl, final ObjectDrawer drawer, final boolean picking) {
		JOGLRenderer renderer = drawer.renderer;
		if ( picking ) {
			gl.glPushMatrix();
			gl.glLoadName(pickingIndex);
			if ( renderer.pickedObjectIndex == pickingIndex ) {
				if ( agent != null /* && !picked */ ) {
					renderer.setPicking(false);
					pick();
					renderer.currentPickedObject = this;
					renderer.displaySurface.selectAgent(agent);
				}
			}
			
			if(this.rotate3D != null){
				gl.glTranslated(this.agent.getLocation().getX(), -this.agent.getLocation().getY(), this.agent.getLocation().getZ());
				gl.glRotatef(this.rotate3D.key.floatValue() , (float) this.rotate3D.value.x, (float) this.rotate3D.value.y, (float) this.rotate3D.value.z);	
				gl.glTranslated(-this.agent.getLocation().getX(), this.agent.getLocation().getY(), -this.agent.getLocation().getZ());
			}
			
			
			((GL2) gl).glTranslated(this.agent.getLocation().getX(), renderer.yFlag*this.agent.getLocation().getY(), this.agent.getLocation().getZ());
			((GL2) gl).glRotated(90, 1.0, 0.0, 0.0);
			super.draw(gl, drawer, picking);
			((GL2) gl).glRotated(-90, 1.0, 0.0, 0.0);
			((GL2) gl).glTranslated(-this.agent.getLocation().getX(), -renderer.yFlag*this.agent.getLocation().getY(), -this.agent.getLocation().getZ());		
			gl.glPopMatrix();
		} else {
			
			if(this.rotate3D != null){
				gl.glTranslated(this.agent.getLocation().getX(), -this.agent.getLocation().getY(), this.agent.getLocation().getZ());
				gl.glRotatef(this.rotate3D.key.floatValue() , (float) this.rotate3D.value.x, (float) this.rotate3D.value.y, (float) this.rotate3D.value.z);	
				gl.glTranslated(-this.agent.getLocation().getX(), this.agent.getLocation().getY(), -this.agent.getLocation().getZ());
			}
			
			((GL2) gl).glTranslated(this.agent.getLocation().getX(), renderer.yFlag*this.agent.getLocation().getY(), this.agent.getLocation().getZ());
			((GL2) gl).glRotated(90, 1.0, 0.0, 0.0);
			super.draw(gl, drawer, picking);
			((GL2) gl).glRotated(-90, 1.0, 0.0, 0.0);
			((GL2) gl).glTranslated(-this.agent.getLocation().getX(), -renderer.yFlag*this.agent.getLocation().getY(), -this.agent.getLocation().getZ());
		}
	}

	@Override
	protected Texture computeTexture(GL gl, JOGLRenderer renderer) {
		// TODO Auto-generated method stub
		return null;
	}
}