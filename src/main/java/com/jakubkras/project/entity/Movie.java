package com.jakubkras.project.entity;


import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(
        name = "Movie",
        description = "Schema to hold informations about Movies"
)
public class Movie {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "imdb_id", unique = true, updatable = false)
    @JsonProperty("imdbID")
    @Schema(description = "ID of Movie", example = "tt1234567")
    private String imdbID;

    @JsonProperty("Title")
    @NotNull(message = "Field title cannot be empty")
    @Schema(description = "Title of Movie", example = "Mickey Mouse")
    private String title;

    @Column(name = "release_year")
    @JsonProperty("Released")
    @Schema(description = "Release year of Movie", example = "2022")
    private String releaseYear;

    @JsonAlias("Genre")
    @JsonProperty("Category")
    @Schema(description = "Category of Movie", example = "Sci-Fi")
    private String genre;

    @JsonProperty("Plot")
    @Schema(description = "Short description of Movie", example = "Rare book dealers Joel and Garda take a summertime jaunt to the seashore where he becomes involved in a beauty pageant as investor and judge - much to her chagrin")
    private String plot;

    @JsonProperty("Ratings")
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Rating list of Movies")
    private List<Rating> ratings;

    @JsonProperty("Awards")
    @Schema(description = "Awards the Movie received", example = "Won 6 Oscars. 69 wins & 30 nominations total")
    private String awards;


    @JsonProperty("is_updated")
    @Schema(description = "Shows, if the Movie has been updated")
    private boolean isUpdated= false;


    @JsonProperty("is_deleted")
    @Schema(description = "Shows, if the Movie has been deleted")
    private boolean isDeleted= false;


    @JsonProperty("previous_title")
    @Schema(description = "Previous title of Movie")
    private String previousTitle;
}
