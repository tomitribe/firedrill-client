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
package org.tomitribe.firedrill.client.auth.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tomitribe.firedrill.util.WeightedRandomResult;
import org.tomitribe.util.Base64;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
public class BasicFilter implements ClientRequestFilter {
    @Inject
    private Client client;

    private WeightedRandomResult<User> users;

    @PostConstruct
    private void init() {
        this.users = new WeightedRandomResult<>(createUsers());
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        final User user = users.get();
        headers.add(AUTHORIZATION, "Basic " + new String(Base64.encodeBase64(new String(user.getUsername() + ":" + user.getPassword()).getBytes())));
    }

    private List<User> createUsers() {
        final List<User> users = Stream.of(User.user("eric", "password_trey"),
                                           User.user("kenny", "password_matt"),
                                           User.user("kyle", "password_matt"),
                                           User.user("stan", "password_trey"),
                                           User.user("bebe", "password_jennifer"),
                                           User.user("sharon", "password_april"),
                                           User.user("sheila", "password_mona"))
                                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        final List<User> distributedUsers = new ArrayList<>();
        users.forEach(user -> {
            final Random random = new Random();
            for (int i = 0; i < (random.nextInt(10) + 1) * 2; i++) {
                if (random.nextInt(100) < 75) {
                    distributedUsers.add(user);
                } else {
                    distributedUsers.add(random.nextBoolean() ?
                                        User.user(user.getUsername(), "wrong") :
                                        User.user(user.getUsername() + "1", user.getPassword()));
                }
            }
        });

        Collections.shuffle(distributedUsers);
        return distributedUsers;
    }

    @Data
    @AllArgsConstructor(staticName = "user")
    private static class User {
        private String username;
        private String password;
    }

}
