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
    public static final String CLASS_EXISTS = "classExists";


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

    //Class field
    public static final String SOURCE = "source";
    public static final String EXTERNALID = "externalId";
    public static final String USERS = "users";
    public static final String NBUSERS = "nbUsers";
    public static final String NOTEMPTYGROUP = "notEmptyGroup";
    public static final String STRUCTURENAME = "structureName";
    public static final String DISPLAYNAMESEARCHFIELD = "displayNameSearchField";


    public static final String STATUS = "status";
    public static final String OK = "ok";
    public static final String RESULTS = "results";
    public static final String CLASSGROUPEXTERNALIDS = "classGroupExternalIds";
    public static final String MANUALGROUPNAMES = "manualGroupNames";
    public static final String STARTDATE = "startDate";
    public static final String STARTTIME = "startTime";
    public static final String NAME = "name";
    public static final String MAIN_TEACHER_ID = "main_teacher_id";
    public static final String SECOND_TEACHER_IDS = "second_teacher_ids";
    public static final String SUBJECT_ID = "subject_id";
    public static final String CLASS_OR_GROUP_IDS = "class_or_group_ids";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String ENTERED_END_DATE = "entered_end_date";
    public static final String CO_TEACHING = "co_teaching";
    public static final String IS_VISIBLE = "is_visible";
    public static final String IDS_MULTITEACHINGTOUPDATE = "ids_multiTeachingToUpdate";
    public static final String IDS_MULTITEACHINGTODELETE = "ids_multiTeachingToDelete";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";
    public static final String SERVICES = "services";
    public static final String COMPETENCES = "competences";
    public static final String IDS = "ids";

    //Grouping field
    public static final String GROUPING_ID = "grouping_id";
    public static final String GROUP_ID = "group_id";
    public static final String GROUP_ID_CAMEL = "groupId";
    public static final String STUDENT_DIVISION_ID = "student_division_id";

    //Test field
    public static final String VALUES = "values";
    public static final String STATEMENT = "statement";
    public static final String ACTION = "action";
    public static final String PREPARED = "prepared";

    private Field() {
        throw new IllegalStateException("Utility class");
    }
}
