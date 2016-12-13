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
package org.tomitribe.firedrill.client.scenario;

import org.tomitribe.firedrill.client.TargetResourceBase;
import org.tomitribe.firedrill.util.WeightedRandomResult;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roberto Cortez
 */
@ApplicationScoped
public class MovieScenario extends TargetResourceBase {
    @Inject
    private EndpointScenario endpointScenario;

    private WeightedRandomResult<Endpoint> endpointsToExecute;

    @PostConstruct
    private void init() {
        final List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(Endpoint.of("movie/rest/movies", "GET"));

        endpointsToExecute = new WeightedRandomResult<>(endpoints);
    }

    @Override
    public WebTarget createWebTarget(final WebTarget webTarget) {
        endpointScenario.setEndpoint(endpointsToExecute.get());
        return webTarget.path(endpointScenario.getEndpoint().getPath());
    }

    @Override
    public Response executeRequest(final WebTarget target) {
        return target.request().method(endpointScenario.getEndpoint().getMethod());
    }
}
