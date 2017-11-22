package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface UtilsService {

    /**
     * Ajoute l'element en parametre dans la liste imbriquee dans la map a la cle fournie
     *
     * @param value     valeur a ajouter
     * @param key       cle a laquelle ajouter la valeur
     * @param map       map dans laquelle la valeur est ajoutee
     * @param <T>       type de la valeur a ajouter
     * @param <V>       type de la cle a laquelle ajouter la valeur
     */
    public <T, V> void addToMap(V value, T key, Map<T, List<V>> map);

    public JsonObject[] convertTo(Object[] value);

    /**
     * Renvoie le type des groupes identifies par id. True si le groupe est une classe, false si c'est un groupe
     *
     * @param idClasses       ids des groupes a identifier
     * @param handler          handler portant le resultat de la requete
     */
    public void getTypeGroupe(String[] idClasses, Handler<Either<String, JsonArray>> handler);
}
