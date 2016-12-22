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

import org.tomitribe.firedrill.client.auth.AuthMethod;
import org.tomitribe.sabot.Config;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.tomitribe.firedrill.client.provider.ClientUtils.createClient;
import static org.tomitribe.firedrill.client.provider.ClientUtils.getRandomInt;

public abstract class TargetResourceBase implements Runnable {
    private static Logger logger = Logger.getLogger(TargetResourceBase.class.getName());

    @Inject
    @Named("runningAtomic")
    private AtomicBoolean running;
    @Inject
    @Config("TargetResourceBase.targetUrl")
    private String targetUrl;

    private AuthMethod authMethod;

    @Override
    public void run() {
        while (true) {
            Response response = null;
            try {
                if (running.get()) {
                    sleep();
                    response = post();
                } else {
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dispose(response);
            }
        }
    }

    private Response post() throws Exception {
        Client client = createClient();
        final AuthMethod authMethod = getAuthMethod();
        authMethod.preExecute(client);
        final WebTarget target = createWebTarget(client.target(targetUrl));
        final Response response = executeRequest(target);
        logger.info(String.format("%s - %s - %d", getMethod(), target.getUri(), response.getStatus()));
        authMethod.postExecute(client, response);
        return response;
    }

    public abstract AuthMethod getAuthMethod();

    public abstract WebTarget createWebTarget(final WebTarget webTarget);

    public abstract String getMethod();

    public Response executeRequest(final WebTarget target) {
        return target.request().method(getMethod());
    }

    private void dispose(final Response response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sleep() throws Exception {
        int rateLimit = 1;
        int seconds = secondsSinceMidnight();
        if (seconds <= (86400 / 2)) {
            rateLimit = (seconds / 100);
        } else {
            rateLimit = (seconds - (((seconds - (86400 / 2)) * 2) - 1)) / 100;
        }
        rateLimit = randomizeRateLimit(rateLimit);
        if (rateLimit < 10) {
            rateLimit = 10;
        }
        Thread.sleep(rateLimit);
    }

    private int secondsSinceMidnight() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        minutes += hours * 60;
        seconds += minutes * 60;
        return seconds;
    }

    private int randomizeRateLimit(int rateLimit) {
        int range = (rateLimit > 4 ? rateLimit : 4) / 4;
        int nextInt = getRandomInt(range);
        int base = (rateLimit * 3) / 4;
        return nextInt + base;
    }
}
