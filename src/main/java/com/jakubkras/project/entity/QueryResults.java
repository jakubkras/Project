package com.jakubkras.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "A shortened version of the Movies entity")
public class QueryResults {

    @JsonProperty("imdbID")
    @Schema(description = "ID of the movie in IMDB", example = "tt1234567")
    private String imdbID;

    @JsonProperty("Title")
    @Schema(description = "Title of the movie", example = "Mickey Mouse")
    private String title;

    @JsonProperty("Plot")
    @Schema(description = "Short description of Movie", example = "Rare book dealers Joel and Garda take a summertime jaunt to the seashore where he becomes involved in a beauty pageant as investor and judge - much to her chagrin")
    private String plot;

}



