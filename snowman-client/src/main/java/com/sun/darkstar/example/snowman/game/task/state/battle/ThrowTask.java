package com.sun.darkstar.example.snowman.game.task.state.battle;

import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.sun.darkstar.example.snowman.common.entity.view.View;
import com.sun.darkstar.example.snowman.common.physics.enumn.EForce;
import com.sun.darkstar.example.snowman.exception.ObjectNotFoundException;
import com.sun.darkstar.example.snowman.game.Game;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowballEntity;
import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
import com.sun.darkstar.example.snowman.game.physics.util.PhysicsManager;
import com.sun.darkstar.example.snowman.game.task.RealTimeTask;
import com.sun.darkstar.example.snowman.game.task.enumn.ETask;

/**
 * <code>ThrowTask</code> extends <code>RealTimeTask</code> to initiate the
 * throw motion of a snow ball.
 * <p>
 * <code>ThrowTask</code> execution logic:
 * 1. Calculate horizontal motion direction based on current position and destination.
 * 2. Calculate the magnitude of the vertical force based on the maximum height.
 * 3. Calculate the vertical travel time based on vertical velocity.
 * 4. Calculate the magnitude of the horizontal force based on the travel time.
 * 5. Calculate final force as a combination of horizontal and vertical forces.
 * 6. Add the force to the snow ball with calculated direction.
 * 7. Mark the snow ball entity for physics update.
 * <p>
 * <code>ThrowTask</code> are considered 'equal' if and only if the entity
 * of two <code>ThrowTask</code> are 'equal'.
 * 
 * @author Yi Wang (Neakor)
 * @version Creation date: 07-25-2008 17:18 EST
 * @version Modified date: 07-20-2008 13:51 EST
 */
public class ThrowTask extends RealTimeTask {
	/**
	 * The <code>SnowballEntity</code> that is being thrown.
	 */
	private final SnowballEntity snowball;
	
	/**
	 * Constructor of <code>ThrowTask</code>.
	 * @param game The <code>Game</code> instance.
	 * @param snowball The <code>SnowballEntity</code>.
	 */
	public ThrowTask(Game game, SnowballEntity snowball) {
		super(ETask.Throw, game);
		this.snowball = snowball;

	}

	@Override
	public void execute() {
		try {
			// Step 1.
			View view = (View)ViewManager.getInstance().getView(this.snowball);
			Vector3f direction = new Vector3f();
			direction.set(this.snowball.getDestination().subtract(view.getLocalTranslation())).normalizeLocal();
			direction.y = 0;
			// Step 2.
			float speedV = FastMath.sqrt(2 * EForce.Gravity.getMagnitude() * this.snowball.getMaxHeight() / this.snowball.getMass());
			float constant = this.snowball.getMass() / PhysicsManager.getInstance().getRate();
			float magnitudeV = speedV * constant;
			Vector3f forceV = Vector3f.UNIT_Y.clone().multLocal(magnitudeV);
			// Step 3.
			float travelTime = magnitudeV * PhysicsManager.getInstance().getRate() / EForce.Gravity.getMagnitude() * 2;
			// Step 4.
			float distance = this.getPlanarDistance(view.getLocalTranslation(), this.snowball.getDestination());
			float magnitudeH = (distance * this.snowball.getMass()) / (travelTime * PhysicsManager.getInstance().getRate());
			Vector3f forceH = direction.multLocal(magnitudeH);
			// Step 5.
			Vector3f force = forceH.addLocal(forceV);
			// Step 6.
			this.snowball.addForce(force);
			// Step 7.
			PhysicsManager.getInstance().markForUpdate(this.snowball);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve the planar distance between the given two vectors.
	 * @param vector1 The <code>Vector3f</code> of the first position.
	 * @param vector2 The <code>Vector3f</code> of the second position.
	 * @return The float planar distance between the given two vectors.
	 */
	private float getPlanarDistance(Vector3f vector1, Vector3f vector2) {
		float dx = vector1.x - vector2.x;
		float dz = vector1.z - vector2.z;
		return FastMath.sqrt((dx * dx) + (dz * dz));
	}

	@Override
	public boolean equals(Object object) {
		if(super.equals(object)) {
			if(object instanceof ThrowTask) {
				ThrowTask given = (ThrowTask)object;
				return given.snowball.equals(this.snowball);
			}
		}
		return false;
	}
}