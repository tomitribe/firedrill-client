/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.firedrill.client;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class Controller implements Runnable {
    @Resource
    private ManagedExecutorService mes;
    @Any
    @Inject
    private Instance<TargetResourceBase> targetResources;

    @Produces
    @Named("runningAtomic")
    private AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run() {
        System.out.println("run() - starting executors");
        Iterator<TargetResourceBase> trbi = targetResources.iterator();
        while (trbi.hasNext()) {
            TargetResourceBase next = trbi.next();
            System.out.println("run() - starting:" + next.getClass().getSimpleName());
            mes.execute(next);
        }
        running.set(true);
        System.out.println("run() - complete");
    }
}
