package fr.openent.viescolaire.core.constants;

public class Field<id_groupes> {
    public static final String ID = "id";

    // structure field
    public static final String STRUCTUREID = "structureId";
    public static final String STRUCTURE_ID = "structure_id";
    public static final String STRUCTURE = "structure";
    public static final String IDSTRUCTURE = "idStructure";
    public static final String SCHOOLID = "schoolId";
    public static final String IDETABLISSEMENT = "idEtablissement";
    public static final String ID_ETABLISSEMENT = "id_etablissement";

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
    public static final String CLASSIDS = "classIds";
    public static final String CLASSES = "classes";
    public static final String GROUPS = "groups";

    public static final String ID_CLASS = "id_class";
    public static final String CLASSID = "classId";
    public static final String CLASS_ID = "class_id";
    public static final String CLASS = "Class";
    public static final String CLASS_SHORT = "c";
    public static final String ID_CLASSE = "id_classe";

    public static final String TIMESLOTID = "timeslotId";
    public static final String TIMESLOTSTRUCTUREID = "timeslotStructureId";
    public static final String TIMESLOT_ID = "timeslot_id";
    public static final String ID_TIME_SLOT = "id_time_slot";

    public static final String ERROR_TIME_SLOT_STRUCTURE = "Error to get structure time slot.";
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
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
    public static final String TEACHERS = "teachers";
    public static final String TEACHERIDS = "teacherIds";
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
    public static final String RESULT = "result";
    public static final String CLASSGROUPEXTERNALIDS = "classGroupExternalIds";
    public static final String MANUALGROUPNAMES = "manualGroupNames";
    public static final String STARTDATE = "startDate";
    public static final String ENDDATE = "endDate";
    public static final String STARTTIME = "startTime";
    public static final String NAME = "name";
    public static final String MAIN_TEACHER_ID = "main_teacher_id";
    public static final String SECOND_TEACHER_IDS = "second_teacher_ids";
    public static final String SUBJECT_ID = "subject_id";
    public static final String SUBJECTID = "subjectId";
    public static final String CLASS_OR_GROUP_IDS = "class_or_group_ids";
    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String ENTERED_END_DATE = "entered_end_date";
    public static final String CO_TEACHING = "co_teaching";
    public static final String IS_VISIBLE = "is_visible";

    public static final String DESCRIPTION = "description";
    public static final String IDS_MULTITEACHINGTOUPDATE = "ids_multiTeachingToUpdate";
    public static final String IDS_MULTITEACHINGTODELETE = "ids_multiTeachingToDelete";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";
    public static final String SERVICES = "services";
    public static final String COMPETENCES = "competences";
    public static final String IDS = "ids";
    public static final String NODE_EXISTS = "node_exists";
    public static final String DATA = "data";
    //Grouping field
    public static final String GROUPING = "grouping";
    public static final String GROUPING_ID = "grouping_id";
    public static final String GROUP_ID = "group_id";
    public static final String ID_GROUP = "id_groupe";
    public static final String GROUP_ID_CAMEL = "groupId";
    public static final String ID_GROUPES = "id_groupes";
    public static final String STUDENT_DIVISION_ID = "student_division_id";
    public static final String STUDENT_DIVISION_NAME = "student_division_name";
    public static final String STUDENT_DIVISIONS = "student_divisions";

    //Test field
    public static final String VALUES = "values";
    public static final String STATEMENT = "statement";
    public static final String ACTION = "action";
    public static final String PREPARED = "prepared";
    public static final String IDSAUDIENCE = "idsAudience";
    public static final String IDGROUP = "idGroupe";

    public static final String ACTIVE = "active";
    public static final String PICTURE_ID = "picture_id";
    public static final String PICTUREID = "pictureId";
    public static final String ERRORREPORT = "errorReport";
    public static final String MESSAGE = "message";
    public static final String ALL = "all";
    public static final String FAILUREID = "failureId";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String CLASSNAME = "className";
    public static final String AUDIENCENAME = "audienceName";
    public static final String ID_STRUCTURE = "id_structure";
    public static final String TIMESLOT = "timeslot";
    public static final String SLOTS = "slots";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";
    public static final String STARTHOUR = "startHour";
    public static final String ENDHOUR = "endHour";
    public static final String STUDENT_ID_LIST = "student_id_list";
    public static final String PERIOD_ID_CAMEL = "periodId";
    public static final String GROUPIDS = "groupIds";
    public static final String COUNT = "count";

    public static final String INITIALIZED = "initialized";

    public static final String MORNING = "morning";
    public static final String AFTERNOON = "afternoon";
    public static final String FULLDAYS = "fullDays";
    public static final String HALFDAYS = "halfDays";
    public static final String SYSTEM = "system";
    public static final String ZONE = "zone";

    public static final String INITSCHOOLYEAR = "initSchoolYear";
    public static final String INITTYPE = "initType";
    public static final String SCHOOLYEAR = "schoolYear";
    public static final String TIMETABLE = "timetable";
    public static final String TIMESLOTS = "timeslots";
    public static final String HOLIDAYS = "holidays";
    public static final String PARAMS = "params";

    public static final String I18N_PARAMS = "i18nParams";

    public static final String DOMAIN = "domain";
    public static final String ACCEPT_LANGUAGE = "acceptLanguage";
    public static final String OWNER = "owner";
    public static final String OWNERID = "ownerId";
    public static final String OWNERNAME = "ownerName";
    public static final String LANGUAGE = "language";
    public static final String ENDOFHALFDAY = "endOfHalfDay";
    public static final String BODY = "body";

    public static final String LABEL = "label";
    public static final String CODE = "code";
    public static final String SUBJECT = "subject";

    public static final String ID_MATIERE = "id_matiere";

    public static final String ID_ENSEIGNANT = "id_enseignant";

    public static final String COEFFICIENT = "coefficient";

    public static final String MODALITE = "modalite";

    public static final String EVALUABLE = "evaluable";
    private Field() {
        throw new IllegalStateException("Utility class");
    }
}
