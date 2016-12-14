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
import org.tomitribe.firedrill.client.scenario.Endpoint;
import org.tomitribe.firedrill.client.scenario.EndpointScenario;
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
    private EndpointScenario endpointScenario;

    private WeightedRandomResult<Endpoint> endpointsToExecute;

    @PostConstruct
    private void init() {
        final List<Endpoint> allEndpoints = Arrays.asList(
                Endpoint.of("twitter/api/account/settings", "GET"),
                Endpoint.of("twitter/api/account/verify_credentials", "GET"),
                Endpoint.of("twitter/api/account/remove_profile_banner", "POST"),
                Endpoint.of("twitter/api/account/settings", "POST"),
                Endpoint.of("twitter/api/account/update_delivery_device", "POST"),
                Endpoint.of("twitter/api/account/update_profile", "POST"),
                Endpoint.of("twitter/api/account/update_profile_background_image", "POST"),
                Endpoint.of("twitter/api/account/update_profile_banner", "POST"),
                Endpoint.of("twitter/api/account/update_profile_colors", "POST"),
                Endpoint.of("twitter/api/account/update_profile_image", "POST"),
                Endpoint.of("twitter/api/application/rate_limit_status", "GET"),
                Endpoint.of("twitter/api/blocks/ids", "GET"),
                Endpoint.of("twitter/api/blocks/list", "GET"),
                Endpoint.of("twitter/api/blocks/create", "POST"),
                Endpoint.of("twitter/api/blocks/destroy", "POST"),
                Endpoint.of("twitter/api/direct_messages/sent", "GET"),
                Endpoint.of("twitter/api/direct_messages/show", "GET"),
                Endpoint.of("twitter/api/direct_messages/destroy", "POST"),
                Endpoint.of("twitter/api/direct_messages/new", "POST"),
                Endpoint.of("twitter/api/favorites/list", "GET"),
                Endpoint.of("twitter/api/favorites/create", "POST"),
                Endpoint.of("twitter/api/favorites/destroy", "POST"),
                Endpoint.of("twitter/api/followers/ids", "GET"),
                Endpoint.of("twitter/api/followers/list", "GET"),
                Endpoint.of("twitter/api/friends/ids", "GET"),
                Endpoint.of("twitter/api/friends/list", "GET"),
                Endpoint.of("twitter/api/friendships/incoming", "GET"),
                Endpoint.of("twitter/api/friendships/lookup", "GET"),
                Endpoint.of("twitter/api/friendships/no_retweets/ids", "GET"),
                Endpoint.of("twitter/api/friendships/outgoing", "GET"),
                Endpoint.of("twitter/api/friendships/show", "GET"),
                Endpoint.of("twitter/api/friendships/create", "POST"),
                Endpoint.of("twitter/api/friendships/destroy", "POST"),
                Endpoint.of("twitter/api/friendships/update", "POST"),
                Endpoint.of("twitter/api/geo/id/{place_id}", "GET"),
                Endpoint.of("twitter/api/geo/reverse_geocode", "GET"),
                Endpoint.of("twitter/api/geo/search", "GET"),
                Endpoint.of("twitter/api/geo/similar_places", "GET"),
                Endpoint.of("twitter/api/geo/place", "POST"),
                Endpoint.of("twitter/api/help/configuration", "GET"),
                Endpoint.of("twitter/api/help/languages", "GET"),
                Endpoint.of("twitter/api/help/privacy", "GET"),
                Endpoint.of("twitter/api/help/tos", "GET"),
                Endpoint.of("twitter/api/lists/list", "GET"),
                Endpoint.of("twitter/api/lists/members", "GET"),
                Endpoint.of("twitter/api/lists/members/show", "GET"),
                Endpoint.of("twitter/api/lists/memberships", "GET"),
                Endpoint.of("twitter/api/lists/ownerships", "GET"),
                Endpoint.of("twitter/api/lists/show", "GET"),
                Endpoint.of("twitter/api/lists/statuses", "GET"),
                Endpoint.of("twitter/api/lists/subscribers", "GET"),
                Endpoint.of("twitter/api/lists/subscribers/show", "GET"),
                Endpoint.of("twitter/api/lists/subscriptions", "GET"),
                Endpoint.of("twitter/api/lists/create", "POST"),
                Endpoint.of("twitter/api/lists/destroy", "POST"),
                Endpoint.of("twitter/api/lists/members/create", "POST"),
                Endpoint.of("twitter/api/lists/members/create_all", "POST"),
                Endpoint.of("twitter/api/lists/members/destroy", "POST"),
                Endpoint.of("twitter/api/lists/members/destroy_all", "POST"),
                Endpoint.of("twitter/api/lists/subscribers/create", "POST"),
                Endpoint.of("twitter/api/lists/subscribers/destroy", "POST"),
                Endpoint.of("twitter/api/lists/update", "POST"),
                Endpoint.of("twitter/api/oauth/authenticate", "GET"),
                Endpoint.of("twitter/api/oauth/authorize", "GET"),
                Endpoint.of("twitter/api/oauth/access_token", "POST"),
                Endpoint.of("twitter/api/oauth/request_token", "POST"),
                Endpoint.of("twitter/api/oauth2/invalidate_token", "POST"),
                Endpoint.of("twitter/api/oauth2/token", "POST"),
                Endpoint.of("twitter/api/saved_searches/list", "GET"),
                Endpoint.of("twitter/api/saved_searches/show/{id}", "GET"),
                Endpoint.of("twitter/api/saved_searches/create", "POST"),
                Endpoint.of("twitter/api/saved_searches/destroy/{id}", "POST"),
                Endpoint.of("twitter/api/search/tweets", "GET"),
                Endpoint.of("twitter/api/statuses/firehose", "GET"),
                Endpoint.of("twitter/api/statuses/home_timeline", "GET"),
                Endpoint.of("twitter/api/statuses/mentions_timeline", "GET"),
                Endpoint.of("twitter/api/statuses/oembed", "GET"),
                Endpoint.of("twitter/api/statuses/retweeters/ids", "GET"),
                Endpoint.of("twitter/api/statuses/retweets/{id}", "GET"),
                Endpoint.of("twitter/api/statuses/retweets_of_me", "GET"),
                Endpoint.of("twitter/api/statuses/sample", "GET"),
                Endpoint.of("twitter/api/statuses/show/{id}", "GET"),
                Endpoint.of("twitter/api/statuses/user_timeline", "GET"),
                Endpoint.of("twitter/api/statuses/destroy/{id}", "POST"),
                Endpoint.of("twitter/api/statuses/filter", "POST"),
                Endpoint.of("twitter/api/statuses/retweet/{id}", "POST"),
                Endpoint.of("twitter/api/statuses/update", "POST"),
                Endpoint.of("twitter/api/statuses/update_with_media", "POST"),
                Endpoint.of("twitter/api/trends/available", "GET"),
                Endpoint.of("twitter/api/trends/closest", "GET"),
                Endpoint.of("twitter/api/trends/place", "GET"),
                Endpoint.of("twitter/api/users/contributees", "GET"),
                Endpoint.of("twitter/api/users/contributors", "GET"),
                Endpoint.of("twitter/api/users/lookup", "GET"),
                Endpoint.of("twitter/api/users/profile_banner", "GET"),
                Endpoint.of("twitter/api/users/search", "GET"),
                Endpoint.of("twitter/api/users/show", "GET"),
                Endpoint.of("twitter/api/users/suggestions", "GET"),
                Endpoint.of("twitter/api/users/suggestions/{slug}", "GET"),
                Endpoint.of("twitter/api/users/suggestions/{slug}/members", "GET"),
                Endpoint.of("twitter/api/users/report_spam", "POST"));

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
    public WebTarget createWebTarget(final WebTarget webTarget) {
        endpointScenario.setEndpoint(endpointsToExecute.get());
        return webTarget.path(endpointScenario.getEndpoint().getPath());
    }

    @Override
    public String getMethod() {
        return endpointScenario.getEndpoint().getMethod();
    }

    private void addRandom(final List<Endpoint> endpoints, final Endpoint endpoint) {
          for (int i = 0; i < Math.random() * 10; i ++) {
              endpoints.add(endpoint);
          }
    }
}
