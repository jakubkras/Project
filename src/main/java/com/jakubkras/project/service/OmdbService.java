package com.jakubkras.project.service;

import com.jakubkras.project.entity.Movie;
import com.jakubkras.project.entity.OmdbResponse;
import com.jakubkras.project.entity.QueryResults;
import com.jakubkras.project.entity.Rating;
import com.jakubkras.project.exception.MovieAlreadyExistsException;
import com.jakubkras.project.exception.MovieNotFoundException;
import com.jakubkras.project.exception.NoChangesException;
import com.jakubkras.project.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OmdbService {


    private RestTemplate restTemplate;
    private final MovieRepository movieRepository;
    private final WebClient webClient;

    @Value("${omdb.api.key}")
    private String apiKey;

    @Value("${omdb.api.url}")
    private String apiUrl;


    @Autowired
    public OmdbService(RestTemplateBuilder restTemplateBuilder, WebClient.Builder webClientBuilder, MovieRepository movieRepository, @Value("${omdb.api.url}") String apiUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.movieRepository = movieRepository;
    }

    public Movie getMovieByTitle(String title) throws MovieNotFoundException {

        List<Movie> matchingMovies = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title);

        Optional<Movie> deletedMovie = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedTrue(title);

        if (deletedMovie.isPresent()) {
            throw new MovieNotFoundException("Movie: " + title + " is deleted");
        }

        if (!matchingMovies.isEmpty()) {
            return matchingMovies.get(0);
        }

        Movie omdbMovie = fetchFromOmdb(title);

        if (omdbMovie != null && matchingMovies.isEmpty()) {
            return omdbMovie;
        }

        throw new MovieNotFoundException("Movie: " + title + " doesn't exist");
    }

    private Movie fetchFromOmdb(String title) {
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());

            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("t", encodedTitle)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            Movie movie = restTemplate.getForObject(url, Movie.class);

            return (movie != null && !movie.getTitle().isEmpty() && movie.getTitle() != null ? movie : null);
        } catch (Exception e) {
            throw new RuntimeException("Title: " + title + " not found", e);
        }

    }


    //If I search using query and a given movie title is in the database, the movie title from the database will be displayed and the one from omdb will not appear


    public List<QueryResults> searchMovieByQuery(String query) throws MovieNotFoundException {
        List<QueryResults> finalResults = new ArrayList<>();

        List<Movie> dbMovies = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(query);

        for (Movie movie : dbMovies) {
            QueryResults queryResults = new QueryResults();
            queryResults.setTitle(movie.getTitle());
            queryResults.setImdbID(movie.getImdbID());
            queryResults.setPlot(movie.getPlot());
            finalResults.add(queryResults);
        }
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("s", query)
                .queryParam("apikey", apiKey)
                .toUriString();

        OmdbResponse omdbResponse = restTemplate.getForObject(url, OmdbResponse.class);

        if (omdbResponse != null && omdbResponse.getSearch() != null) {
            for (QueryResults omdbMovie : omdbResponse.getSearch()) {

                boolean isNotInDb = dbMovies.stream().noneMatch(dbmov -> dbmov.getTitle().equalsIgnoreCase(omdbMovie.getTitle()));
                boolean isNotDeleted = movieRepository.findByTitleIgnoreCaseAndIsDeletedTrue(omdbMovie.getTitle()).isEmpty();

                if (isNotDeleted && isNotInDb) {
                    QueryResults detailedMovie = fetchMovieDetails(omdbMovie);
                    finalResults.add(detailedMovie);
                }
            }
        }
        if (finalResults.isEmpty()) {
            throw new MovieNotFoundException("No movies for this query: " + query);
        }
        return finalResults;
    }

    public QueryResults fetchMovieDetails(QueryResults queryResults) {

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("i", queryResults.getImdbID())
                .queryParam("apikey", apiKey)
                .queryParam("plot", "short")
                .toUriString();

        Movie movie = restTemplate.getForObject(url, Movie.class);

        if (movie != null) {
            QueryResults results = new QueryResults();
            results.setImdbID(movie.getImdbID());
            results.setTitle(movie.getTitle());
            results.setPlot(movie.getPlot());
            return results;
        }
        return queryResults;
    }


        @Transactional
    public void deleteMovieByImdbID(String imdbID) throws MovieNotFoundException {

        Optional<Movie> movieInDatabase = movieRepository.findByImdbIDAndIsDeletedFalse(imdbID);

        if (movieInDatabase.isPresent()) {

            Movie movie = movieInDatabase.get();
            movie.setDeleted(true);
            movieRepository.save(movie);
            return;
        }

        if (imdbID.startsWith("ttt")){
            throw new MovieNotFoundException("Movie with imdbID: " + imdbID + " doesn't exist");
        }


        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("i", imdbID)
                .queryParam("apikey", apiKey)
                .toUriString();

        Movie omdbMovie = restTemplate.getForObject(url, Movie.class);

        if (omdbMovie != null && !omdbMovie.getImdbID().isEmpty() && omdbMovie.getImdbID() != null) {

            Movie movie = new Movie();
            movie.setImdbID(omdbMovie.getImdbID());
            movie.setTitle(omdbMovie.getTitle());
            movie.setGenre(omdbMovie.getGenre());
            movie.setPlot(omdbMovie.getPlot());
            movie.setAwards(omdbMovie.getAwards());
            movie.setDeleted(true);
            movie.setUpdated(false);
            movie.setReleaseYear(omdbMovie.getReleaseYear());
            movie.setPreviousTitle(null);

            if (omdbMovie.getRatings() != null) {
                omdbMovie.getRatings().forEach(rating -> rating.setMovie(movie));
                movie.setRatings(omdbMovie.getRatings());
            }

            movieRepository.save(movie);

        } else {
            throw new MovieNotFoundException("Movie with this imdbID: " + imdbID + " doesn't exist");
        }
    }

    @Transactional
    public void updateMovieByImdbID(String imdbID, Movie updatedMovieData) throws MovieNotFoundException {

        Optional<Movie> movieOptional = movieRepository.findByImdbIDAndIsDeletedFalse(imdbID);
        Optional<Movie> deletedMovie = movieRepository.findByImdbIDAndIsDeletedTrue(imdbID);

        Movie omdbMovie = null;

        if(movieOptional.isEmpty() && imdbID.startsWith("tt")) {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("i", imdbID)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            try {
                omdbMovie = restTemplate.getForObject(url, Movie.class);
            } catch (Exception e) {
                throw new MovieNotFoundException("Error retrieving movie data from OMDb API for imdbID: " + imdbID);
            }
        }

        boolean titleExistInDb = movieRepository.existsByTitleIgnoreCase(updatedMovieData.getTitle());
        boolean existingInDb = movieOptional.map(movie -> movie.getTitle().equalsIgnoreCase(updatedMovieData.getTitle())).orElse(false);
        boolean titleExistInOmdb = omdbMovie !=null && omdbMovie.getTitle().equalsIgnoreCase(updatedMovieData.getTitle());


        if (movieOptional.isPresent() && deletedMovie.isEmpty()) {

            Movie movie = movieOptional.get();

            if (isNoChange(movie, updatedMovieData)) {
                throw new NoChangesException("No changes detected for movie with imdbID: " + imdbID);
            }

            if (titleExistInDb || existingInDb || titleExistInOmdb ){
                throw new MovieAlreadyExistsException("Title: "+ updatedMovieData.getTitle() + " already exist");
            }

            if (updatedMovieData.getPreviousTitle() == null)
                movie.setPreviousTitle(movie.getTitle());
            if (updatedMovieData.getTitle() != null)
                movie.setTitle(updatedMovieData.getTitle());
            if (updatedMovieData.getGenre() != null)
                movie.setGenre(updatedMovieData.getGenre());
            if (updatedMovieData.getPlot() != null)
                movie.setPlot(updatedMovieData.getPlot());
            if (updatedMovieData.getAwards() != null)
                movie.setAwards(updatedMovieData.getAwards());
            if (updatedMovieData.getReleaseYear() != null)
                movie.setReleaseYear(updatedMovieData.getReleaseYear());

            if (updatedMovieData.getRatings() != null) {
                for (Rating rating : updatedMovieData.getRatings()) {
                    rating.setMovie(movie);
                }
                movie.getRatings().clear();
                movie.getRatings().addAll(updatedMovieData.getRatings());

            }

            movie.setUpdated(true);
            movieRepository.save(movie);

        } else if (omdbMovie != null && deletedMovie.isEmpty()) {

            Movie movie = new Movie();

            if (isNoChange(movie, updatedMovieData)) {
                throw new NoChangesException("No changes detected for movie with imdbID: " + imdbID);
            }


            if (omdbMovie.getImdbID() != null) {
                movie.setImdbID(omdbMovie.getImdbID());
            } else throw new RuntimeException("Incorrect imdbID to update the movie");

            movie.setUpdated(true);

            movie.setPreviousTitle(omdbMovie.getTitle());

            if (updatedMovieData.getTitle() != null && !updatedMovieData.getTitle().isEmpty())
                movie.setTitle(updatedMovieData.getTitle());
            if (updatedMovieData.getGenre() != null && !updatedMovieData.getGenre().isEmpty())
                movie.setGenre(updatedMovieData.getGenre());
            if (updatedMovieData.getPlot() != null && !updatedMovieData.getPlot().isEmpty())
                movie.setPlot(updatedMovieData.getPlot());
            if (updatedMovieData.getAwards() != null && !updatedMovieData.getAwards().isEmpty())
                movie.setAwards(updatedMovieData.getAwards());
            if (updatedMovieData.getReleaseYear() != null && !updatedMovieData.getReleaseYear().isEmpty())
                movie.setReleaseYear(updatedMovieData.getReleaseYear());

            if (updatedMovieData.getRatings() != null && !updatedMovieData.getRatings().isEmpty()) {
                updatedMovieData.getRatings().forEach(rating -> rating.setMovie(movie));
                movie.setRatings(updatedMovieData.getRatings());
            }
            movieRepository.save(movie);


        } else {
            throw new MovieNotFoundException("Movie with this imdbID: " + imdbID + " is not found");
        }
    }

    public boolean isNoChange(Movie existingMovie, Movie updatedMovieData) {
        return Objects.equals(existingMovie.getTitle(), updatedMovieData.getTitle()) &&
                Objects.equals(existingMovie.getGenre(), updatedMovieData.getGenre()) &&
                Objects.equals(existingMovie.getPlot(), updatedMovieData.getPlot()) &&
                Objects.equals(existingMovie.getAwards(), updatedMovieData.getAwards()) &&
                Objects.equals(existingMovie.getReleaseYear(), updatedMovieData.getReleaseYear()) &&
                areRatingsEquals(existingMovie.getRatings(), updatedMovieData.getRatings());
    }

    public boolean areRatingsEquals(List<Rating> existingRating, List<Rating> updatedRatings) {
        if (existingRating == null && updatedRatings == null) {
            return true;
        }
        if (existingRating == null || updatedRatings == null) {
            return false;
        }
        if (existingRating.size() != updatedRatings.size()) {
            return false;
        }

        for (int i = 0; i < existingRating.size(); i++) {
            Rating r1 = existingRating.get(i);
            Rating r2 = updatedRatings.get(i);

            if (!Objects.equals(r1.getSource(), r2.getSource()) ||
                    !Objects.equals(r1.getValue(), r2.getValue())) {
                return false;
            }
        }
        return true;
    }


    @Transactional
    public void createMovie(Movie newMovie){

        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("t", URLEncoder.encode(newMovie.getTitle(), StandardCharsets.UTF_8))
                .queryParam("apikey", apiKey)
                .toUriString();

        Movie omdbMovie = restTemplate.getForObject(url, Movie.class);

        Optional<Movie> existingTitleInDatabase = movieRepository.findByTitleIgnoreCaseAndIsDeletedFalse(newMovie.getTitle());
        Optional<Movie> deletedMovie = movieRepository.findByTitleIgnoreCaseAndIsDeletedTrue(newMovie.getTitle());


        if (omdbMovie != null && omdbMovie.getTitle() != null
                && omdbMovie.getTitle().equalsIgnoreCase(newMovie.getTitle()) || existingTitleInDatabase.isPresent()) {
            throw new RuntimeException("This movie: " + newMovie.getTitle() + " already exists");
        }


        if (deletedMovie.isPresent()){
                throw new RuntimeException("This movie: " + newMovie.getTitle() +" is deleted");
            }

            Movie movie = new Movie();


            if (newMovie.getTitle() !=null && !newMovie.getTitle().isEmpty())
                movie.setTitle(newMovie.getTitle());

            if (newMovie.getPlot() != null && !newMovie.getPlot().isEmpty())
                movie.setPlot(newMovie.getPlot());

            if (newMovie.getReleaseYear() !=null && !newMovie.getReleaseYear().isEmpty())
                movie.setReleaseYear(newMovie.getReleaseYear());

            if (newMovie.getGenre() !=null && !newMovie.getGenre().isEmpty())
                movie.setGenre(newMovie.getGenre());

            if (newMovie.getAwards() !=null && !newMovie.getAwards().isEmpty())
                movie.setAwards(newMovie.getAwards());

            movie.setImdbID(generateUniqueImdbID());
            movie.setUpdated(false);
            movie.setDeleted(false);
            movie.setPreviousTitle(null);


            if (newMovie.getRatings() != null && newMovie.getRatings().isEmpty()) {
                newMovie.getRatings().forEach(rating -> rating.setMovie(newMovie));
                movie.setRatings(newMovie.getRatings());
            }
            movieRepository.save(movie);

    }
        public String generateUniqueImdbID(){

            String imdbID;
            Random random = new Random();

            do {
                imdbID = "ttt" + String.format("%06d", random.nextInt(1000000));
            } while (movieRepository.existsByImdbIDAndIsDeletedFalse(imdbID));
            return imdbID;
        }

        @Transactional
        public List<Rating> addRating(String title, List <Rating> ratings) throws MovieNotFoundException {

            Optional<Movie> movieOpt = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(title).stream().findFirst();
            Optional<Movie> deletedMovie = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedTrue(title);

            if (deletedMovie.isPresent()) {
                throw new MovieNotFoundException("This movie: " + title + " is deleted");
            }

            if (movieOpt.isPresent()) {
                Movie movie = movieOpt.get();

                if (movie.getTitle().equalsIgnoreCase(title.trim())) {

                    for (Rating rating : ratings) {
                        rating.setMovie(movie);
                        movie.getRatings().add(rating);
                    }
                    movieRepository.save(movie);
                    return movie.getRatings();
                }
            }

            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);

            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("t", encodedTitle)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            Movie omdbMovie = restTemplate.getForObject(url, Movie.class);

            if (omdbMovie.getTitle() != null && movieOpt.isEmpty()) {

                Movie movie = new Movie();

                movie.setImdbID(omdbMovie.getImdbID());
                movie.setTitle(omdbMovie.getTitle());
                movie.setAwards(omdbMovie.getAwards());
                movie.setPlot(omdbMovie.getPlot());
                movie.setGenre(omdbMovie.getGenre());
                movie.setReleaseYear(omdbMovie.getReleaseYear());
                movie.setRatings(omdbMovie.getRatings());
                movie.setUpdated(false);
                movie.setDeleted(false);
                movie.setPreviousTitle(null);

                List<Rating> ratingList = new ArrayList<>(ratings);

                for (Rating rating : ratingList) {
                    rating.setMovie(movie);
                }

                movie.setRatings(ratingList);
                movieRepository.save(movie);

                return ratingList;
            }
            throw new RuntimeException("Movie with this title: " + title + " doesn't exist");
        }

        public List<Movie> searchMovieByCategory (String query, String category) throws MovieNotFoundException {

            List<Movie> movieList = movieRepository.findByTitleContainingIgnoreCaseAndIsDeletedFalse(query);


            List<Movie> moviesIsDb = movieList.stream()
                    .filter(movie -> movie.getGenre()
                            .contains(category)).toList();

            List<Movie> results = new ArrayList<>(moviesIsDb);

            String url = UriComponentsBuilder.fromUri(URI.create(apiUrl))
                    .queryParam("s", query)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            OmdbResponse omdbResponse = restTemplate.getForObject(url, OmdbResponse.class);


            if (omdbResponse != null && omdbResponse.getSearch() != null) {
                List<Movie> filteredMovies = omdbResponse.getSearch().stream().map(movie -> getMovieDetails(movie.getImdbID()))
                        .filter(movie -> movie.getGenre().contains(category))
                        .toList();

                results.addAll(filteredMovies);
            }

            if (results.isEmpty()){

                throw new MovieNotFoundException("Movies not found");
            }

          return results;
        }


        public Movie getMovieDetails(String imdbId){

            String url = UriComponentsBuilder.fromUri(URI.create(apiUrl))
                    .queryParam("i", imdbId)
                    .queryParam("apikey", apiKey)
                    .toUriString();

            return restTemplate.getForObject(url, Movie.class);
        }

        @Transactional
        public void enableMovie (String title) throws MovieNotFoundException {

        Optional<Movie> movieOpt = movieRepository.findByTitleIgnoreCaseAndIsDeletedTrue(title);
        Optional<Movie> enabledMovie = movieRepository.findByTitleIgnoreCaseAndIsDeletedFalse(title);

        if (enabledMovie.isEmpty() && movieOpt.isPresent()){
            Movie movie = movieOpt.get();
            movie.setDeleted(false);
            movieRepository.save(movie);
            return;
        }

        if (enabledMovie.isPresent()){
            throw new MovieNotFoundException("Movie with this title: "+ title + " is enable");
        }

        throw new MovieNotFoundException("Movie: "+ title + " doesn't exist");
        }
}