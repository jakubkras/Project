package com.jakubkras.project.controller;


import com.jakubkras.project.entity.Movie;
import com.jakubkras.project.entity.QueryResults;
import com.jakubkras.project.entity.Rating;
import com.jakubkras.project.exception.MovieNotFoundException;
import com.jakubkras.project.service.OmdbService;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@Validated
public class OmdbController {

    private final OmdbService omdbService;

    public OmdbController(OmdbService omdbService) {
        this.omdbService = omdbService;
    }

    @GetMapping("/search/{title}")
    public Movie getMovieByTitle(@PathVariable String title) throws MovieNotFoundException {
       return omdbService.getMovieByTitle(title);
    }

    @GetMapping("/search")
            public List<QueryResults> getByQuery(@RequestParam String query) throws MovieNotFoundException {
        return omdbService.searchMovieByQuery(query);
    }

    @DeleteMapping("/")
    public ResponseEntity<String> deleteMovieByImdbID (@RequestParam @Pattern(regexp = "^(tt\\d{7}|ttt\\d{6})$", message =  "Invalid IMDb ID format. ImdbID should start with 'tt' and after that there should be 7 digits or 'ttt' and 6 digits") String imdbID) throws MovieNotFoundException {
        omdbService.deleteMovieByImdbID(imdbID);
        return ResponseEntity.ok("Movie with imdbID: " +imdbID + " was successfully deleted");
    }

        @PutMapping("/")
    public ResponseEntity<String> updateMovieByImdbID(@RequestParam @Pattern(regexp = "^(tt\\d{7}|ttt\\d{6})$", message = "Invalid IMDb ID format. ImdbID should start with 'tt' and after that there should be 7 digits or 'ttt' and 6 digits") String imdbID, @RequestBody Movie movie) throws MovieNotFoundException {
            omdbService.updateMovieByImdbID(imdbID, movie);
        return ResponseEntity.ok("Movie with imdbID: " + imdbID + " was successfully updated.");
        }

        @PostMapping("/movie")
    public ResponseEntity<String> createMovie (@RequestBody Movie newMovie){
        omdbService.createMovie(newMovie);
        return ResponseEntity.ok("Movie " + newMovie.getTitle() + " has been successfully created");
        }

        @PostMapping("/rating")
    public ResponseEntity<String> addRating (@RequestParam String title,@RequestBody List <Rating> ratings) throws MovieNotFoundException {
        omdbService.addRating(title, ratings);
        return ResponseEntity.ok("Rating has been successfully added to movie: " + title );
        }

        @GetMapping("/searchByCategory")
    public List<Movie> getMovieByCategory (@RequestParam String query, @RequestParam String category) throws MovieNotFoundException {
       return omdbService.searchMovieByCategory(query, category);
        }

        @PatchMapping("/")
    public ResponseEntity<String> enableMovie (@RequestParam String title) throws MovieNotFoundException {
       omdbService.enableMovie(title);
        return ResponseEntity.ok("Movie: "+ title+ " is enable again");
        }


}
