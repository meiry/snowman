package com.sun.darkstar.example.snowman.game.state.scene;

import com.sun.darkstar.example.snowman.common.util.enumn.EWorld;
import com.sun.darkstar.example.snowman.data.util.DataManager;
import com.sun.darkstar.example.snowman.game.Game;
import com.sun.darkstar.example.snowman.game.entity.view.DynamicView;
import com.sun.darkstar.example.snowman.game.state.GameState;
import com.sun.darkstar.example.snowman.game.state.enumn.EGameState;
import com.sun.darkstar.example.snowman.game.world.World;

/**
 * <code>BattleState</code> extends <code>GameState</code> to define the world
 * game state where the player interacts with other players in a snow ball
 * fight.
 * <p>
 * <code>BattleState</code> initializes by first loading the battle scene graph
 * then construct all the <code>IController</code> that is responsible for handling
 * user input events. It is also responsible for propagating the game update
 * invocation down to the <code>IController</code> and <code>IEntity</code> it
 * manages.
 * 
 * @author Yi Wang (Neakor)
 * @version Creation date: 07-11-2008 12:25 EST
 * @version Modified date: 07-17-2008 16:59 EST
 */
public class BattleState extends GameState {

	/**
	 * Constructor of <code>BattleState</code>.
	 * @param game The <code>Game</code> instance.
	 */
	public BattleState(Game game) {
		super(EGameState.BattleState, game);
	}

	@Override
	protected void initializeWorld() {
		this.world = (World)DataManager.getInstance().getWorld(EWorld.Battle);
	}

	@Override
	protected void initializeState() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void updateState(float interpolation) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Initialize a chase camera with the given dynamic view as target.
	 * @param view The target <code>DynamicView</code> instance.
	 */
	public void initializeChaseCam(DynamicView view) {
		// TODO
	}
}