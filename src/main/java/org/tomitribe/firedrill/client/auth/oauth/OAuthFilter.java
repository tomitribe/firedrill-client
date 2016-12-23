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
import org.tomitribe.firedrill.client.auth.signature.SignatureFilter;
import org.tomitribe.firedrill.util.WeightedRandomResult;
import org.tomitribe.sabot.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
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
@ApplicationScoped
public class OAuthFilter implements ClientRequestFilter {
    @Inject
    private Client client;
    @Inject
    @Config("oauth.token.server")
    private String oAuthTokenServer;

    private static final Map<String, Token> tokenCache = new ConcurrentHashMap<>();
    private WeightedRandomResult<Secret> secrets;
    private WeightedRandomResult<User> users;

    @PostConstruct
    private void init() {
        this.secrets = new WeightedRandomResult<>(createSecrets());
        this.users = new WeightedRandomResult<>(createUsers());
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();

        final User user = users.get();
        tokenCache.computeIfAbsent(user.getUsername(), s -> getToken(user).orElse(null));
        if (Math.random() * 100 > 90) {
            tokenCache.computeIfPresent(user.getUsername(), (s, token) -> getRefreshToken(token).orElse(null));
        }

        Optional.ofNullable(tokenCache.get(user.getUsername()))
                .ifPresent(token -> {
                    if (isSignatureAuthentication(requestContext)) {
                        headers.add("Bearer", token.access_token);
                    } else {
                        headers.add(AUTHORIZATION, "Bearer " + token.access_token);
                    }
                });
    }

    private Optional<Token> getToken(final User user) {
        final Secret secret = secrets.get();
        final Form form = new Form();
        form.param("client_id", secret.getClientId());
        form.param("client_secret", secret.getClientSecret());
        form.param("username", user.getUsername());
        form.param("password", user.getPassword());
        form.param("grant_type", "password");
        return postToken(form);
    }

    private Optional<Token> getRefreshToken(final Token token) {
        final Secret secret = secrets.get();
        final Form form = new Form();
        form.param("client_id", secret.getClientId());
        form.param("client_secret", secret.getClientSecret());
        form.param("refresh_token", token.refresh_token);
        form.param("grant_type", "refresh_token");
        return postToken(form);
    }

    private Optional<Token> postToken(final Form form) {
        final Response response = client.target(oAuthTokenServer)
                                        .request(APPLICATION_JSON_TYPE)
                                        .post(Entity.entity(form, APPLICATION_FORM_URLENCODED_TYPE));

        return OK.getStatusCode() == response.getStatus() ?
               Optional.of(response.readEntity(Token.class)) :
               Optional.empty();
    }

    private boolean isSignatureAuthentication(final ClientRequestContext requestContext) {
        return requestContext.getConfiguration().getInstances()
                             .stream()
                             .filter(o -> o instanceof SignatureFilter)
                             .findAny()
                             .isPresent();
    }

    private List<Secret> createSecrets() {
        final List<Secret> secrets = Stream.of(Secret.secret("imdb", "m0vies"),
                                               Secret.secret("netflix", "m0vies"),
                                               Secret.secret("amazon-prime", "m0vies"),
                                               Secret.secret("cinema", "m0vies"))
                                           .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        final List<Secret> distributedSecrets = new ArrayList<>();
        secrets.forEach(secret -> {
            final Random random = new Random();
            for (int i = 0; i < (random.nextInt(10) + 1) * 2; i++) {
                if (random.nextInt(100) < 75) {
                    distributedSecrets.add(secret);
                } else {
                    distributedSecrets.add(random.nextBoolean() ?
                                        Secret.secret(secret.clientId, "wrong") :
                                        Secret.secret(secret.clientId + "1", secret.clientSecret));
                }
            }
        });

        Collections.shuffle(distributedSecrets);
        return distributedSecrets;
    }

    private List<User> createUsers() {
        final List<User> users = Stream.of(User.user("eric", "trey"),
                                           User.user("kenny", "matt"),
                                           User.user("kyle", "matt"),
                                           User.user("stan", "trey"),
                                           User.user("bebe", "jennifer"),
                                           User.user("sharon", "april"),
                                           User.user("sheila", "mona"))
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
    @AllArgsConstructor(staticName = "secret")
    private static class Secret {
        private String clientId;
        private String clientSecret;
    }

    @Data
    @AllArgsConstructor(staticName = "user")
    private static class User {
        private String username;
        private String password;
    }

    @Data
    private static class Token {
        private String access_token;
        private String refresh_token;
        private String token_type;
    }
}
