package fr.openent.viescolaire.model.InitForm;

public enum ZoneEnum {
    ZONE_A("Zone A"),
    ZONE_B("Zone B"),
    ZONE_C("Zone C"),
    CORSE("Corse"),
    GUADELOUPE("Guadeloupe"),
    GUYANE("Guyane"),
    MARTINIQUE("Martinique"),
    MAYOTTE("Mayotte"),
    NOUVELLE_CALEDONIE("Nouvelle Calédonie"),
    POLYNESIE("Polynésie"),
    REUNION("Réunion"),
    SAINT_PIERRE_ET_MIQUELON("Saint Pierre et Miquelon"),
    WALLIS_ET_FUTUNA("Wallis et Futuna");

    private final String zoneType;

    ZoneEnum(String zoneType) {
        this.zoneType = zoneType;
    }

    public String zone() {
        return this.zoneType;
    }
}
