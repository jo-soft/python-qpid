/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * This file is auto-generated by Qpid Gentools v.0.1 - do not modify.
 * Supported AMQP version:
 *   8-0
 */

package org.apache.qpid.framing;

import java.io.DataOutput;
import java.io.IOException;

import org.apache.qpid.AMQException;
import org.apache.qpid.codec.MarkableDataInput;

public class BasicGetBody extends AMQMethodBodyImpl implements EncodableAMQDataBlock, AMQMethodBody
{

    public static final int CLASS_ID =  60;
    public static final int METHOD_ID = 70;

    // Fields declared in specification
    private final int _ticket; // [ticket]
    private final AMQShortString _queue; // [queue]
    private final byte _bitfield0; // [noAck]

    // Constructor
    public BasicGetBody(MarkableDataInput buffer) throws AMQFrameDecodingException, IOException
    {
        _ticket = buffer.readUnsignedShort();
        _queue = buffer.readAMQShortString();
        _bitfield0 = buffer.readByte();
    }

    public BasicGetBody(
            int ticket,
            AMQShortString queue,
            boolean noAck
                       )
    {
        _ticket = ticket;
        _queue = queue;
        byte bitfield0 = (byte)0;
        if( noAck )
        {
            bitfield0 = (byte) (((int) bitfield0) | (1 << 0));
        }
        _bitfield0 = bitfield0;
    }

    public int getClazz()
    {
        return CLASS_ID;
    }

    public int getMethod()
    {
        return METHOD_ID;
    }

    public final int getTicket()
    {
        return _ticket;
    }
    public final AMQShortString getQueue()
    {
        return _queue;
    }
    public final boolean getNoAck()
    {
        return (((int)(_bitfield0)) & ( 1 << 0)) != 0;
    }

    protected int getBodySize()
    {
        int size = 3;
        size += getSizeOf( _queue );
        return size;
    }

    public void writeMethodPayload(DataOutput buffer) throws IOException
    {
        writeUnsignedShort( buffer, _ticket );
        writeAMQShortString( buffer, _queue );
        writeBitfield( buffer, _bitfield0 );
    }

    public boolean execute(MethodDispatcher dispatcher, int channelId) throws AMQException
	{
        return dispatcher.dispatchBasicGet(this, channelId);
	}

    public String toString()
    {
        StringBuilder buf = new StringBuilder("[BasicGetBodyImpl: ");
        buf.append( "ticket=" );
        buf.append(  getTicket() );
        buf.append( ", " );
        buf.append( "queue=" );
        buf.append(  getQueue() );
        buf.append( ", " );
        buf.append( "noAck=" );
        buf.append(  getNoAck() );
        buf.append("]");
        return buf.toString();
    }

    public static void process(final MarkableDataInput buffer,
                               final ServerChannelMethodProcessor dispatcher)
            throws IOException
    {

        int ticket = buffer.readUnsignedShort();
        AMQShortString queue = buffer.readAMQShortString();
        boolean noAck = (buffer.readByte() & 0x01) != 0;
        if(!dispatcher.ignoreAllButCloseOk())
        {
            dispatcher.receiveBasicGet(queue, noAck);
        }
    }
}