package com.sun.darkstar.example.snowman.game.task.state.battle;

import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import com.jme.system.DisplaySystem;
import com.sun.darkstar.example.snowman.common.util.CollisionManager;
import com.sun.darkstar.example.snowman.common.util.SingletonRegistry;
import com.sun.darkstar.example.snowman.exception.ObjectNotFoundException;
import com.sun.darkstar.example.snowman.game.Game;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowmanEntity;
import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
import com.sun.darkstar.example.snowman.game.task.RealTimeTask;
import com.sun.darkstar.example.snowman.game.task.enumn.ETask;

/**
 * <code>SetDestinationTask</code> extends <code>RealTimeTask</code> to
 * set the destination of the user controlled <code>SnowmanEntity</code>.
 * <p>
 * <code>SetDestinationTask</code> execution logic:
 * 1. Create a <code>Ray</code> goes into the screen based on mouse screen
 * coordinates.
 * 2. Find and set the valid destination using <code>CollisionManager</code>.
 * <p>
 * Two <code>SetDestinationTask</code> are considered 'equal' if and only
 * if both of them are setting on the same <code>SnowmanEntity</code>.
 * 
 * @author Yi Wang (Neakor)
 * @version Creation date: 07-21-2008 15:58 EST
 * @version Modified date: 07-21-2008 18:21 EST
 */
public class SetDestinationTask extends RealTimeTask {
	/**
	 * The <code>SnowmanEntity</code> to be set.
	 */
	private final SnowmanEntity snowman;
	/**
	 * The x coordinate of the clicked screen position.
	 */
	private final int x;
	/**
	 * The y coordinate of the clicked screen position.
	 */
	private final int y;

	/**
	 * Constructor of <code>SetDestinationTask</code>.
	 * @param game The <code>Game</code> instance.
	 * @param snowman The <code>SnowmanEntity</code> to be set.
	 * @param x The x coordinate of the clicked screen position.
	 * @param y The y coordinate of the clicked screen position.
	 */
	public SetDestinationTask(Game game, SnowmanEntity snowman, int x, int y) {
		super(ETask.SetDestination, game);
		this.snowman = snowman;
		this.x = x;
		this.y = y;
	}

	@Override
	public void execute() {
		DisplaySystem display = DisplaySystem.getDisplaySystem();
		CollisionManager collisionManager = SingletonRegistry.getCollisionManager();
		Vector3f worldCoords = new Vector3f();
		display.getWorldCoordinates(new Vector2f(this.x, this.y), 1, worldCoords);
		Ray ray = new Ray();
		ray.setOrigin(display.getRenderer().getCamera().getLocation());
		ray.setDirection(worldCoords.subtractLocal(display.getRenderer().getCamera().getLocation()).normalizeLocal());
		Vector3f click = collisionManager.getIntersection(ray, this.game.getActiveState().getWorld(), new Vector3f(), true);
		try {
			Spatial view = (Spatial) ViewManager.getInstance().getView(this.snowman);
			Vector3f local = view.getLocalTranslation();
			Vector3f destination = collisionManager.getDestination(local.x, local.z, click.x, click.z, this.game.getActiveState().getWorld());
			this.snowman.setDestination(destination);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
	}
}