package fr.gouv.dsfr.showcase;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Form of composants.xhtml (DSFR form fields). */
@Named
@RequestScoped
public class Preferences {

    private String comment;
    private String password;
    private LocalDate birthDate;
    private String department;
    private String contact;
    private List<String> topics = new ArrayList<>();
    private boolean cgu;
    private boolean notifications = true;
    private String street;
    private String city;

    public List<String> getTopicItems() {
        return List.of("Culture", "Environnement", "Sport");
    }

    public void save() {
        String detail = "Département : " + department + " ; contact : " + contact + " ; thèmes : "
                + String.join(", ", topics) + " ; naissance : " + birthDate + " ; conditions : " + (cgu ? "oui" : "non")
                + " ; notifications : " + (notifications ? "oui" : "non") + " ; ville : " + city;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Préférences enregistrées", detail));
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public boolean isCgu() {
        return cgu;
    }

    public void setCgu(boolean cgu) {
        this.cgu = cgu;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
