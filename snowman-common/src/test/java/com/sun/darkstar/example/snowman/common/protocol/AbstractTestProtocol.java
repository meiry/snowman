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

import com.sun.darkstar.example.snowman.common.protocol.enumn.EOPCODE;
import com.sun.darkstar.example.snowman.common.protocol.processor.IProtocolProcessor;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.Assert;
import org.easymock.EasyMock;


/**
 * Test the Protocol class
 * 
 * @author Owen Kellett
 */
public abstract class AbstractTestProtocol 
{
    private Protocol source;
    
    /**
     * Get the source object to be tested
     */
    public Protocol getSource() {
        return source;
    }
    public void setSource(Protocol source) {
        this.source = source;
    }

    
    /**
     * Verify that READY packet contains ready opcode and
     * timestamp
     */
    @Test
    public void testCreateReadyPkt() {
        ByteBuffer readyPacket = getSource().createReadyPkt();
        readyPacket.flip();
        checkOpcodeAndTimestamp(readyPacket, EOPCODE.READY);
        
        //ensure we are at the end of the buffer
        Assert.assertFalse(readyPacket.hasRemaining());
    }
    
    /**
     * Verify the standard header of the packet.
     * First check that the given opcode matches the first byte of
     * the packet.  Then check that the next 8 bytes are a long which
     * is equivalent to a timestamp approximately equal to now.
     * (Checks that it is within 20 ms)
     * @param packet packet to check
     * @param opcode opcode to verify against
     */
    protected void checkOpcodeAndTimestamp(ByteBuffer packet, 
                                           EOPCODE opcode) {
        byte opbyte = packet.get();
        Assert.assertTrue((opbyte >= 0) && (opbyte < EOPCODE.values().length));

        EOPCODE code = EOPCODE.values()[opbyte];
        Assert.assertTrue(code == opcode);
        
        long timestamp = packet.getLong();
        long now = System.currentTimeMillis();
        
        Assert.assertTrue((now - timestamp) < 20 &&
                          (now - timestamp) >= 0);
    }
    
    
    @Test
    public void parseReadyPkt() {
        ByteBuffer readyPacket = getSource().createReadyPkt();
        readyPacket.flip();
        
        IProtocolProcessor mockProcessor = EasyMock.createMock(IProtocolProcessor.class);
        //record expected processor calls
        mockProcessor.ready();
        EasyMock.replay(mockProcessor);
        
        getSource().parsePacket(readyPacket, mockProcessor);
        
        EasyMock.verify(mockProcessor);
        
        //ensure we are at the end of the buffer
        Assert.assertFalse(readyPacket.hasRemaining());
    }

}
