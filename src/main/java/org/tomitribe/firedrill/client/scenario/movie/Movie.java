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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Roberto Cortez
 */
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

    public Movie() {
    }

    public Movie(final String director, final String title, final int year, final String genre, final int rating) {
        this.director = director;
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.rating = rating;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(final String director) {
        this.director = director;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(final int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Movie{");
        sb.append("director='").append(director).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", year=").append(year);
        sb.append(", genre='").append(genre).append('\'');
        sb.append(", rating=").append(rating);
        sb.append('}');
        return sb.toString();
    }

    @XmlRootElement(name = "movie")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class MovieWrapper {
        @XmlElement
        private Movie movie;

        public MovieWrapper() {
        }

        public MovieWrapper(final Movie movie) {
            this.movie = movie;
        }

        public Movie getMovie() {
            return movie;
        }

        public void setMovie(final Movie movie) {
            this.movie = movie;
        }

        @Override
        public String toString() {
            return movie.toString();
        }
    }
}
