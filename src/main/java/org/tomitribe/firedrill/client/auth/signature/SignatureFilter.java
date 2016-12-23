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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tomitribe.auth.signatures.Signer;
import org.tomitribe.firedrill.util.WeightedRandomResult;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author Roberto Cortez
 */
@RequestScoped
public class SignatureFilter implements ClientRequestFilter {
    private WeightedRandomResult<Alias> aliases;

    @PostConstruct
    private void init() {
        final List<Alias> aliases = Stream.of(Alias.alias("eric:eric1", "parker"),
                                              Alias.alias("kenny:kenny1", "stone"),
                                              Alias.alias("kyle:kyle1", "stone"),
                                              Alias.alias("stan:stan1", "parker"),
                                              Alias.alias("bebe:bebe1", "howell"),
                                              Alias.alias("sharon:sharon1", "stewart"),
                                              Alias.alias("sheila:sheila1", "marshall"))
                                          .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        //Collections.shuffle(aliases);
        this.aliases = new WeightedRandomResult<>(aliases);
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final String date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).format(new Date());
        final String digest = getDigest();

        final Alias alias = aliases.get();

        final SecretKey secretKey = new SecretKeySpec(alias.getSecret().getBytes(), "hmacSHA256");
        final org.tomitribe.auth.signatures.Signature signature =
                new org.tomitribe.auth.signatures.Signature(alias.getKeyId(), "hmac-sha256", null, "(request-target)",
                                                            "digest", "date");

        final Map<String, String> signHeaders = new HashMap<>();
        signHeaders.put(HttpHeaders.DATE, date);
        signHeaders.put("Digest", digest);

        final String path = requestContext.getUri().getPath();
        System.out.println("path = " + path);
        final org.tomitribe.auth.signatures.Signature sign =
                new Signer(secretKey, signature).sign(requestContext.getMethod(),
                                                      path, signHeaders);

        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, sign.toString());
        headers.add(HttpHeaders.DATE, date);
        headers.add("Digest", digest);
    }

    private String getDigest() {
        try {
            return new String(getEncoder().encode(MessageDigest.getInstance("sha-256").digest("".getBytes("UTF-8"))));
        } catch (Exception e) {
            return "";
        }
    }

    @Data
    @AllArgsConstructor(staticName = "alias")
    private static class Alias {
        private String keyId;
        private String secret;
    }
}
