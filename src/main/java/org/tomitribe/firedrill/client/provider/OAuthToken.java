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

public class OAuthToken {
    public final String accessToken;
    public final String refreshToken;
    public final String userName;
    public final String clientId;

    public OAuthToken(String accessToken, String refreshToken, String userName, String clientId) {
        super();
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userName = userName;
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "OAuthToken [accessToken=" + accessToken + ", refreshToken=" + refreshToken + ", userName=" + userName + ", clientId="
                + clientId + "]";
    }
}
