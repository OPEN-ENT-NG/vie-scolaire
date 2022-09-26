package fr.openent.viescolaire.model;

import io.vertx.core.json.JsonObject;

/**
 * ⚠ Classes implementing this model must have a public constructor with JsonObject parameter
 */
public interface IModel<I extends IModel<I>> {
     /**
      * Convert object to jsonObject
      * @return jsonObject
      */
     JsonObject toJsonObject();

     /**
      * Check that the object is correctly populated
      * @return true if correctly populated
      */
     boolean validate();
}