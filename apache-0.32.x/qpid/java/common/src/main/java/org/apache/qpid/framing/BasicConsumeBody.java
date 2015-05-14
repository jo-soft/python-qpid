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

public class BasicConsumeBody extends AMQMethodBodyImpl implements EncodableAMQDataBlock, AMQMethodBody
{

    public static final int CLASS_ID =  60;
    public static final int METHOD_ID = 20;

    // Fields declared in specification
    private final int _ticket; // [ticket]
    private final AMQShortString _queue; // [queue]
    private final AMQShortString _consumerTag; // [consumerTag]
    private final byte _bitfield0; // [noLocal, noAck, exclusive, nowait]
    private final FieldTable _arguments; // [arguments]

    // Constructor
    public BasicConsumeBody(MarkableDataInput buffer) throws AMQFrameDecodingException, IOException
    {
        _ticket = buffer.readUnsignedShort();
        _queue = buffer.readAMQShortString();
        _consumerTag = buffer.readAMQShortString();
        _bitfield0 = buffer.readByte();
        _arguments = EncodingUtils.readFieldTable(buffer);
    }

    public BasicConsumeBody(
            int ticket,
            AMQShortString queue,
            AMQShortString consumerTag,
            boolean noLocal,
            boolean noAck,
            boolean exclusive,
            boolean nowait,
            FieldTable arguments
                           )
    {
        _ticket = ticket;
        _queue = queue;
        _consumerTag = consumerTag;
        byte bitfield0 = (byte)0;
        if( noLocal )
        {
            bitfield0 = (byte) (((int) bitfield0) | (1 << 0));
        }

        if( noAck )
        {
            bitfield0 = (byte) (((int) bitfield0) | (1 << 1));
        }

        if( exclusive )
        {
            bitfield0 = (byte) (((int) bitfield0) | (1 << 2));
        }

        if( nowait )
        {
            bitfield0 = (byte) (((int) bitfield0) | (1 << 3));
        }

        _bitfield0 = bitfield0;
        _arguments = arguments;
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
    public final AMQShortString getConsumerTag()
    {
        return _consumerTag;
    }
    public final boolean getNoLocal()
    {
        return (((int)(_bitfield0)) & ( 1 << 0)) != 0;
    }
    public final boolean getNoAck()
    {
        return (((int)(_bitfield0)) & ( 1 << 1)) != 0;
    }
    public final boolean getExclusive()
    {
        return (((int)(_bitfield0)) & ( 1 << 2)) != 0;
    }
    public final boolean getNowait()
    {
        return (((int)(_bitfield0)) & ( 1 << 3)) != 0;
    }
    public final FieldTable getArguments()
    {
        return _arguments;
    }

    protected int getBodySize()
    {
        int size = 3;
        size += getSizeOf( _queue );
        size += getSizeOf( _consumerTag );
        size += getSizeOf( _arguments );
        return size;
    }

    public void writeMethodPayload(DataOutput buffer) throws IOException
    {
        writeUnsignedShort( buffer, _ticket );
        writeAMQShortString( buffer, _queue );
        writeAMQShortString( buffer, _consumerTag );
        writeBitfield( buffer, _bitfield0 );
        writeFieldTable( buffer, _arguments );
    }

    public boolean execute(MethodDispatcher dispatcher, int channelId) throws AMQException
	{
        return dispatcher.dispatchBasicConsume(this, channelId);
	}

    public String toString()
    {
        StringBuilder buf = new StringBuilder("[BasicConsumeBodyImpl: ");
        buf.append( "ticket=" );
        buf.append(  getTicket() );
        buf.append( ", " );
        buf.append( "queue=" );
        buf.append(  getQueue() );
        buf.append( ", " );
        buf.append( "consumerTag=" );
        buf.append(  getConsumerTag() );
        buf.append( ", " );
        buf.append( "noLocal=" );
        buf.append(  getNoLocal() );
        buf.append( ", " );
        buf.append( "noAck=" );
        buf.append(  getNoAck() );
        buf.append( ", " );
        buf.append( "exclusive=" );
        buf.append(  getExclusive() );
        buf.append( ", " );
        buf.append( "nowait=" );
        buf.append(  getNowait() );
        buf.append( ", " );
        buf.append( "arguments=" );
        buf.append(  getArguments() );
        buf.append("]");
        return buf.toString();
    }

    public static void process(final MarkableDataInput buffer,
                               final ServerChannelMethodProcessor dispatcher)
            throws IOException, AMQFrameDecodingException
    {

        int ticket = buffer.readUnsignedShort();
        AMQShortString queue = buffer.readAMQShortString();
        AMQShortString consumerTag = buffer.readAMQShortString();
        byte bitfield = buffer.readByte();

        boolean noLocal = (bitfield & 0x01) == 0x01;
        boolean noAck = (bitfield & 0x02) == 0x02;
        boolean exclusive = (bitfield & 0x04) == 0x04;
        boolean nowait = (bitfield & 0x08) == 0x08;
        FieldTable arguments = EncodingUtils.readFieldTable(buffer);
        if(!dispatcher.ignoreAllButCloseOk())
        {
            dispatcher.receiveBasicConsume(queue, consumerTag, noLocal, noAck, exclusive, nowait, arguments);
        }
    }
}
