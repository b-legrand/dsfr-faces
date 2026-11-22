package fr.gouv.dsfr.showcase;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/** Backing bean of the migrated RichFaces page (richfaces/inscription.xhtml). */
@Named
@RequestScoped
public class Signup {

    private String login;
    private String email;
    private String profile;
    private boolean registered;

    public void register() {
        registered = true;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Inscription enregistrée", "Un courriel de confirmation a été envoyé à " + email + "."));
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean isRegistered() {
        return registered;
    }
}
