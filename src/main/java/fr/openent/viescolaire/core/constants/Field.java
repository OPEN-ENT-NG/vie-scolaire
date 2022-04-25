package fr.openent.viescolaire.core.constants;

public class Field {
    public static final String ID = "id";

    // structure field
    public static final String STRUCTUREID = "structureId";
    public static final String STRUCTURE_ID = "structure_id";
    public static final String STRUCTURE = "structure";
    public static final String IDSTRUCTURE = "idStructure";
    public static final String SCHOOLID = "schoolId";

    // student field
    public static final String STUDENTID = "studentId";
    public static final String STUDENT_ID = "student_id";
    public static final String STUDENT = "student";
    public static final String USERID = "userId";

    public static final String AUDIENCEID = "audienceId";
    public static final String AUDIENCEIDS = "audienceIds";
    public static final String AUDIENCE_ID = "audience_id";
    public static final String ID_AUDIENCE = "id_audience";

    public static final String ID_CLASSES = "id_classes";
    public static final String ID_CLASS = "id_class";
    public static final String CLASSID = "classId";
    public static final String CLASS_ID = "class_id";
    public static final String CLASS = "Class";
    public static final String CLASS_SHORT = "c";

    public static final String TIMESLOTID = "timeslotId";
    public static final String TIMESLOTSTRUCTUREID = "timeslotStructureId";
    public static final String TIMESLOT_ID = "timeslot_id";
    public static final String ID_TIME_SLOT = "id_time_slot";

    public static final String ERROR_TIME_SLOT_STRUCTURE = "Error to get structure time slot.";
    public static final String ERROR = "error";
    public static final String TIMESLOT_NOT_FOUND = "Timeslot not found";
    public static final String STRUCTURE_TIMESLOT_NOT_FOUND = "No timeslot found for this structure";
    public static final String CLASS_NOT_FOUND = "Class not found";
    public static final String AUDIENCE_CLASS_NOT_FOUND = "No class found for this audience";
    public static final String USER_NOT_IN_AUDIENCE_STRUCTURE = "User must be in audience structure";
    public static final String USER_NOT_IN_TIMESLOT_STRUCTURE = "User must be in timeslot structure";
    public static final String USER_TIMESLOT_CLASS_NOT_SAME_STRUCTURE = "Class, timeslot and user must be in the same structure";


    // User profiles
    public static final String PROFILE = "profile";
    public static final String PROFILES = "profiles";
    public static final String TEACHER = "Teacher";

    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String DISPLAYNAME = "displayName";

    public static final String METADATA = "metadata";
    public static final String LABELS = "labels";
    public static final String ISRESPONSE = "isResponse";
    public static final String _ID = "_id";
    public static final String TIME = "time";
    public static final String FIELD = "field";
    public static final String FIELDS = "fields";
    public static final String Q = "q";
    public static final String QUERY = "query";

    private Field() {
        throw new IllegalStateException("Utility class");
    }
}
