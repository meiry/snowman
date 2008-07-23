package com.sun.darkstar.example.snowman.common.entity.terrain;

import java.io.IOException;

import com.jme.util.export.InputCapsule;
import com.jme.util.export.JMEExporter;
import com.jme.util.export.JMEImporter;
import com.jme.util.export.OutputCapsule;
import com.sun.darkstar.example.snowman.common.entity.EditableEntity;
import com.sun.darkstar.example.snowman.common.entity.enumn.EEntity;

/**
 * <code>TerrainEntity</code> extends <code>EditableEntity</code> to define
 * the editable terrain during world editing stages. It maintains the basic
 * data of the <code>TerrainCluster</code> including the width, the depth
 * and the number of triangles per <code>TerrainMesh</code> value of the
 * <code>TerrainCluster</code>.
 * 
 * @author Yi Wang (Neakor)
 * @author Tim Poliquin (Weenahmen)
 * @version Creation date: 07-01-2008 23:35 EST
 * @version Modified date: 07-02-2008 24:08 EST
 */
public class TerrainEntity extends EditableEntity {
	/**
	 * The width of the <code>TerrainCluster</code>.
	 */
	private int width;
	/**
	 * The depth of the <code>TerrainCluster</code>.
	 */
	private int depth;
	/**
	 * The number of triangles per <code>TerrainMesh</code>.
	 */
	private int trianglesPerMesh;
	
	/**
	 * Constructor of <code>TerrainEntity</code>.
	 */
	public TerrainEntity() {
		super();
	}
	
	/**
	 * Constructor of <code>TerrainEntity</code>.
	 * @param id The integer ID number of this entity.
	 */
	public TerrainEntity(int id) {
		super(EEntity.Terrain, id);
	}
	
	/**
	 * Set the width of the terrain cluster.
	 * @param width The width value to be set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Set the depth of the terrain cluster.
	 * @param depth The depth value to be set.
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	/**
	 * Set the number of triangles per terrain mesh.
	 * @param trianglesPerMesh The number of triangles value.
	 */
	public void setTrianglesPerMesh(int trianglesPerMesh) {
		this.trianglesPerMesh = trianglesPerMesh;
	}
	
	/**
	 * Retrieve the width of the terrain cluster.
	 * @return The integer width value.
	 */
	public int getWidth() {
		return this.width;
	}
	
	/**
	 * Retrieve the depth of the terrain cluster.
	 * @return The integer depth value.
	 */
	public int getDepth() {
		return this.depth;
	}
	
	/**
	 * Retrieve the number of triangles per terrain mesh.
	 * @return The integer number of triangles value.
	 */
	public int getTrianglesPerMesh() {
		return this.trianglesPerMesh;
	}
	
	@Override
	public void write(JMEExporter ex) throws IOException {
		super.write(ex);
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(this.width, "Width", 0);
		oc.write(this.depth, "Depth", 0);
		oc.write(this.trianglesPerMesh, "TrianglesPerMesh", 0);
	}
	
	@Override
	public void read(JMEImporter im) throws IOException {
		super.read(im);
		InputCapsule ic = im.getCapsule(this);
		this.width = ic.readInt("Width", 0);
		this.depth = ic.readInt("Depth", 0);
		this.trianglesPerMesh = ic.readInt("TrianglesPerMesh", 0);
	}
}