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

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * @author Roberto Cortez
 */
public class SignatureAuthFilter implements ClientRequestFilter {
    private Signature signature;
    private String date;
    private String digest;

    public SignatureAuthFilter(final Signature signature, final String date, final String digest) {
        this.signature = signature;
        this.date = date;
        this.digest = digest;
    }

    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, signature.toString());
        headers.add(HttpHeaders.DATE, date);
        headers.add("Digest", digest);
    }
}