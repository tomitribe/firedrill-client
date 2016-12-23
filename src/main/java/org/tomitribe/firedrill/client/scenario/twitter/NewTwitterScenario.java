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

import org.tomitribe.firedrill.client.auth.oauth.OAuth;
import org.tomitribe.firedrill.client.scenario.Endpoint;
import org.tomitribe.firedrill.client.scenario.ScenarioInvoker;
import org.tomitribe.firedrill.util.WeightedRandomResult;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.tomitribe.firedrill.client.scenario.Endpoint.endpoint;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
@OAuth
public class NewTwitterScenario extends ScenarioInvoker {
    @Override
    protected List<Endpoint> getEndpoints() {
        return Stream.of(endpoint("twitter/api/account/settings", "GET"),
                         endpoint("twitter/api/account/update_profile_banner", "POST"),
                         endpoint("twitter/api/account/update_profile_image", "POST"),
                         endpoint("twitter/api/application/rate_limit_status", "GET"),
                         endpoint("twitter/api/blocks/ids", "GET"),
                         endpoint("twitter/api/blocks/list", "GET"),
                         endpoint("twitter/api/direct_messages/show", "GET"),
                         endpoint("twitter/api/favorites/destroy", "POST"),
                         endpoint("twitter/api/followers/list", "GET"),
                         endpoint("twitter/api/friends/ids", "GET"),
                         endpoint("twitter/api/friendships/no_retweets/ids", "GET"),
                         endpoint("twitter/api/geo/place", "POST"),
                         endpoint("twitter/api/geo/similar_places", "GET"),
                         endpoint("twitter/api/help/configuration", "GET"),
                         endpoint("twitter/api/lists/list", "GET"),
                         endpoint("twitter/api/lists/members", "GET"),
                         endpoint("twitter/api/lists/members/destroy_all", "POST"),
                         endpoint("twitter/api/lists/members/show", "GET"),
                         endpoint("twitter/api/saved_searches/list", "GET"),
                         endpoint("twitter/api/statuses/firehose", "GET"),
                         endpoint("twitter/api/statuses/home_timeline", "GET"),
                         endpoint("twitter/api/statuses/mentions_timeline", "GET"),
                         endpoint("twitter/api/statuses/retweeters/ids", "GET"),
                         endpoint("twitter/api/statuses/retweets/{id}", "GET"),
                         endpoint("twitter/api/statuses/sample", "GET"),
                         endpoint("twitter/api/statuses/show/{id}", "GET"),
                         endpoint("twitter/api/statuses/update_with_media", "POST"),
                         endpoint("twitter/api/users/contributees", "GET"),
                         endpoint("twitter/api/users/search", "GET"),
                         endpoint("twitter/api/users/suggestions/{slug}/members", "GET"),
                         endpoint("twitter/api/users/report_spam", "POST"))
                     .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    protected WeightedRandomResult<Endpoint> distributeEndpoints() {
        final ArrayList<Endpoint> endpoints =
                getEndpoints().stream()
                              .filter(e -> e.getMethod().equals("GET"))
                              .collect(ArrayList::new, this::addRandom, ArrayList::addAll);

        shuffle(endpoints);
        return new WeightedRandomResult<>(endpoints);
    }

    private void addRandom(final List<Endpoint> endpoints, final Endpoint endpoint) {
        for (int i = 0; i < Math.random() * 10; i ++) {
            endpoints.add(endpoint);
        }
    }
}
