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
package org.apache.qpid.server.message;

import org.apache.qpid.server.store.StorableMessageMetaData;
import org.apache.qpid.server.txn.ServerTransaction;
import org.apache.qpid.server.util.Action;

public interface MessageDestination extends MessageNode
{

    public String getName();

    /**
     * Routes a message
     *
     *
     * @param message the message to be routed
     * @param routingAddress
     * @param instanceProperties the instance properties
     * @param txn the transaction to enqueue within
     * @param postEnqueueAction action to perform on the result of every enqueue (may be null)
     * @return the number of queues in which the message was enqueued performed
     */
    <M extends ServerMessage<? extends StorableMessageMetaData>> int send(M message,
                                                                          final String routingAddress,
                                                                          InstanceProperties instanceProperties,
                                                                          ServerTransaction txn,
                                                                          Action<? super MessageInstance> postEnqueueAction);
}
