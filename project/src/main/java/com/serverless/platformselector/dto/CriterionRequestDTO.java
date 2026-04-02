package com.serverless.platformselector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CriterionRequestDTO {
    /**
     * Το όνομα του κριτηρίου, π.χ. "cost", "memory", "timeout", "runtimes"
     */
    private String name;

    /**
     * Ο τύπος του κριτηρίου:
     * "numeric", "categorical", "categorical_multiple_all"
     */
    private String type;

    /**
     * Για numeric κριτήρια – ελάχιστη τιμή στο dataset
     */
    private Double minValue;

    /**
     * Για numeric κριτήρια – μέγιστη τιμή στο dataset
     */
    private Double maxValue;

    /**
     * Για categorical κριτήρια – λίστα με πιθανές τιμές
     */
    private List<String> categoricalValues;

    /**
     * "positive" ή "negative" – αν υψηλότερες τιμές είναι καλύτερες ή χειρότερες
     */
    private String direction;

    /**
     * Η τιμή/ες που επέλεξε ο χρήστης για το συγκεκριμένο κριτήριο.
     * - Αν είναι numeric ή categorical: τότε περιέχει 1 μόνο τιμή (String)
     * - Αν είναι categorical_multiple_all: μπορεί να περιέχει πολλές τιμές
     */
    private List<String> userValues;

    /**
     * Το βάρος που όρισε ο χρήστης για το κριτήριο (0.0–1.0).
     * Όλα τα βάρη μαζί πρέπει να δίνουν άθροισμα 1.0
     */
    private Double weight;
    private String userValue;
}
