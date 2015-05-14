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
package org.apache.qpid.server.consumer;

import org.apache.qpid.server.message.MessageInstance;
import org.apache.qpid.server.message.ServerMessage;
import org.apache.qpid.server.protocol.AMQSessionModel;
import org.apache.qpid.server.util.StateChangeListener;

public interface ConsumerTarget
{


    void acquisitionRemoved(MessageInstance node);

    void removeStateChangeListener(StateChangeListener<ConsumerTarget, State> listener);

    enum State
    {
        ACTIVE, SUSPENDED, CLOSED
    }

    State getState();

    void consumerAdded(ConsumerImpl sub);

    void consumerRemoved(ConsumerImpl sub);

    void addStateListener(StateChangeListener<ConsumerTarget, State> listener);

    long getUnacknowledgedBytes();

    long getUnacknowledgedMessages();

    AMQSessionModel getSessionModel();

    long send(final ConsumerImpl consumer, MessageInstance entry, boolean batch);

    void flushBatched();

    void queueDeleted();

    void queueEmpty();

    boolean allocateCredit(ServerMessage msg);

    void restoreCredit(ServerMessage queueEntry);

    boolean isSuspended();

    boolean close();

    boolean trySendLock();

    void getSendLock();

    void releaseSendLock();

}
