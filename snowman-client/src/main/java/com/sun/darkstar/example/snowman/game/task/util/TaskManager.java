package com.sun.darkstar.example.snowman.game.task.util;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme.util.Debug;
import com.jme.util.stat.StatCollector;
import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
import com.sun.darkstar.example.snowman.game.Game;
import com.sun.darkstar.example.snowman.game.entity.scene.CharacterEntity;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowballEntity;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowmanEntity;
import com.sun.darkstar.example.snowman.game.state.enumn.EGameState;
import com.sun.darkstar.example.snowman.game.stats.SnowmanStatType;
import com.sun.darkstar.example.snowman.game.task.enumn.*;
import com.sun.darkstar.example.snowman.game.task.enumn.ETask.ETaskType;
import com.sun.darkstar.example.snowman.game.task.state.*;
import com.sun.darkstar.example.snowman.game.task.state.battle.*;
import com.sun.darkstar.example.snowman.game.task.state.login.*;
import com.sun.darkstar.example.snowman.interfaces.IRealTimeTask;
import com.sun.darkstar.example.snowman.interfaces.ITask;
import com.sun.darkstar.example.snowman.unit.Manager;
import com.sun.darkstar.example.snowman.unit.enumn.EManager;

/**
 * <code>TaskManager</code> is a <code>Manager</code> that is responsible for
 * handling all <code>ITask</code> generated by packets received from the server.
 * <code>TaskManager</code> is constructed by the <code>Game</code> at during
 * initialization of the system.
 * <p>
 * <code>TaskManager</code> is updated by <code>Game</code> during main game
 * update/render loop to execute all the buffered <code>ITask</code>.
 * <p>
 * <code>TaskManager</code> executes the buffered <code>ITask</code> in the
 * order in which they are generated and buffered. This means the <code>ITask</code>
 * generated first will be executed first before other <code>ITask</code> generated
 * later.
 * <p>
 * During one single update cycle, <code>TaskManager</code> tries to execute as
 * many buffered <code>ITask</code> as possible. However, it may not execute all
 * the <code>ITask</code> if the total execution time exceeds the allowed maximum
 * limit. The left over <code>ITask</code> is then put on top of the queue and
 * will be executed first during the next update cycle.
 * <p>
 * <code>TaskManager</code> automatically submits the newly created <code>ITask</code>.
 * When a new <code>RealTimeTask</code> is created, <code>TaskManager</code>
 * automatically discards the older version of the task that is considered 'equal'
 * to the newly added one. This guarantees that there is only the newest version of
 * the same <code>RealTimeTask</code> in the task queue.
 * 
 * @author Yi Wang (Neakor)
 * @version Creation date: 06-02-2008 14:40 EST
 * @version Modified date: 08-07-2008 14:00 EST
 */
public class TaskManager extends Manager {
	/**
	 * The <code>TaskManager</code> instance.
	 */
	private static TaskManager instance;
	/**
	 * The <code>Game</code> instance.
	 */
	private final Game game;
	/**
	 * The maximum allowed execution time per cycle in milliseconds.
	 */
	private final float executeTime;
	/**
	 * The maximum allowed enqueuing time per cycle in milliseconds.
	 */
	private final float enqueueTime;
	/**
	 * The buffered <code>ITask</code> queue.
	 */
	private final ConcurrentLinkedQueue<ITask> taskQueue;
	/**
	 * The temporary <code>LinkedList</code> buffer of submitted <code>ITask</code>.
	 */
	private final LinkedList<ITask> submitted;
	/**
	 * The time before the last execution started in nanoseconds.
	 */
	private long starttime;
	/**
	 * The time after the last execution finished in nanoseconds.
	 */
	private long endtime;
	/**
	 * The time elapsed since the start of the current update cycle in milliseconds.
	 */
	private float totaltime;

	/**
	 * Constructor of <code>TaskManager</code>.
	 * @param game The <code>Game</code> instance.
	 */
	private TaskManager(Game game){
		super(EManager.TaskManager);
		this.game = game;
		this.executeTime = 20;
		this.enqueueTime = 5;
		this.taskQueue = new ConcurrentLinkedQueue<ITask>();
		this.submitted = new LinkedList<ITask>();
	}

	/**
	 * Create the task manager for the first time.
	 * @param game The <code>Game</code> instance.
	 * @return The <code>TaskManager</code> instance.
	 */
	public static TaskManager create(Game game) {
		if(game == null) return null;
		if(TaskManager.instance == null) {
			TaskManager.instance = new TaskManager(game);
			TaskManager.instance.logger.info("Created new TaskManager.");
		}
		return TaskManager.instance;
	}

	/**
	 * Retrieve the <code>TaskManager</code> singleton instance.
	 * @return The <code>TaskManager</code> instance.
	 */
	public static TaskManager getInstance() {
		return TaskManager.instance;
	}

	/**
	 * Update the <code>TaskManager</code> to execute the buffered task.
	 */
	public void update() {
		// Enqueue tasks.
		while(!this.submitted.isEmpty() && this.totaltime < this.enqueueTime) {
			this.starttime = System.nanoTime();
			this.enqueue(this.submitted.pop());
			this.endtime = System.nanoTime();
			this.totaltime += (this.endtime-this.starttime)/1000000.0f;
		}
		// Reset total time.
		this.totaltime = 0;
		// Execute as many tasks as possible.
		while(!this.taskQueue.isEmpty() && this.totaltime < this.executeTime) {
			this.starttime = System.nanoTime();
			this.taskQueue.poll().execute();
			this.endtime = System.nanoTime();
			this.totaltime += (this.endtime-this.starttime)/1000000.0f;
		}
		this.totaltime = 0;
	}

	/**
	 * Create a task with given type and submit it to the task execution queue.
	 * @param enumn The <code>ETask</code> enumeration.
	 * @param args The <code>Object</code> arguments for the task.
	 * @return The newly created <code>ITask</code>.
	 */
	public ITask createTask(ETask enumn, Object... args) {
		ITask task = null;
		switch(enumn) {
		case Authenticate: task = new AuthenticateTask(this.game, (String)args[0], (String)args[1]); break;
		case ResetLogin: task = new ResetLoginTask(this.game); break;
		case GameState: task = new GameStateTask(this.game, (EGameState)args[0]); break;
		case AddMOB: task = new AddMOBTask(this.game, (Integer)args[0], (EMOBType)args[1], (ETeamColor) args[2], (Float)args[3], (Float)args[4], (Boolean)args[5]); break;
		case Ready: task = new ReadyTask(this.game); break;
		case StartGame: task = new StartGameTask(this.game); break;
		case UpdateState: task = new UpdateStateTask(this.game, (SnowmanEntity)args[0], (Integer)args[1], (Integer)args[2]); break;
		case MoveCharacter:
			if(args.length == 3) {
				if (Debug.stats) {
					StatCollector.addStat(SnowmanStatType.STAT_LOCALMOVE_COUNT, 1);
				}
				task = new MoveCharacterTask(this.game, (CharacterEntity)args[0], (Integer)args[1], (Integer)args[2]);
			} else {
				if (Debug.stats) {
					StatCollector.addStat(SnowmanStatType.STAT_ENTITYMOVE_COUNT, 1);
				}
				task = new MoveCharacterTask(this.game, (Integer)args[0], (Float)args[1], (Float)args[2], (Float)args[3], (Float)args[4]);
			}
			break;
		case Attacking: 
			if(args.length == 2) {
				task = new AttackTask(this.game, (Integer)args[0], (Integer)args[1]);
			} else if(args.length == 4) {
				task = new AttackTask(this.game, (Integer)args[0], (Integer)args[1], (Integer)args[2], (Boolean)args[3]);
			}
			break;
		case CreateSnowball: 
			task = new CreateSnowballTask(this.game, (CharacterEntity)args[0], (CharacterEntity)args[1]);
			if (Debug.stats) {
				StatCollector.addStat(SnowmanStatType.STAT_SNOWBALL_COUNT, 1);
			}
			break;
		case MoveSnowball: task = new MoveSnowballTask(this.game, (SnowballEntity)args[0]); break;
		}
		this.submit(task);
		return task;
	}

	/**
	 * Submit the given task to the <code>TaskManager</code> for later execution.
	 * However, there is no guarantee that the given task will be enqueued.
	 * @see <code>RealTimeTask</code> for 'equal' determination details.
	 * @param task The <code>ITask</code> to be submitted.
	 * @return True if the task is successfully submitted.
	 */
	public boolean submit(ITask task) {
		if(task == null) return false;
		return this.submitted.add(task);
	}

	/**
	 * Enqueue the given task to the <code>TaskManager</code> for later execution.
	 * If there is an earlier <code>RealTimeTask</code> that is considered 'equal'
	 * as the newly given one, the older version is automatically removed before
	 * the new one is added. If the given task is earlier than the 'equal' one in
	 * the queue, the given task is discarded.
	 * @see <code>RealTimeTask</code> for 'equal' determination details.
	 * @param task The <code>ITask</code> to be added.
	 * @return True if the task is successfully enqueued. False if the given task is discarded.
	 */
	private void enqueue(ITask task) {
		// Check real time tasks.
		if(task.getEnumn().getType() == ETaskType.RealTime) {
			final IRealTimeTask given = (IRealTimeTask)task;
			IRealTimeTask inQueue = null;
			for(ITask t : this.taskQueue) {
				if(t.getEnumn().getType() == ETaskType.RealTime) {
					inQueue = (IRealTimeTask)t;
					if(inQueue.equals(given)) {
						// Remove existing one.
						if(given.isLaterThan(inQueue)) {
							this.taskQueue.remove(inQueue);
							this.logger.info("Replaced older real time task " + inQueue.getEnumn());
							break;
							// Discard the given one.
						} else {
							this.logger.info("Discarded given real time task " + given.getEnumn());
						}
					}
				}
			}
		}
		this.taskQueue.add(task);
	}

	/**
	 * Clean up the <code>TaskManager</code> by removing all tasks in the queue.
	 */
	@Override
	public void cleanup() {
		this.taskQueue.clear();
	}
}
