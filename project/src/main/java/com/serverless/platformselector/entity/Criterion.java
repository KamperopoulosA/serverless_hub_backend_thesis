package com.serverless.platformselector.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Criterion {

    private String name;
    private String type; // "numeric", "categorical", "categorical_multiple_all", κτλ.
    private Double minValue;
    private Double maxValue;
    private List<String> categoricalValues; // για κατηγορικά πεδία
    private String direction; // "positive" ή "negative"


}
