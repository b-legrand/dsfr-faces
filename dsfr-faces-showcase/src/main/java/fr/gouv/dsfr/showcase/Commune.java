package fr.gouv.dsfr.showcase;

import java.io.Serializable;

/** Demo data row. */
public class Commune implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String department;
    private final int population;

    public Commune(String name, String department, int population) {
        this.name = name;
        this.department = department;
        this.population = population;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPopulation() {
        return population;
    }
}
