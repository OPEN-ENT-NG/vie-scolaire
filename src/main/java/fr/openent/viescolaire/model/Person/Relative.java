package fr.openent.viescolaire.model.Person;

import fr.openent.viescolaire.helper.RelativeHelper;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class Relative extends Person implements Cloneable {

    private String externalId;
    private String name;
    private String title;
    private List<String> mobile;
    private List<String> phone;
    private String address;
    private String email;
    private Boolean activated;
    private Boolean primary;

    public Relative(JsonObject relative) {
        super();
        this.id = relative.getString("id", null);
        this.externalId = relative.getString("externalId", null);
        this.name = relative.getString("name", null);
        this.title = relative.getString("title", null);

        // Phone numbers retrieved are sometimes typed String. If not typed JsonArray, the string is added to a new array.
        try {
            this.mobile = RelativeHelper.toPhoneList(relative.getJsonArray("mobile", null));
        } catch (ClassCastException e) {
            this.mobile = new ArrayList<>();
            this.mobile.add(relative.getString("mobile", ""));
        }

        try {
            this.phone = RelativeHelper.toPhoneList(relative.getJsonArray("phone", null));
        } catch (ClassCastException e) {
            this.phone = new ArrayList<>();
            this.phone.add(relative.getString("phone", ""));
        }

        this.address = relative.getString("address", null);
        this.email = relative.getString("email", null);
        this.activated = relative.getBoolean("activated", null);
        this.primary = relative.getBoolean("primary", null);
    }

    public Relative(String relativeId) {
        super();
        this.id = relativeId;
    }

    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id", this.id)
                .put("name", this.name)
                .put("title", this.title)
                .put("mobile", this.getMobile())
                .put("phone", this.getPhone())
                .put("address", this.address)
                .put("email", this.email)
                .put("activated", this.activated)
                .put("primary", this.primary);
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFullName() {
        return name;
    }

    public void setFullName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMobile() {
        return mobile.get(0);
    }

    public void setMobile(List<String> mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone.get(0);
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMail() {
        return email;
    }

    public void setMail(String email) {
        this.email = email;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }
}
