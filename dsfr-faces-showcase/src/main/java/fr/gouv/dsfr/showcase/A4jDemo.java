package fr.gouv.dsfr.showcase;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/** Backing bean of richfaces/a4j.xhtml (a4j: P2 tags). View scoped: the poll and the counters span requests. */
@Named
@ViewScoped
public class A4jDemo implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_TICKS = 3;

    private String lastAction = "aucune";
    private boolean details;
    private String city;
    private String other;
    private String cityResult;
    private String picked;
    private Integer population;
    private Integer answer;
    private String source;
    private String loaded;
    private boolean polling;
    private int ticks;
    private String department;
    private String size;
    private String listened;

    // ------------------------------------------------------------------ outputPanel, status

    public void toggleDetails() {
        details = !details;
        lastAction = "détails";
    }

    public void slow() throws InterruptedException {
        Thread.sleep(800);
        lastAction = "traitement long";
    }

    public void waitShort() throws InterruptedException {
        Thread.sleep(800);
        lastAction = "attente";
    }

    // ------------------------------------------------------------------ region

    public void checkCity() {
        cityResult = "Commune vérifiée : " + city;
        lastAction = "région";
    }

    // ------------------------------------------------------------------ param, actionListener

    public void pick() {
        source = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("origin");
        lastAction = "choix";
    }

    public String getPickedText() {
        if (picked == null) {
            return "";
        }
        return picked + (population == null ? "" : " (" + population + " hab.)")
                + (answer == null ? "" : " ; réponse : " + answer)
                + (source == null ? "" : " ; origine : " + source);
    }

    public void onAction(ActionEvent event) {
        listened = "Écouteur : " + event.getComponent().getId();
    }

    public void afterListener() {
        listened = listened + " puis action";
    }

    // ------------------------------------------------------------------ jsFunction

    public void load() {
        lastAction = "jsFunction";
    }

    // ------------------------------------------------------------------ poll

    public void startPolling() {
        ticks = 0;
        polling = true;
    }

    public void tick() {
        ticks++;
        if (ticks >= MAX_TICKS) {
            polling = false;
        }
    }

    // ------------------------------------------------------------------ getters / setters

    public String getLastAction() {
        return lastAction;
    }

    public boolean isDetails() {
        return details;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getCityResult() {
        return cityResult;
    }

    public String getPicked() {
        return picked;
    }

    public void setPicked(String picked) {
        this.picked = picked;
    }

    public Integer getPopulation() {
        return population;
    }

    public void setPopulation(Integer population) {
        this.population = population;
    }

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public String getLoaded() {
        return loaded;
    }

    public void setLoaded(String loaded) {
        this.loaded = loaded;
    }

    public boolean isPolling() {
        return polling;
    }

    public int getTicks() {
        return ticks;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getListened() {
        return listened;
    }
}
