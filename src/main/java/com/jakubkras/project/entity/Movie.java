package com.jakubkras.project.entity;


import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Data;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "movie", uniqueConstraints = {
        @UniqueConstraint(columnNames = "imdb_id")
})
public class Movie {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "imdb_id", unique = true, updatable = false)
    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @Column(name = "release_year")
    @JsonProperty("Released")
    private String releaseYear;

    @JsonAlias("Genre")
    @JsonProperty("Category")
    private String genre;

    @JsonProperty("Plot")
    private String plot;

    @JsonProperty("Ratings")
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

    @JsonProperty("Awards")
    private String awards;


    @JsonProperty("is_updated")
    private boolean isUpdated= false;


    @JsonProperty("is_deleted")
    private boolean isDeleted= false;


    @JsonProperty("previous_title")
    private String previousTitle;
}
