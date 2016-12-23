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
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Roberto Cortez
 */
@Data
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Movie {
    @XmlElement
    private Integer id;
    @XmlElement
    private String director;
    @XmlElement
    private String title;
    @XmlElement
    private int year;
    @XmlElement
    private String genre;
    @XmlElement
    private int rating;

    public static MovieWrapper generateMovie() {
        final String fullName = Fabricator.contact().fullName(false, false);
        final String title = Fabricator.words().word();
        final int year = (int) (Math.random() * 30) + 1986;
        final String genre = Fabricator.words().word();
        final int rating = (int) (Math.random() * 10);
        return new Movie.MovieWrapper(new Movie(null, fullName, title, year, genre, rating));
    }

    @Data
    @AllArgsConstructor
    @XmlRootElement(name = "movie")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MovieWrapper {
        @XmlElement
        private Movie movie;

        @Override
        public String toString() {
            return movie.toString();
        }
    }
}
