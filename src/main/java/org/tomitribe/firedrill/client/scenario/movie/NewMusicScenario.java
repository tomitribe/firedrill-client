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
package org.tomitribe.firedrill.client.scenario.movie;

import fabricator.Fabricator;
import org.tomitribe.firedrill.client.auth.signature.Signature;
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
import static org.tomitribe.firedrill.client.provider.ClientUtils.getRandomInt;
import static org.tomitribe.firedrill.client.scenario.Endpoint.endpoint;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
@Signature
public class NewMusicScenario extends ScenarioInvoker {
    @Override
    protected List<Endpoint> getEndpoints() {
        return Stream.of(endpoint("music/rest/musics", "GET"),
                         endpoint("music/rest/musics/count", "GET"),
                         endpoint("music/rest/musics", "POST", this::generatePostData),
                         endpoint("music/rest/musics/1", "DELETE"),
                         endpoint("music/rest/musics/1", "GET"))
                     .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Override
    protected WeightedRandomResult<Endpoint> distributeEndpoints() {
        final ArrayList<Endpoint> endpoints =
                getEndpoints().stream()
                              .filter(e -> e.getMethod().equals("GET"))
                              .collect(ArrayList::new, (list, endpoint) -> {
                                  list.add(endpoint);
                                  list.add(endpoint);
                                  list.add(endpoint);
                                  list.add(endpoint);
                              }, ArrayList::addAll);

        shuffle(endpoints);
        return new WeightedRandomResult<>(endpoints);
    }

    private Object generatePostData() {
        int chance = getRandomInt(100);
        if (chance >= 95) {
            return "";
        } else {
            final String fullName = Fabricator.contact().fullName(false, false);
            final String title = Fabricator.words().word();
            final int year = getRandomInt(30) + 1986;
            final String genre = Fabricator.words().word();
            final int rating = getRandomInt(10);
            return new Movie.MovieWrapper(new Movie(fullName, title, year, genre, rating));
        }
    }
}
