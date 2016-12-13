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
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class OAuthTokenTokenRepository {
    private final Map<String, OAuthToken> cache = new HashMap<>();

    public void cache(OAuthToken oauthToken) {
        synchronized (cache) {
            cache.put(toKey(oauthToken), oauthToken);
        }
    }

    public OAuthToken retrieve(String userName, String clientId) {
        synchronized (cache) {
            return cache.get(toKey(userName, clientId));
        }
    }

    public void remove(String userName, String clientId) {
        synchronized (cache) {
            cache.remove((toKey(userName, clientId)));
        }
    }

    private String toKey(String userName, String clientId) {
        return clientId + ":" + userName;
    }

    private String toKey(OAuthToken oauthToken) {
        return toKey(oauthToken.userName, oauthToken.clientId);
    }
}
