package fr.openent.evaluations.service.impl;

import fr.openent.Viescolaire;
import fr.openent.evaluations.bean.NoteDevoir;
import fr.openent.evaluations.service.CompetenceNoteService;
import fr.openent.evaluations.service.NoteService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;

public class DefaultAnnotationService extends SqlCrudService implements fr.openent.evaluations.service.AnnotationService {
    private final NoteService noteService;
    private final CompetenceNoteService competenceNoteService;
    public DefaultAnnotationService(String schema, String table) {
        super(schema, table);
        noteService = new DefaultNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_NOTES_TABLE);
        competenceNoteService = new DefaultCompetenceNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
    }

    public void listAnnotations(String idEtab, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".annotations ")
                .append("WHERE "+ Viescolaire.EVAL_SCHEMA +".annotations.id_etablissement = ?");

        values.addString(idEtab);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createAppreciation(JsonObject appreciation, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(appreciation, user, handler);
    }

    @Override
    public void updateAppreciation(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.update(data.getValue("id").toString(), data, user, handler);
    }

    @Override
    public void deleteAppreciation(Long idAppreciation, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.delete(idAppreciation.toString(), user, handler);
    }

    @Override
    public void createAnnotationDevoir(final Long idDevoir,final Long idAnnotation,final String idEleve,final  Handler<Either<String, JsonObject>> handler) {
        noteService.getNotesParElevesParDevoirs(new String[]{idEleve}, new Long[]{idDevoir},
                new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(final Either<String, JsonArray> eventNotesDevoirs) {
                        if (eventNotesDevoirs.isRight()) {
                            competenceNoteService.getCompetencesNotes(idDevoir,idEleve,
                                    new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> eventCompetencesDevoir) {
                                            JsonArray statements = new JsonArray();

                                            //Si on une compétence note existe sur le devoir, pour un élève donné, on le supprime
                                            if(eventCompetencesDevoir.right() != null){
                                                if(eventCompetencesDevoir.right().getValue() != null
                                                        && eventCompetencesDevoir.right().getValue().size()>0){
                                                    // Suppression compétence note
                                                    StringBuilder queryDeleteCompetenceNote = new StringBuilder().append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competences_notes WHERE id_devoir = ? AND id_eleve = ? ;");
                                                    JsonArray paramsDeleteCompetenceNote = new JsonArray();
                                                    paramsDeleteCompetenceNote.addNumber(idDevoir).addString(idEleve);
                                                    statements.add(new JsonObject()
                                                            .putString("statement", queryDeleteCompetenceNote.toString())
                                                            .putArray("values", paramsDeleteCompetenceNote)
                                                            .putString("action", "prepared"));
                                                }
                                            }

                                            //Si on une note existe sur le devoir, pour un élève donné, on le supprime
                                            if(eventNotesDevoirs.right() != null){
                                                if(eventNotesDevoirs.right().getValue() != null
                                                        && eventNotesDevoirs.right().getValue().size()>0){
                                                    // Suppression Note
                                                    StringBuilder queryDeleteNote = new StringBuilder().append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".notes WHERE id_devoir = ? AND id_eleve = ? ;");
                                                    JsonArray paramsDeleteNote = new JsonArray();
                                                    paramsDeleteNote.addNumber(idDevoir).addString(idEleve);
                                                    statements.add(new JsonObject()
                                                            .putString("statement", queryDeleteNote.toString())
                                                            .putArray("values", paramsDeleteNote)
                                                            .putString("action", "prepared"));
                                                }
                                            }

                                            // Ajout de l'annotation sur le devoir, pour un élève donné
                                            StringBuilder query = new StringBuilder().append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".rel_annotations_devoirs (id_devoir, id_annotation, id_eleve) VALUES (?, ?, ?);");
                                            JsonArray params = new JsonArray();
                                            params.addNumber(idDevoir).addNumber(idAnnotation).addString(idEleve);
                                            statements.add(new JsonObject()
                                                    .putString("statement", query.toString())
                                                    .putArray("values", params)
                                                    .putString("action", "prepared"));

                                            Sql.getInstance().transaction(statements, SqlResult.validRowsResultHandler(handler));
                                        }
                                    });


                        } else {
                            handler.handle(new Either.Left<String, JsonObject>(eventNotesDevoirs.left().getValue()));
                        }
                    }
                });
    }

    @Override
    public void updateAnnotationDevoir(Long idDevoir, Long idAnnotation, String idEleve, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE "+ Viescolaire.EVAL_SCHEMA +".rel_annotations_devoirs SET id_annotation = ? WHERE id_devoir = ? AND id_eleve = ?;");
        values.addNumber(idAnnotation).addNumber(idDevoir).addString(idEleve);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void deleteAnnotation(Long idDevoir, String idEleve, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".rel_annotations_devoirs WHERE id_devoir = ? AND id_eleve = ?;");
        values.addNumber(idDevoir).addString(idEleve);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validRowsResultHandler(handler));
    }
}
