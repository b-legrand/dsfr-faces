package fr.gouv.dsfr.showcase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.List;

/** Fixed demo data: 23 communes, in a stable (unsorted) order. */
@Named
@ApplicationScoped
public class Communes {

    private static final List<Commune> ALL = List.of(
            new Commune("Nantes", "Loire-Atlantique", 323204),
            new Commune("Albi", "Tarn", 49236),
            new Commune("Brest", "Finistère", 139926),
            new Commune("Lyon", "Rhône", 522250),
            new Commune("Quimper", "Finistère", 63283),
            new Commune("Toulouse", "Haute-Garonne", 504078),
            new Commune("Angers", "Maine-et-Loire", 157175),
            new Commune("Rennes", "Ille-et-Vilaine", 225081),
            new Commune("Saint-Nazaire", "Loire-Atlantique", 73111),
            new Commune("Castres", "Tarn", 42079),
            new Commune("Villeurbanne", "Rhône", 156928),
            new Commune("Cholet", "Maine-et-Loire", 54076),
            new Commune("Saint-Malo", "Ille-et-Vilaine", 47255),
            new Commune("Colomiers", "Haute-Garonne", 40330),
            new Commune("Morlaix", "Finistère", 15220),
            new Commune("Vénissieux", "Rhône", 67129),
            new Commune("Saumur", "Maine-et-Loire", 25937),
            new Commune("Rezé", "Loire-Atlantique", 43060),
            new Commune("Fougères", "Ille-et-Vilaine", 20661),
            new Commune("Muret", "Haute-Garonne", 26117),
            new Commune("Gaillac", "Tarn", 15897),
            new Commune("Écully", "Rhône", 18451),
            new Commune("Vitré", "Ille-et-Vilaine", 18924));

    private static final List<String> DEPARTMENTS = ALL.stream().map(Commune::getDepartment)
            .distinct().sorted().toList();

    public List<Commune> getAll() {
        return ALL;
    }

    public List<String> getDepartments() {
        return DEPARTMENTS;
    }
}
