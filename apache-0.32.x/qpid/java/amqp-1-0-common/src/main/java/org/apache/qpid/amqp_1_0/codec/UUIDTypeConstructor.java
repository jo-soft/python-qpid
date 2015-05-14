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
package org.apache.qpid.amqp_1_0.codec;

import org.apache.qpid.amqp_1_0.type.*;
import org.apache.qpid.amqp_1_0.type.transport.Error;
import org.apache.qpid.amqp_1_0.type.transport.ConnectionError;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDTypeConstructor implements TypeConstructor
{
    private static final UUIDTypeConstructor INSTANCE = new UUIDTypeConstructor();


    public static UUIDTypeConstructor getInstance()
    {
        return INSTANCE;
    }

    private UUIDTypeConstructor()
    {
    }

    public Object construct(final ByteBuffer in, ValueHandler handler) throws AmqpErrorException
    {
        if(in.remaining()>=16)
        {
            long msb = in.getLong();
            long lsb = in.getLong();
            return new UUID(msb, lsb);
        }
        else
        {
            org.apache.qpid.amqp_1_0.type.transport.Error error = new Error();
            error.setCondition(ConnectionError.FRAMING_ERROR);
            error.setDescription("Cannot construct UUID: insufficient input data");
            throw new AmqpErrorException(error);

        }
    }

}