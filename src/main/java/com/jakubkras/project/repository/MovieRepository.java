package com.jakubkras.project.repository;

import com.jakubkras.project.entity.Movie;
import com.jakubkras.project.entity.QueryResults;
import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByTitleContainingIgnoreCaseAndIsDeletedFalse(String title);

    Optional<Movie> findByTitleIgnoreCaseAndIsDeletedTrue(String title);

    Optional<Movie> findByTitleContainingIgnoreCaseAndIsDeletedTrue(String title);

    Optional<Movie> findByTitleIgnoreCaseAndIsDeletedFalse (String title);

    Optional<Movie> findByImdbIDAndIsDeletedFalse(String imdbID);

    Optional<Movie> findByImdbIDAndIsDeletedTrue(String imdbID);

    boolean existsByTitleIgnoreCase (String title);

    boolean existsByImdbIDAndIsDeletedFalse(String imdbId);

}
