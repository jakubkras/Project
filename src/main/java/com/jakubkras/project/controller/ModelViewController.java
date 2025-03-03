package com.jakubkras.project.controller;


import com.jakubkras.project.entity.Movie;
import com.jakubkras.project.entity.QueryResults;
import com.jakubkras.project.entity.Rating;
import com.jakubkras.project.exception.MovieNotFoundException;
import com.jakubkras.project.repository.MovieRepository;
import com.jakubkras.project.service.OmdbService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/movies/home")
@Validated
public class ModelViewController {


    private OmdbService omdbService;

    private MovieRepository movieRepository;

    @Autowired
    public ModelViewController(OmdbService omdbService, MovieRepository movieRepository) {
        this.omdbService = omdbService;
        this.movieRepository = movieRepository;
    }



    @GetMapping
    public ModelAndView homepage(Model model) {
        model.addAttribute("movie", new Movie());
        return new ModelAndView("index");
    }

    @PostMapping("/searchByTitle")
    public String searchMoviesByTitle(@RequestParam("title") String title, Model model, RedirectAttributes redirectAttributes) {
      try {
          Movie movie = omdbService.getMovieByTitle(title);
          model.addAttribute("movie", movie);
          return "searchByTitle";
      }catch (Exception e){
          redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
          return "redirect:/movies/home";
      }
    }

    @PostMapping("/searchByQuery")
    public String searchByQuery(@RequestParam("query") String query, Model model, RedirectAttributes redirectAttributes) {

        try {
           List<QueryResults> results = omdbService.searchMovieByQuery(query);
           model.addAttribute("results", results);
           return "searchByQuery";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";        }
    }

    @PostMapping("/searchByCategory")
    public String searchByCategory(@RequestParam("query") String query,
                                         @RequestParam("category") String category, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Movie> results = omdbService.searchMovieByCategory(query, category);
            model.addAttribute("results", results);
            return "searchByCategory";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }

    @PostMapping("/createMovie")
    public String createMovie(@ModelAttribute("movie") Movie movie, RedirectAttributes redirectAttributes, Model model) {
        try {
            Movie savedMovie = omdbService.createMovieFront(movie);

            model.addAttribute("movie", savedMovie);

            return "createMovie";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }

    @PostMapping("/addRating")
    public String addRating (@RequestParam String title, @ModelAttribute("movie") Movie movie, Model model, RedirectAttributes redirectAttributes){
        try {
            List<Rating> ratings = new ArrayList<>();

            if (movie.getRatings() != null && !movie.getRatings().isEmpty()){
                ratings.add(movie.getRatings().getFirst());
            }

            Movie updatedMovie = omdbService.addRating(title, ratings);
            model.addAttribute("movie", updatedMovie);
            return "addRating";

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }

    @PostMapping("/updateMovie")
    public String updateMovie(@RequestParam String imdbID, @ModelAttribute("movie")  Movie movie, Model model, RedirectAttributes redirectAttributes) {
        try{

           Movie updatedMovie =omdbService.updateMovieByImdbID(imdbID, movie);

            model.addAttribute("movie", updatedMovie);
            return "updateMovie";

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }

    @PostMapping("/deleteMovie")
    public String deleteMovie (@RequestParam String imdbID, Model model, RedirectAttributes redirectAttributes){
        try{
            Movie deletedMovie = omdbService.deleteMovieByImdbID(imdbID);
            model.addAttribute("movie", deletedMovie);
            return "deleteMovie";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }

    @PostMapping("/enableMovie")
    public String enableMovie (@RequestParam String title, Model model, RedirectAttributes redirectAttributes){
        try {
            Movie enabledMovie = omdbService.enableMovie(title);
            model.addAttribute("movie", enabledMovie);
            return "enableMovie";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/home";
        }
    }


}
