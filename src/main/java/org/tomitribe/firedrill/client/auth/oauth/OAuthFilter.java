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
package org.tomitribe.firedrill.client.auth.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tomitribe.firedrill.util.WeightedRandomResult;
import org.tomitribe.sabot.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author Roberto Cortez
 */
@RequestScoped
public class OAuthFilter implements ClientRequestFilter {
    @Inject
    private Client client;
    @Inject
    @Config("oauth.token.server")
    private String oAuthTokenServer;

    private WeightedRandomResult<User> users;

    @PostConstruct
    private void init() {
        final List<User> users = Stream.of(User.user("eric", "trey"),
                                           User.user("kenny", "matt"),
                                           User.user("kyle", "matt"),
                                           User.user("stan", "trey"),
                                           User.user("bebe", "jennifer"),
                                           User.user("sharon", "april"),
                                           User.user("sheila", "mona"))
                                       .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        final List<User> distributeUsers = new ArrayList<>();
        users.forEach(user -> {
            final Random random = new Random();
            for (int i = 0; i < (random.nextInt(10) + 1) * 2; i++) {
                if (random.nextInt(100) < 75) {
                    distributeUsers.add(user);
                } else {
                    distributeUsers.add(random.nextBoolean() ?
                                        User.user(user.getUsername(), "wrong") :
                                        User.user(user.getUsername() + "1", user.getPassword()));
                }
            }
        });

        Collections.shuffle(distributeUsers);
        distributeUsers.forEach(System.out::println);

        this.users = new WeightedRandomResult<>(distributeUsers);
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        getToken().ifPresent(token -> headers.add(AUTHORIZATION, "Bearer " + token.access_token));
    }

    private Optional<Token> getToken() {
        final User user = users.get();

        final Form form = new Form();
        form.param("username", user.getUsername());
        form.param("password", user.getPassword());
        form.param("grant_type", "password");

        final Response response = client.target(oAuthTokenServer)
                                        .request(APPLICATION_JSON_TYPE)
                                        .post(Entity.entity(form, APPLICATION_FORM_URLENCODED_TYPE));

        return OK.getStatusCode() == response.getStatus() ?
               Optional.of(response.readEntity(Token.class)) :
               Optional.empty();
    }

    @Data
    private static class Token {
        private String access_token;
        private String refresh_token;
        private String token_type;
    }

    @Data
    @AllArgsConstructor(staticName = "user")
    static class User {
        private String username;
        private String password;
    }
}
