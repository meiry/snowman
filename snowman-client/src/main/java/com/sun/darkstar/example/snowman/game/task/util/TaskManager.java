/*
 * Copyright (c) 2008, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.darkstar.example.snowman.game.task.util;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.jme.util.Debug;
import com.jme.util.stat.StatCollector;
import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
import com.sun.darkstar.example.snowman.common.protocol.enumn.ETeamColor;
import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
import com.sun.darkstar.example.snowman.game.Game;
import com.sun.darkstar.example.snowman.game.entity.scene.CharacterEntity;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowballEntity;
import com.sun.darkstar.example.snowman.game.entity.scene.SnowmanEntity;
import com.sun.darkstar.example.snowman.game.state.enumn.EGameState;
import com.sun.darkstar.example.snowman.game.stats.SnowmanStatType;
import com.sun.darkstar.example.snowman.game.task.enumn.ETask;
import com.sun.darkstar.example.snowman.game.task.enumn.ETask.ETaskType;
import com.sun.darkstar.example.snowman.game.task.state.GameStateTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.AddMOBTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.AttackTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.CorrectionTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.CreateSnowballTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.AttachTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.MoveCharacterTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.MoveSnowballTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.RemoveMOBTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.RespawnTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.StartGameTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.UpdateCursorStateTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.ScoreTask;
import com.sun.darkstar.example.snowman.game.task.state.battle.ChatTask;
import com.sun.darkstar.example.snowman.game.task.state.login.AuthenticateTask;
import com.sun.darkstar.example.snowman.game.task.state.login.ReadyTask;
import com.sun.darkstar.example.snowman.game.task.state.login.ResetLoginTask;
import com.sun.darkstar.example.snowman.game.task.state.login.LoginSuccessTask;
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
 * @version Modified date: 08-14-2008 16:30 EST
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
	private final ConcurrentLinkedQueue<ITask> submitted;
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
		this.submitted = new ConcurrentLinkedQueue<ITask>();
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
			TaskManager.instance.logger.fine("Created new TaskManager.");
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
			this.enqueue(this.submitted.poll());
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
                    case Authenticate:
                        task = new AuthenticateTask(this.game, (String) args[0], (String) args[1]);
                        break;
                    case ResetLogin:
                        task = new ResetLoginTask(this.game, (String) args[1]);
                        break;
                    case LoginSuccess:
                        task = new LoginSuccessTask(this.game);
                        break;
                    case GameState:
                        if(args.length == 1) {
                            task = new GameStateTask(this.game, (EGameState) args[0]);
                        } else {
                            task = new GameStateTask(this.game, (EGameState) args[0], (EEndState) args[1]);
                        }
                        break;
                    case AddMOB:
                        task = new AddMOBTask(this.game, (Integer) args[0], (EMOBType) args[1], (ETeamColor) args[2], (Float) args[3], (Float) args[4], (String) args[5], (Boolean) args[6]);
                        break;
                    case Ready:
                        task = new ReadyTask(this.game);
                        break;
                    case StartGame:
                        task = new StartGameTask(this.game);
                        break;
                    case UpdateCursorState:
                        task = new UpdateCursorStateTask(this.game, (SnowmanEntity) args[0], (Integer) args[1], (Integer) args[2]);
                        break;
                    case MoveCharacter:
                        if (args.length == 3) {
                            if (Debug.stats) {
                                StatCollector.addStat(SnowmanStatType.STAT_LOCALMOVE_COUNT, 1);
                            }
                            task = new MoveCharacterTask(this.game, (CharacterEntity) args[0], (Integer) args[1], (Integer) args[2]);
			} else {
                            if (Debug.stats) {
                                StatCollector.addStat(SnowmanStatType.STAT_ENTITYMOVE_COUNT, 1);
                            }
                            task = new MoveCharacterTask(this.game, (Integer) args[0], (Float) args[1], (Float) args[2], (Float) args[3], (Float) args[4]);
                        }
                        break;
                    case Attack:
                        if (args.length == 2) {
                            task = new AttackTask(this.game, (Integer) args[0], (Integer) args[1]);
                        } else if (args.length == 4) {
                            task = new AttackTask(this.game, (Integer) args[0], (Integer) args[1], (Integer) args[2], (Boolean) args[3]);
                        }
                        break;
                    case CreateSnowball:
                        task = new CreateSnowballTask(this.game, (CharacterEntity) args[0], (CharacterEntity) args[1]);
                        if (Debug.stats) {
                            StatCollector.addStat(SnowmanStatType.STAT_SNOWBALL_COUNT, 1);
                        }
                        break;
                    case MoveSnowball:
                        task = new MoveSnowballTask(this.game, (SnowballEntity) args[0]);
                        break;
                    case Correction:
                        task = new CorrectionTask(this.game, (Integer) args[0], (Float) args[1], (Float) args[2]);
                        break;
                    case Respawn:
                        task = new RespawnTask(this.game, (Integer) args[0], (Float) args[1], (Float) args[2], (Boolean) args[3]);
                        break;
                    case Attach:
                        task = new AttachTask(this.game, (Integer) args[0], (Integer) args[1], (Boolean) args[2]);
                        break;
                    case Remove:
                        task = new RemoveMOBTask(this.game, (Integer) args[0]);
                        break;
                    case Score:
                        task = new ScoreTask(this.game, (ETeamColor) args[0], (Float) args[1], (Float) args[2]);
                        break;
                    case Chat:
                        task = new ChatTask(this.game, (Integer) args[0], (String)args[1], (Boolean) args[2]);
            }
            this.submit(task);
            return task;
	}

	/**
	 * Submit the given task to the <code>TaskManager</code> for later execution.
	 * However, there is no guarantee that the given task will be enqueued.
	 * 
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
							this.logger.fine("Replaced older real time task " + inQueue.getEnumn());
							break;
							// Discard the given one.
						} else {
							this.logger.fine("Discarded given real time task " + given.getEnumn());
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
