package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.vertx.java.core.Handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface UtilsService {

    /**
     * Ajoute l'element en parametre dans la liste imbriquee a l'index fourni
     *
     * @param value     valeur a ajouter
     * @param index     index auquel ajouter la valeur
     * @param list      la liste dans laquelle ajouter l'element
     * @param <V>       type de la valeur a ajouter
     */
    public <V> void addToList(V value, int index, List<List<V>> list);

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


    /**
     * Renvoie le type des groupes identifies par id. True si le groupe est une classe, false si c'est un groupe
     *
     * @param idClasses       ids des groupes a identifier
     * @param handler          handler portant le resultat de la requete
     */
    public void getTypeGroupe(String[] idClasses,
                              final Handler<Either<String, Map<Boolean, List<String>>>> handler);
}
