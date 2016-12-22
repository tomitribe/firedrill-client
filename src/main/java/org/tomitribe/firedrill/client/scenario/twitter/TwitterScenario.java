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
package org.tomitribe.firedrill.client.scenario.twitter;

import org.tomitribe.firedrill.client.TargetResourceBase;
import org.tomitribe.firedrill.client.auth.AuthMethod;
import org.tomitribe.firedrill.client.auth.oauth.OAuthMethod;
import org.tomitribe.firedrill.client.scenario.Endpoint;
import org.tomitribe.firedrill.client.scenario.ScenarioEndpoint;
import org.tomitribe.firedrill.util.WeightedRandomResult;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.shuffle;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
public class TwitterScenario extends TargetResourceBase {
    @Inject
    private ScenarioEndpoint scenarioEndpoint;
    @Inject
    private OAuthMethod oAuthMethod;

    private WeightedRandomResult<Endpoint> endpointsToExecute;

    @PostConstruct
    private void init() {
        final List<Endpoint> allEndpoints = Arrays.asList(
                Endpoint.endpoint("twitter/api/account/settings", "GET"),
                Endpoint.endpoint("twitter/api/account/update_profile_banner", "POST"),
                Endpoint.endpoint("twitter/api/account/update_profile_image", "POST"),
                Endpoint.endpoint("twitter/api/application/rate_limit_status", "GET"),
                Endpoint.endpoint("twitter/api/blocks/ids", "GET"),
                Endpoint.endpoint("twitter/api/blocks/list", "GET"),
                Endpoint.endpoint("twitter/api/direct_messages/show", "GET"),
                Endpoint.endpoint("twitter/api/favorites/destroy", "POST"),
                Endpoint.endpoint("twitter/api/followers/list", "GET"),
                Endpoint.endpoint("twitter/api/friends/ids", "GET"),
                Endpoint.endpoint("twitter/api/friendships/no_retweets/ids", "GET"),
                Endpoint.endpoint("twitter/api/geo/place", "POST"),
                Endpoint.endpoint("twitter/api/geo/similar_places", "GET"),
                Endpoint.endpoint("twitter/api/help/configuration", "GET"),
                Endpoint.endpoint("twitter/api/lists/list", "GET"),
                Endpoint.endpoint("twitter/api/lists/members", "GET"),
                Endpoint.endpoint("twitter/api/lists/members/destroy_all", "POST"),
                Endpoint.endpoint("twitter/api/lists/members/show", "GET"),
                Endpoint.endpoint("twitter/api/saved_searches/list", "GET"),
                Endpoint.endpoint("twitter/api/statuses/firehose", "GET"),
                Endpoint.endpoint("twitter/api/statuses/home_timeline", "GET"),
                Endpoint.endpoint("twitter/api/statuses/mentions_timeline", "GET"),
                Endpoint.endpoint("twitter/api/statuses/retweeters/ids", "GET"),
                Endpoint.endpoint("twitter/api/statuses/retweets/{id}", "GET"),
                Endpoint.endpoint("twitter/api/statuses/sample", "GET"),
                Endpoint.endpoint("twitter/api/statuses/show/{id}", "GET"),
                Endpoint.endpoint("twitter/api/statuses/update_with_media", "POST"),
                Endpoint.endpoint("twitter/api/users/contributees", "GET"),
                Endpoint.endpoint("twitter/api/users/search", "GET"),
                Endpoint.endpoint("twitter/api/users/suggestions/{slug}/members", "GET"),
                Endpoint.endpoint("twitter/api/users/report_spam", "POST"));

        final ArrayList<Endpoint> endpoints = new ArrayList<>();
        endpoints.addAll(allEndpoints.stream()
                                     .filter(e -> e.getMethod().equals("GET"))
                                     .collect(ArrayList::new, this::addRandom, ArrayList::addAll));

        endpoints.addAll(allEndpoints);
        shuffle(endpoints);
        endpoints.stream().map(Endpoint::getPath).forEach(System.out::println);
        endpointsToExecute = new WeightedRandomResult<>(endpoints);
    }

    @Override
    public AuthMethod getAuthMethod() {
        return oAuthMethod;
    }

    @Override
    public WebTarget createWebTarget(final WebTarget webTarget) {
        scenarioEndpoint.setEndpoint(endpointsToExecute.get());
        return webTarget.path(scenarioEndpoint.getEndpoint().getPath());
    }

    @Override
    public String getMethod() {
        return scenarioEndpoint.getEndpoint().getMethod();
    }

    private void addRandom(final List<Endpoint> endpoints, final Endpoint endpoint) {
          for (int i = 0; i < Math.random() * 10; i ++) {
              endpoints.add(endpoint);
          }
    }
}
