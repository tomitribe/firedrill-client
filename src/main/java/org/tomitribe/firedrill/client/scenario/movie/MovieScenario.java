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
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.shuffle;
import static org.tomitribe.firedrill.client.provider.ClientUtils.getRandomInt;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
public class MovieScenario extends TargetResourceBase {
    @Inject
    private ScenarioEndpoint scenarioEndpoint;
    @Inject
    private OAuthMethod oAuthMethod;

    private WeightedRandomResult<Endpoint> endpointsToExecute;

    @PostConstruct
    private void init() {
        final List<Endpoint> allEndpoints = new ArrayList<>();
        allEndpoints.add(Endpoint.endpoint("movie/rest/movies", "GET"));
        allEndpoints.add(Endpoint.endpoint("movie/rest/movies/count", "GET"));
        allEndpoints.add(Endpoint.endpoint("movie/rest/movies", "POST", this::generatePostData));
        allEndpoints.add(Endpoint.endpoint("movie/rest/movies/1", "DELETE"));
        //allEndpoints.add(Endpoint.endpoint("movie/rest/movies/1", "PUT"));
        allEndpoints.add(Endpoint.endpoint("movie/rest/movies/1", "GET"));

        // GET endpoints are called more.
        final ArrayList<Endpoint> getEndpoints =
                allEndpoints.stream()
                            .filter(e -> e.getMethod().equals("GET"))
                            .collect(ArrayList::new, (list, endpoint) -> {
                                list.add(endpoint);
                                list.add(endpoint);
                                list.add(endpoint);
                                list.add(endpoint);
                            }, ArrayList::addAll);

        getEndpoints.addAll(allEndpoints);
        shuffle(getEndpoints);
        getEndpoints.stream().map(Endpoint::getPath).forEach(System.out::println);

        endpointsToExecute = new WeightedRandomResult<>(getEndpoints);
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

    @Override
    public Response executeRequest(final WebTarget target) {
        return target.request().method(getMethod(), scenarioEndpoint.getEndpoint().getEntity());
    }

    private Object generatePostData() {
        int chance = getRandomInt(100);
        if (chance >= 85) {
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
