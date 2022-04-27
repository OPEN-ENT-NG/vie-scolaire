package fr.openent.viescolaire.core.enums;

import java.util.Arrays;

public enum ServicesFieldEnum {
    ID_ETABLISSEMENT("id_etablissement"),
    ID_ENSEIGNANT("id_enseignant"),
    ID_MATIERE("id_matiere"),
    ID_GROUPE("id_groupe"),
    MODALITE("modalite"),
    EVALUABLE("evaluable"),
    ORDRE("ordre"),
    COEFFICIENT("coefficient"),
    IS_VISIBLE("is_visible");

    private final String value;

    ServicesFieldEnum(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static String getServicesField(String key) {
        ServicesFieldEnum servicesFieldValue =  Arrays.stream(ServicesFieldEnum.values())
                .filter(servicesFieldEnum -> servicesFieldEnum.value().equals(key))
                .findFirst()
                .orElse(null);
        return servicesFieldValue == null ? null : servicesFieldValue.value();
    }
}
