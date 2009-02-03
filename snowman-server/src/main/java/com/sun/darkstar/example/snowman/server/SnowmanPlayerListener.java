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

package com.sun.darkstar.example.snowman.server;

import com.sun.darkstar.example.snowman.common.util.SingletonRegistry;
import com.sun.darkstar.example.snowman.server.interfaces.SnowmanPlayer;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.ObjectNotFoundException;
import com.sun.sgs.app.AppContext;
import java.nio.ByteBuffer;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <code>SnowmanPlayerListener</code> is responsible for receiving
 * incoming messages and forwarding them to the appropriate 
 * {@link SnowmanPlayer} for processing.
 * 
 * @author Owen Kellett
 */
public class SnowmanPlayerListener implements Serializable,
                                              ClientSessionListener
{
    private static Logger logger = Logger.getLogger(SnowmanPlayerListener.class.getName());
    public static final long serialVersionUID = 1L;
    
    private final ManagedReference<SnowmanPlayer> playerRef;
    
    protected SnowmanPlayerListener(SnowmanPlayer player)
    {
        this.playerRef = AppContext.getDataManager().createReference(player);
    }
    
    public void receivedMessage(ByteBuffer arg0) {
        try {
            SingletonRegistry.getMessageHandler().parseServerPacket(arg0,playerRef.get().getProcessor());
        } catch (ObjectNotFoundException disconnected) {}
    }

    public void disconnected(boolean arg0) {
        try {
            SnowmanPlayer player = playerRef.get();
        
            if (logger.isLoggable(Level.FINE))
                logger.log(Level.FINE,
                           "Player {0} logged out", player.getName());

            if (player.getGame() != null)
                player.getGame().removePlayer(player);

            AppContext.getDataManager().removeObject(player);
        } catch (ObjectNotFoundException alreadyDisconnected) {}
    }
    
    public ManagedReference<SnowmanPlayer> getSnowmanPlayerRef() {
        return playerRef;
    }
}
