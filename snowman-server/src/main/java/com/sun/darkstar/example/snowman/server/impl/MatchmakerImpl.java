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
package com.sun.darkstar.example.snowman.server.impl;

import com.sun.darkstar.example.snowman.server.interfaces.Matchmaker;
import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
import com.sun.darkstar.example.snowman.server.interfaces.SnowmanGame;
import com.sun.darkstar.example.snowman.common.protocol.messages.ServerMessages;
import com.sun.darkstar.example.snowman.server.interfaces.TeamColor;
import com.sun.darkstar.example.snowman.server.context.SnowmanAppContext;
import com.sun.darkstar.example.snowman.server.interfaces.GameFactory;
import com.sun.darkstar.example.snowman.server.interfaces.EntityFactory;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * This class is the simple Project Snowman matchmaker.  It keeps a
 * list of 4 logged in players,  When all four "seats" are filled, it
 * launches a game session and assigns them to it
 * @author Jeffrey Kesselman
 */
public class MatchmakerImpl implements Matchmaker, Serializable, ManagedObject {

    private static Logger logger = Logger.getLogger(Matchmaker.class.getName());
    private static final int NUMPLAYERSPERGAME = 2;
    public static final long serialVersionUID = 1L;
    /**
     * The list of waiting players
     */
    @SuppressWarnings("unchecked")
    ManagedReference<SnowmanPlayer>[] waiting =
            new ManagedReference[NUMPLAYERSPERGAME];
    
    /**
     * The name of the game to launch
     */
    public String name;
    
    private SnowmanAppContext appContext;
    private GameFactory gameFactory;
    private EntityFactory entityFactory;
    
    /**
     * The constuctor
     * @param name The name of the game to launch.  Must be unique
     */
    public MatchmakerImpl(String name,
                          SnowmanAppContext appContext,
                          GameFactory gameFactory,
                          EntityFactory entityFactory) {
        this.name = name;
        this.appContext = appContext;
        this.gameFactory = gameFactory;
        this.entityFactory = entityFactory;
        clearQueue();
    }

    /**
     * This method resets the witing queue to all null
     * It must be done at initialization to make the places in the list
     * to set the actual players.
     */
    private void clearQueue() {
        appContext.getDataManager().markForUpdate(this);
        for (int i = 0; i < waiting.length; i++) {
            waiting[i] = null;
        }
    }

    /**
     * This mehod is used to find an index to assign to a newly added player
     * @return the first null index in the waiting list
     */
    private int getNullIdx() {
        for (int i = 0; i < waiting.length; i++) {
            if (waiting[i] == null) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method is called to add a player to the list of players waiting for
     * this game session to begin
     * @param player the player to add to the game
     */
    public void addWaitingPlayer(SnowmanPlayer player) {
        appContext.getDataManager().markForUpdate(this);
        int idx = getNullIdx();
        if (idx == -1) {
            logger.severe("Error: tried to add player to full wait quwue");
            return;
        }
        waiting[idx] = appContext.getDataManager().createReference(player);
        if (getNullIdx() == -1) { // full queue
            launchGameSession(name);
        }
    }

    /**
     * This method takes a player off the waiting players list.
     * It is primarily used by the code that handles dropped users
     * @param player the player to remove from the waiting list
     * 
     * @param player the player to remove from the game
     */
    public void removeWaitingPlayer(SnowmanPlayer player) {
        appContext.getDataManager().markForUpdate(this);
        ManagedReference<SnowmanPlayer> playerRef =
                appContext.getDataManager().createReference(player);
        for (int i = 0; i < waiting.length; i++) {
            if ((waiting[i] != null) && (waiting[i].equals(playerRef))) {
                waiting[i] = null;
                return;
            }
        }
    }

    /**
     * This method gets called by the addWaitingPlayer method when the 
     * fourth player gets added.  It creates the game session and starts it.
     * Then it clears the queu so it can wait for 4 new players.
     * @param name The unique name of the game session to launch
     */
    private void launchGameSession(String name) {
        appContext.getDataManager().markForUpdate(this);
        SnowmanGame game = gameFactory.createSnowmanGame(name, appContext, entityFactory);
        TeamColor color = TeamColor.values()[0];
        for (int i = 0; i < waiting.length; i++) {
        	game.addPlayer(waiting[i].get(), color);
        	waiting[i].get().send(ServerMessages.createNewGamePkt(waiting[i].get().getID(), 
            	"default_map"));
            color = TeamColor.values()[
                    (color.ordinal()+1)%TeamColor.values().length];
        }
        game.sendMapInfo();
      
        clearQueue();
    }
}
