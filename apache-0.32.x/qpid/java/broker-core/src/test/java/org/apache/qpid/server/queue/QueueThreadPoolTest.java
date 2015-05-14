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
package org.apache.qpid.server.queue;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.pool.ReferenceCountingExecutorService;
import org.apache.qpid.server.model.Queue;
import org.apache.qpid.server.model.UUIDGenerator;
import org.apache.qpid.server.util.BrokerTestHelper;
import org.apache.qpid.server.virtualhost.VirtualHostImpl;
import org.apache.qpid.test.utils.QpidTestCase;

public class QueueThreadPoolTest extends QpidTestCase
{

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        BrokerTestHelper.setUp();
    }

    @Override
    public void tearDown() throws Exception
    {
        BrokerTestHelper.tearDown();
        super.tearDown();
    }

    public void test() throws Exception
    {
        int initialCount = ReferenceCountingExecutorService.getInstance().getReferenceCount();
        VirtualHostImpl test = BrokerTestHelper.createVirtualHost("test");

        try
        {
            Map<String,Object> attributes = new HashMap<String, Object>();
            attributes.put(Queue.ID, UUIDGenerator.generateRandomUUID());
            attributes.put(Queue.NAME, "test");
            AMQQueue queue = test.createQueue(attributes);

            assertFalse("Creation did not start Pool.", ReferenceCountingExecutorService.getInstance().getPool().isShutdown());

            assertEquals("References not increased", initialCount + 1, ReferenceCountingExecutorService.getInstance().getReferenceCount());

            queue.close();

            assertEquals("References not decreased", initialCount , ReferenceCountingExecutorService.getInstance().getReferenceCount());
        }
        finally
        {
            test.close();
        }
    }
}
