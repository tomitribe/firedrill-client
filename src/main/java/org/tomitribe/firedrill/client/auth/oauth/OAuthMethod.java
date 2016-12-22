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

import org.tomitribe.firedrill.client.auth.AuthMethod;
import org.tomitribe.firedrill.client.provider.OAuthToken;
import org.tomitribe.firedrill.client.provider.OAuthTokenClient;
import org.tomitribe.firedrill.client.provider.OAuthTokenTokenRepository;
import org.tomitribe.firedrill.client.provider.UserNameProvider;
import org.tomitribe.sabot.Config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;

import static org.tomitribe.firedrill.client.provider.ClientUtils.getRandomInt;

@ApplicationScoped
public class OAuthMethod implements AuthMethod {
    @Inject
    @Config("OAuth.password")
    private String password;

    @Inject
    @Config("OAuth.clientIdList")
    private String clientIdList;
    private String[] clientIds;

    @Inject
    private UserNameProvider userNameProvider;

    @Inject
    @Config("OAuth.deniedClientIdList")
    private String deniedClientIdList;
    private String[] deniedClientIds;

    @Inject
    private OAuthTokenTokenRepository oauthTokenTokenRepository;

    @Inject
    private OAuthTokenClient oauthTokenClient;

    @PostConstruct
    void postContruct() {
        System.out.println("postContruct() - splitting client ids");
        clientIds = clientIdList.split(",");
        deniedClientIds = deniedClientIdList.split(",");
    }

    @Override
    public void preExecute(Client client) throws Exception {
        String userName = userNameProvider.randomUserName();
        String clientId = clientIds[getRandomInt(clientIds.length)];

        OAuthToken oauthToken = oauthTokenTokenRepository.retrieve(userName, clientId);
        if (oauthToken == null) {
            oauthToken = oauthTokenClient.requestToken(userName, randomizeClientId(clientId), randomizePassword(password));
            oauthTokenTokenRepository.cache(oauthToken);
        }
        client = client.register(new HTTPBearerAuthFilter(oauthToken));
    }

    @Override
    public void postExecute(Client client, Response response) throws Exception {
        int status = response.getStatus();
        Configuration configuration = client.getConfiguration();
        HTTPBearerAuthFilter filter = null;
        for (Object instance : configuration.getInstances()) {
            if (instance instanceof HTTPBearerAuthFilter) {
                filter = (HTTPBearerAuthFilter) instance;
                break;
            }
        }
        OAuthToken oauthToken = filter.oauthToken;
        if (status >= 300 || status < 200) {
            // half the time, try and use the refresh token,
            // the other half the time, get a new access token
            if (getRandomInt(2) == 0) {
                oauthTokenTokenRepository.remove(oauthToken.userName, oauthToken.clientId);
            } else {
                oauthToken = oauthTokenClient.requestToken(oauthToken);
                oauthTokenTokenRepository.cache(oauthToken);
            }
        }
    }

    private String randomizeClientId(String clientId) {
        int chance = getRandomInt(100);
        if (chance >= 98) {
            System.out.println("randomizeClientId() using bad client id");
            return deniedClientIds[getRandomInt(deniedClientIds.length)];
        } else {
            return clientId;
        }
    }

    private String randomizePassword(String userName) {
        int chance = getRandomInt(100);
        if (chance >= 98) {
            System.out.println("randomizePassword() user forgot password");
            return "FORGOT PASSword";
        } else {
            return userName;
        }
    }

}
