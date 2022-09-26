package fr.openent.viescolaire.model.Person;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;

public class Student extends Person implements IModel<Student> {

    private String classId;
    private String className;
    private String audienceId;
    private String audienceName;

    public Student(JsonObject student) {
        super();
        this.id = student.getString("id", null);
        this.displayName = student.getString("displayName", null);
        this.firstName = student.getString("firstName", null);
        this.lastName = student.getString("lastName", null);
        this.classId = student.getString("classId", null);
        this.className = student.getString("className", null);
        this.audienceId = student.getString("audienceId", null);
        this.audienceName = student.getString("audienceName", null);
    }

    public Student(String studentId) {
        super();
        this.id = studentId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAudienceId() {
        return audienceId;
    }

    public void setAudienceId(String audienceId) {
        this.audienceId = audienceId;
    }

    public String getAudienceName() {
        return audienceName;
    }

    public void setAudienceName(String audienceName) {
        this.audienceName = audienceName;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(Field.ID, this.id)
                .put(Field.DISPLAYNAME, this.displayName)
                .put(Field.FIRSTNAME, this.firstName)
                .put(Field.LASTNAME, this.lastName)
                .put(Field.CLASSID, this.classId)
                .put(Field.CLASSNAME, this.className)
                .put(Field.AUDIENCEID, this.audienceId)
                .put(Field.AUDIENCENAME, this.audienceName);
    }

    @Override
    public boolean validate() {
        return false;
    }
}
