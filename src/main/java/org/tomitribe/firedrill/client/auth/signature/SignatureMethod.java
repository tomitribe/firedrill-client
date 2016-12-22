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
package org.tomitribe.firedrill.client.auth.signature;

import org.tomitribe.auth.signatures.Signature;
import org.tomitribe.auth.signatures.Signer;
import org.tomitribe.firedrill.client.auth.AuthMethod;
import org.tomitribe.firedrill.client.scenario.EndpointScenario;
import org.tomitribe.sabot.Config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
public class SignatureMethod implements AuthMethod {
    @Inject
    @Config("SignatureAuth.secret")
    private String secret;
    @Inject
    @Config("SignatureAuth.alias")
    private String alias;

    @Inject
    private EndpointScenario endpointScenario;

    @Override
    public void preExecute(final Client client) throws Exception {
        final Random random = new Random(System.nanoTime());
        final String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(new Date());
        final String digest = new String(
                Base64.getEncoder().encode(MessageDigest.getInstance("sha-256").digest("".getBytes("UTF-8"))));

        final String[] aliases = alias.split(" *, *");
        final String keyId = aliases[random.nextInt(aliases.length)];

        final SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "hmacSHA256");
        final Signature signature =
                new Signature(keyId, "hmac-sha256", null, "(request-target)", "digest", "date");

        final Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.DATE, date);
        headers.put("Digest", digest);

        final Signature sign = new Signer(secretKey, signature).sign(endpointScenario.getEndpoint().getMethod(),
                                                                     endpointScenario.getEndpoint().getPath(),
                                                                     headers);

        client.register(new SignatureAuthFilter(sign, date, digest));
    }

    @Override
    public void postExecute(final Client client, final Response response) throws Exception {

    }
}
