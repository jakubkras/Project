package com.jakubkras.project.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OmdbResponse {

    @JsonProperty("Search")
    private List<QueryResults> search;

}
