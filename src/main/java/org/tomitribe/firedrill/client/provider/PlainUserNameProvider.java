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
package org.tomitribe.firedrill.client.provider;

import javax.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class PlainUserNameProvider implements UserNameProvider {
    private final String[] userNames;
    private final Random random = new SecureRandom(SecureRandom.getSeed(4));

    public PlainUserNameProvider() {
        try {
            List<String> userNames = new LinkedList<>();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/users.txt"), StandardCharsets.UTF_8));
            String buffer;
            while ((buffer = br.readLine()) != null) {
                userNames.add(buffer);
            }
            this.userNames = userNames.toArray(new String[userNames.size()]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String randomUserName() {
        return userNames[random.nextInt(userNames.length)];
    }
}
