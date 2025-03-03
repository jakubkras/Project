package com.jakubkras.project.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Schema(description = "Schema to hold information about Ratings")
public class Rating {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ratingId;


    @JsonProperty("Source")
    @Schema(description = "Source of the Movie's rating", example = "Internet Movie Database")
    private String source;

    @JsonProperty("Value")
    @Schema(description = "Value of the Movie's rating", example = "8.6/10")
    private String value;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    
}
