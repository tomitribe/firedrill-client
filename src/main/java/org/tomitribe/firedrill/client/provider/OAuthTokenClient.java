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

import org.tomitribe.sabot.Config;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.StringReader;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.tomitribe.firedrill.client.provider.ClientUtils.createClient;

@ApplicationScoped
public class OAuthTokenClient {
    @Inject
    @Config("OAuthTokenClient.soaIagUrl")
    private String soaIagUrl;
    @Inject
    @Config("OAuthTokenClient.clientSecret")
    private String clientSecret;

    public OAuthToken requestToken(String userName, String clientId, String password) {
        System.out.format("requestToken() - userName:%s clientId:%s password:%s\r\n", userName, clientId, password);
        Client client = createClient();
        WebTarget oauthTarget = client.target(soaIagUrl);
        Form form = new Form();
        form.param("client_id", clientId);
        form.param("client_secret", clientSecret);
        form.param("username", userName);
        form.param("password", password);
        form.param("grant_type", "password");
        Response response = oauthTarget.request().post(Entity.entity(form, APPLICATION_FORM_URLENCODED), Response.class);
        return readResponse(userName, clientId, response);
    }

    public OAuthToken requestToken(OAuthToken oauthToken) {
        System.out.format("requestToken() - oauthToken:%s\r\n", oauthToken.toString());
        Client client = createClient();
        WebTarget oauthTarget = client.target(soaIagUrl);
        Form form = new Form();
        form.param("client_id", oauthToken.clientId);
        form.param("client_secret", clientSecret);
        form.param("refresh_token", oauthToken.refreshToken);
        form.param("grant_type", "refresh_token");
        Response response = oauthTarget.request().post(Entity.entity(form, APPLICATION_FORM_URLENCODED), Response.class);
        oauthToken = readResponse(oauthToken.userName, oauthToken.clientId, response);
        return oauthToken;
    }

    private OAuthToken readResponse(String userName, String clientId, Response response) {
        response.bufferEntity();
        String entity = response.readEntity(String.class);
        if (response.getStatus() != 200) {
            System.out.format("readResponse() - got a non-200 reponse, entity:%s\r\n", entity);
            throw new RuntimeException("Couldn't get a token");
        }
        JsonObject token = Json.createReader(new StringReader(entity)).readObject();
        return new OAuthToken(token.getString("access_token"), token.getString("refresh_token"), userName, clientId);
    }
}
