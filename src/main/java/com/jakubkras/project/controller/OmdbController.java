package com.jakubkras.project.controller;


import com.jakubkras.project.entity.ErrorResponse;
import com.jakubkras.project.entity.Movie;
import com.jakubkras.project.entity.QueryResults;
import com.jakubkras.project.entity.Rating;
import com.jakubkras.project.exception.MovieNotFoundException;
import com.jakubkras.project.service.OmdbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@Tag(   name = "CRUD REST Movie API",
        description = "CRUD REST API to CREATE, READ, UPDATE and DELETE Movies")
@RestController
@RequestMapping(path = "/movies", produces = {MediaType.APPLICATION_JSON_VALUE})
@Validated
public class OmdbController {

    private final OmdbService omdbService;

    public OmdbController(OmdbService omdbService) {
        this.omdbService = omdbService;
    }



    @Operation(
            summary = "Search for Movie by title",
            description = "Fetches a Movie by its title. First, the system checks the local database for the movie. If not found, it attempts to fetch the Movie details from the external OMDB API. If the movie is deleted, an exception is thrown."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie details fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Movie.class),
                            examples = @ExampleObject(value = """
                                         {
                                                       "imdbID": "tt3741634",
                                                       "Title": "Tokyo Ghoul",
                                                       "Released": "03 Jul 2014",
                                                       "Category": "Animation, Action, Drama",
                                                       "Plot": "A Tokyo college student is attacked by a ghoul, a superpowered human who feeds on human flesh. He survives, but has become part ghoul and becomes a fugitive on the run.",
                                                       "Ratings": [
                                                           {
                                                               "Source": "Internet Movie Database",
                                                               "Value": "7.7/10"
                                                           }
                                                       ],
                                                       "Awards": "6 wins & 5 nominations total",
                                                       "is_updated": false,
                                                       "is_deleted": false,
                                                       "previous_title": null
                                                   }
                                        
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Movie does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Movie Not Found",
                                            value = """
                         {
                            "message": "Movie: Inception doesn't exist",
                             "status": 404,
                             "timestamp": "2025-02-22T12:00:00",
                              "details": "/search/inception"
                         }
                         """
                                    ),
                                    @ExampleObject(
                                            name = "Conflict - Movie is deleted",
                                            value = """
                                                {
                                                  "message": "Movie: Inception is deleted",
                                                  "status": 404,
                                                  "timestamp": "2025-02-22T12:00:00",
                                                  "details": "/search/inception"
                                                }
                                                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Error fetching movie from OMDB API",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                  "message": "Title: Inception not found",
                  "status": 500,
                  "timestamp": "2025-02-22T12:00:00",
                  "details": "/search/inception"
                }
                """)
                    )
            )
    })
    @GetMapping("/search/{title}")
    public Movie getMovieByTitle(@PathVariable String title) throws MovieNotFoundException {
       return omdbService.getMovieByTitle(title);
    }



    @Operation(
            summary = "Search for Movies by Query",
            description = "Searches for Movies matching the given Query in the local database and the external OMDB API. If a Movie exists in the local database, it is returned without fetching from OMDB."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of found Movies",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = QueryResults.class)),
                            examples = @ExampleObject(value = """
                [
                  {
                    "title": "Inception",
                    "imdbID": "tt1375666",
                    "plot": "A thief with the ability to enter people's dreams is given the chance to have his past crimes forgiven."
                  },
                  {
                    "title": "Interstellar",
                    "imdbID": "tt0816692",
                    "plot": "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival."
                  }
                ]
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Empty Query value",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                  "message": "Endpoint value cannot be empty.",
                  "status": 400,
                  "timestamp": "2025-02-22T12:00:00",
                  "details": "/search"
                }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - No Movies found for the given query",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                {
                  "message": "No movies found for this query: Inception.",
                  "status": 404,
                  "timestamp": "2025-02-22T12:00:00",
                  "details": "/search"
                }
                """)
                    )
            )
    })
    @GetMapping("/search")
            public List<QueryResults> getByQuery(@RequestParam String query) throws MovieNotFoundException {
        return omdbService.searchMovieByQuery(query);
    }


    @Operation(
            summary = "Delete a Movie by IMDB ID",
            description = "Marks a Movie as deleted in the database. If the Movie is not found in the database, it tries to fetch it from an external OMDB API and marks it as deleted there. If the Movie is already deleted, an exception is thrown."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie successfully deleted",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Movie with imdbID: tt1375666 was successfully deleted.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid IMDB ID format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "Invalid IMDB ID format. ImdbId should start with 'tt' and contain 7 digits or 'ttt' with 6 digits.",
                                  "status": 400,
                                  "timestamp": "2025-02-22T12:00:00",
                                  "details": "/"
                                }
                                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Movie does not exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Movie Not Found",
                                            value = """
                                                {
                                                  "message": "Movie with imdbID: tt1375666 doesn't exist.",
                                                  "status": 404,
                                                  "timestamp": "2025-02-22T12:00:00",
                                                  "details": "/"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "Conflict - Movie is already deleted",
                                            value = """
                                {
                                  "message": "Movie with imdbID: tt1375666 has already been deleted.",
                                  "status": 404,
                                  "timestamp": "2025-02-22T12:00:00",
                                  "details": "/"
                                }
                                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Error fetching movie from OMDb API",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                {
                                  "message": "Error fetching movie from external API: Network timeout",
                                  "status": 500,
                                  "timestamp": "2025-02-22T12:00:00",
                                  "details": "/"
                                }
                                """)
                    )
            )
    })
    @DeleteMapping("/")
    public ResponseEntity<String> deleteMovieByImdbID (@RequestParam @Pattern(regexp = "^(tt\\d{7}|ttt\\d{6})$", message =  "Invalid IMDb ID format. ImdbID should start with 'tt' and after that there should be 7 digits or 'ttt' and 6 digits") String imdbID) throws MovieNotFoundException {
        omdbService.deleteMovieByImdbID(imdbID);
        return ResponseEntity.ok("Movie with imdbID: " +imdbID + " was successfully deleted");
    }



    @Operation(
            summary = "Update a Movie by IMDB ID",
            description = """
                Updates an existing Movie in the database using its IMDB ID.
                If the Movie is not found in the database, the method attempts to retrieve it from the OMDB API.
                The Movie must not be deleted to be updated.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie successfully updated",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Movie with imdbID: 'tt1375666' was successfully updated."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid IMDB ID format or missing required fields",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Invalid IMDB ID format. ImdbID should start with 'tt' and be followed by 7 digits or 'ttt' followed by 6 digits.",
                                          "status": 400,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/updateMovie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Movie does not exist or is marked as deleted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Movie with this imdbID: 'tt1375666' is not found",
                                          "status": 404,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/updateMovie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - No changes detected or Movie title already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "No changes detected for movie with imdbID: 'tt1375666'",
                                          "status": 409,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/updateMovie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "An unexpected error occurred.",
                                          "status": 500,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/updateMovie"
                                        }
                                        """
                            )
                    )
            )
    })
        @PutMapping("/")
    public ResponseEntity<String> updateMovieByImdbID(@RequestParam @Pattern(regexp = "^(tt\\d{7}|ttt\\d{6})$", message = "Invalid IMDB ID format. ImdbID should start with 'tt' and after that there should be 7 digits or 'ttt' and 6 digits") String imdbID,@Valid @RequestBody Movie movie) throws MovieNotFoundException {
            omdbService.updateMovieByImdbID(imdbID, movie);
        return ResponseEntity.ok("Movie with imdbID: " + imdbID + " was successfully updated.");
        }




    @Operation(
            summary = "Create a new Movie",
            description = "Creates a new Movie and stores it in the database. " +
                    "The method checks if the Movie already exists in the database or if it has been deleted. " +
                    "If a movie is found in an external API (OMDB) and is not present in the database, it will be saved."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Movie successfully created",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Movie 'Inception' has been successfully created."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - The movie already exists or invalid data provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                            value = """
                                                {
                                                  "message": "This Movie: 'Inception' already exists in the database.",
                                                  "status": 400,
                                                  "timestamp": "2025-02-22T12:00:00",
                                                  "details": "/movie"
                                                }
                                                """
                                    )

                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The Movie is marked as deleted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "This Movie: 'Inception' has been deleted.",
                                          "status": 404,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/movie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "An unexpected error occurred.",
                                          "status": 500,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/movie"
                                        }
                                        """
                            )
                    )
            )
    })

    @PostMapping("/movie")
    public ResponseEntity<String> createMovie (@Valid @RequestBody Movie newMovie) throws MovieNotFoundException {
        omdbService.createMovie(newMovie);
        return ResponseEntity.ok("Movie " + newMovie.getTitle() + " has been successfully created");
        }


    @Operation(
            summary = "Add ratings to a Movie",
            description = """
                Adds a list of Ratings to a movie stored in the database.
                If the Movie does not exist in the database, it will be fetched from an external API (OMDB) and stored.
                If the Movie is marked as deleted, the operation will fail.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ratings successfully added",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "Rating has been successfully added to movie: Inception"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Empty title or invalid data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Endpoint value cannot be empty",
                                          "status": 400,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/movie/rating"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Movie does not exist or is deleted",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Movie Not Found",
                                            value = """
                                                {
                                                  "message": "Movie with this title: 'Inception' doesn't exist",
                                                  "status": 404,
                                                  "timestamp": "2025-02-22T12:00:00",
                                                  "details": "/movie/rating"
                                                }
                                                """
                                    ),
                                    @ExampleObject(
                                            name = "Movie Deleted",
                                            value = """
                                                {
                                                  "message": "This movie: 'Inception' is deleted",
                                                  "status": 404,
                                                  "timestamp": "2025-02-22T12:00:00",
                                                  "details": "/movie/rating"
                                                }
                                                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "An unexpected error occurred.",
                                          "status": 500,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/movie/rating"
                                        }
                                        """
                            )
                    )
            )
    })
        @PostMapping("/rating")
    public ResponseEntity<String> addRating (@RequestParam String title,@RequestBody List <Rating> ratings) throws MovieNotFoundException {
        omdbService.addRating(title, ratings);
        return ResponseEntity.ok("Rating has been successfully added to movie: " + title );
        }




    @Operation(
            summary = "Search for Movies by Category",
            description = """
                Searches for Movies based on a given query and category.
                The search is performed first in the database, and if no results are found, an external API (OMDB) is queried.
                If no Movies match the criteria, a `MovieNotFoundException` is thrown.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movies successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                         {
                                                       "imdbID": "tt3741634",
                                                       "Title": "Tokyo Ghoul",
                                                       "Released": "03 Jul 2014",
                                                       "Category": "Animation, Action, Drama",
                                                       "Plot": "A Tokyo college student is attacked by a ghoul, a superpowered human who feeds on human flesh. He survives, but has become part ghoul and becomes a fugitive on the run.",
                                                       "Ratings": [
                                                           {
                                                               "Source": "Internet Movie Database",
                                                               "Value": "7.7/10"
                                                           }
                                                       ],
                                                       "Awards": "6 wins & 5 nominations total",
                                                       "is_updated": false,
                                                       "is_deleted": false,
                                                       "previous_title": null
                                                   }
                                        
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Query or Category is empty",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Endpoint values cannot be empty",
                                          "status": 400,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/searchByCategory"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - No movies match the criteria",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Movies not found",
                                          "status": 404,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/searchByCategory"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "An unexpected error occurred.",
                                          "status": 500,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/searchByCategory"
                                        }
                                        """
                            )
                    )
            )
    })
        @GetMapping("/searchByCategory")
    public List<Movie> getMovieByCategory (@RequestParam String query, @RequestParam String category) throws MovieNotFoundException {
       return omdbService.searchMovieByCategory(query, category);
        }



    @Operation(
            summary = "Enable a deleted Movie",
            description = """
                Restores a previously deleted Movie by setting its `deleted` flag to `false`.
                If the Movie is already enabled or does not exist, an error is returned.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movie successfully enabled",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Movie: 'Inception' is enabled again."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Title parameter is empty",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Endpoint value cannot be empty",
                                          "status": 400,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/enableMovie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Movie does not exist or is already enabled",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "Movie: 'Inception' doesn't exist",
                                          "status": 404,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/enableMovie"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "message": "An unexpected error occurred.",
                                          "status": 500,
                                          "timestamp": "2025-02-22T12:00:00",
                                          "details": "/enableMovie"
                                        }
                                        """
                            )
                    )
            )
    })
        @PatchMapping("/")
    public ResponseEntity<String> enableMovie (@RequestParam String title) throws MovieNotFoundException {
       omdbService.enableMovie(title);
        return ResponseEntity.ok("Movie: "+ title+ " is enable again");
        }


}
