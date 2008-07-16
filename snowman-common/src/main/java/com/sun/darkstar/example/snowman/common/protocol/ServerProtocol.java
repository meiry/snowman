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

package com.sun.darkstar.example.snowman.common.protocol;

import java.nio.ByteBuffer;

import com.sun.darkstar.example.snowman.common.protocol.enumn.EEndState;
import com.sun.darkstar.example.snowman.common.protocol.enumn.EMOBType;
import com.sun.darkstar.example.snowman.common.protocol.enumn.EOPCODE;
import com.sun.darkstar.example.snowman.common.protocol.processor.IProtocolProcessor;
import com.sun.darkstar.example.snowman.common.protocol.processor.IServerProcessor;

/**
 * <code>ServerProtocol</code> is a singleton class responsible for generating packets
 * that are sent from the server to the client.
 * <p>
 * <code>ServerProtocol</code> is also responsible for parsing the packets received
 * from the client and invoke the corresponding methods in <code>ServerProcessor</code>.
 * 
 * @author Yi Wang (Neakor)
 * @author Jeffrey Kesselman
 * @author Owen Kellett
 * @version Creation date: 05-29-08 12:10 EST
 * @version Modified date: 06-17-08 10:46 EST
 */
public class ServerProtocol extends Protocol {
    /**
     * The <code>ServerProtocol</code> instance.
     */
    private static ServerProtocol instance;

    /**
     * Constructor of <code>ServerProtocol</code>.
     */
    private ServerProtocol() {
    }

    /**
     * Retrieve the <code>ServerProtocol</code> instance.
     * @return The <code>ServerProtocol</code> instance.
     */
    public static ServerProtocol getInstance() {
        if (ServerProtocol.instance == null) {
            ServerProtocol.instance = new ServerProtocol();
        }
        return ServerProtocol.instance;
    }


    /**
     * Create a "new game" packet which notifies the client to enter battle state
     * with given ID number and map name.
     * @param myID The ID number assigned to the client.
     * @param mapname The name of the map to play on.
     * @return The <code>ByteBuffer</code> "new game" packet.
     */
    public ByteBuffer createNewGamePkt(int myID, String mapname) {
        byte[] bytes = new byte[1 + 8 + 8 + mapname.length()];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.NEWGAME.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(myID);
        buffer.putInt(mapname.length());
        buffer.put(mapname.getBytes());
        return buffer;
    }

    /**
     * Create a "start game" packet which notifies the client to start the battle.
     * @return The <code>ByteBuffer</code> "start game" packet.
     */
    public ByteBuffer createStartGamePkt() {
        byte[] bytes = new byte[1 + 8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.STARTGAME.ordinal());
        buffer.putLong(System.currentTimeMillis());
        return buffer;
    }

    /**
     * Create a "end game" packet which notifies the client to end the battle
     * with given state.
     * @param state The <code>EndState</code> of the battle.
     * @return The <code>ByteBuffer</code> "end game" packet.
     */
    public ByteBuffer createEndGamePkt(EEndState state) {
        byte[] bytes = new byte[1 + 8 + 4];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.ENDGAME.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(state.ordinal());
        return buffer;
    }


    /**
     * Create an "add MOB" packet which notifies the client to add a MOB with
     * given ID number, <code>MOBType</code> at the given position.
     * @param targetID The ID number of the new map object.
     * @param x The X coordinate of the object.
     * @param y The Y coordinate of the object.
     * @param mobType The <code>MOBType</code> of object.
     * @return The <code>ByteBuffer</code> "add MOB" packet.
     */
    public ByteBuffer createAddMOBPkt(int targetID, float x, float y, EMOBType mobType) {
        byte[] bytes = new byte[1 + 8 + 16];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.ADDMOB.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(targetID);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putInt(mobType.ordinal());
        return buffer;
    }
    
    /**
     * Create a "remove MOB" packet which notifies the client to remove
     * the MOB with given ID.
     * @param targetID The ID number of the MOB to be removed.
     * @return The <code>ByteBuffer</code> "remove MOB" packet.
     */
    public ByteBuffer createRemoveMOBPkt(int targetID) {
        byte[] bytes = new byte[1 + 8 + 4];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.REMOVEMOB.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(targetID);
        return buffer;
    }

    /**
     * Create a "move MOB" packet which notifies the client to move the MOB
     * with given ID towards the given destination.
     * @param targetID The ID number of the MOB to be moved.
     * @param startx The x coordinate of the starting position.
     * @param starty The y coordinate of the starting position.
     * @param endx The x coordinate of the ending position.
     * @param endy The y coordinate of the ending position.
     * @param timestart The timestamp that the client started moving
     * @return The <code>ByteBuffer</code> "move MOB" packet.
     */
    public ByteBuffer createMoveMOBPkt(int targetID, float startx, float starty, float endx, float endy, long timestart) {
        byte[] bytes = new byte[1 + 8 + 28];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.MOVEMOB.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(targetID);
        buffer.putFloat(startx);
        buffer.putFloat(starty);
        buffer.putFloat(endx);
        buffer.putFloat(endy);
        buffer.putLong(timestart);
        return buffer;
    }
    
    /**
     * Create a "stop MOB" packet which notifies the client to stop the MOB
     * with given ID at the given destination
     * @param targetID The ID number of the MOB to be stopped.
     * @param x The x coordinate of the stop position.
     * @param y The y coordinate of the stop position.
     * @return The <code>ByteBuffer</code> "move MOB" packet.
     */
    public ByteBuffer createStopMOBPkt(int targetID, float x, float y) {
        byte[] bytes = new byte[1 + 8 + 12];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.STOPMOB.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(targetID);
        buffer.putFloat(x);
        buffer.putFloat(y);
        return buffer;
    }

    /**
     * Create an "attach object" packet which notifies the client to attach
     * the object with given source ID to the object with given target ID.
     * @param sourceID The ID number of the object to be re-attached.
     * @param targetID the ID number of the object to attach it to.
     * @return The <code>ByteBuffer</code> "attach object" packet.
     */
    public ByteBuffer createAttachObjPkt(int sourceID, int targetID) {
        byte[] bytes = new byte[1 + 8 + 8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.ATTACHOBJ.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(sourceID);
        buffer.putInt(targetID);
        return buffer;
    }

    /**
     * Create an "attacked" packet which notifies the client that the object
     * with given source ID has attacked the object with given target ID.
     * @param sourceID The ID number of the attacker.
     * @param targetID The ID number of the target.
     * @return The <code>ByteBuffer</code> "attacked" packet.
     */
    public ByteBuffer createAttackedPkt(int sourceID, int targetID) {
        byte[] bytes = new byte[1 + 8 + 8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.ATTACKED.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(sourceID);
        buffer.putInt(targetID);
        return buffer;
    }

    /**
     * Create an "set HP" packet which notifies the client to set the HP
     * value of the object with given ID.
     * @param objectID The ID number of the object to be set.
     * @param hp The HP value to be set.
     * @return The <code>ByteBuffer</code> "set HP" packet.
     */
    public ByteBuffer createSetHPPkt(int objectID, int hp) {
        byte[] bytes = new byte[1 + 8 + 8];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.put((byte) EOPCODE.SETHP.ordinal());
        buffer.putLong(System.currentTimeMillis());
        buffer.putInt(objectID);
        buffer.putInt(hp);
        return buffer;
    }

    @Override
    protected void parsePacket(EOPCODE code, ByteBuffer packet, IProtocolProcessor processor) {
        IServerProcessor unit = (IServerProcessor) processor;
        switch (code) {
            case MOVEME:
                unit.moveMe(packet.getLong(), 
                            packet.getFloat(), 
                            packet.getFloat(),
                            packet.getFloat(),
                            packet.getFloat());
                break;
            case ATTACK:
                unit.attack(packet.getLong(), 
                            packet.getInt(),
                            packet.getFloat(), 
                            packet.getFloat());
                break;
            case GETFLAG:
                //discard timestamp
                packet.getLong();
                unit.getFlag(packet.getInt());
                break;
            case STOPME:
                unit.stopMe(packet.getLong(),
                            packet.getFloat(),
                            packet.getFloat());
                break;
            default:
                this.logger.info("Unsupported OPCODE: " + code.toString());
        }
    }
}
