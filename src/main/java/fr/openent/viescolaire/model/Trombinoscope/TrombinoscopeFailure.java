package fr.openent.viescolaire.model.Trombinoscope;

import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigInteger;


public class TrombinoscopeFailure implements Cloneable, IModel<TrombinoscopeFailure> {

    private BigInteger id;
    private String path;
    private String message;
    private String structure_id;
    private String picture_id;
    private String created_at;

    public TrombinoscopeFailure(JsonObject failure) {
        this.id = BigInteger.valueOf(failure.getInteger("id", null));
        this.path = failure.getString("path", null);
        this.message = failure.getString("message", null);
        this.structure_id = failure.getString("structure_id", null);
        this.picture_id = failure.getString("picture_id", null);
        this.created_at = failure.getString("created_at", null);
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStructureId() {
        return structure_id;
    }

    public void setStructureId(String structure_id) {
        this.structure_id = structure_id;
    }

    public String getPictureId() {
        return picture_id;
    }

    public void setPictureId(String picture_id) {
        this.picture_id = picture_id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("id", this.id)
                .put("path", this.path)
                .put("message", this.message)
                .put("structure_id", this.structure_id)
                .put("picture_id", this.picture_id)
                .put("created_at", this.created_at);
    }

    @Override
    public boolean validate() {
        throw new NotImplementedException();
    }
}

