package fr.gouv.dsfr.showcase;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Backing bean of composants.xhtml (table, modal, tabs) and richfaces/catalogue.xhtml. */
@Named
@RequestScoped
public class Catalogue {

    private Commune selected;
    private Date since;
    private String department;
    private String searchResult;
    private int clicks;
    private String nickname;

    public void select(Commune commune) {
        this.selected = commune;
    }

    public void search() {
        String date = since == null ? "toujours" : new SimpleDateFormat("dd/MM/yyyy").format(since);
        searchResult = "Recherche : " + (department == null ? "tous les départements" : department) + " depuis le " + date + ".";
    }

    public void count() {
        clicks++;
    }

    public Commune getSelected() {
        return selected;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSearchResult() {
        return searchResult;
    }

    public int getClicks() {
        return clicks;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
