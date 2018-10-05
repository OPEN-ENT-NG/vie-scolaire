package fr.openent.viescolaire.service.impl;

import com.opencsv.CSVReader;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.ImportCsvService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.storage.Storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.Map;

public class DefaultImportCsvService implements ImportCsvService {
    protected static final Logger log = LoggerFactory.getLogger(DefaultImportCsvService.class);

    @Override
    public void importAbsencesAndRetard(String idClasse, Long idPeriode, Storage storage,
                                        HttpServerRequest request, Handler<Either<String, JsonObject>> handler){
        storage.writeUploadFile(request, Viescolaire.IMPORT_MAX_SIZE_OCTETS, new Handler<JsonObject>() {
            public void handle(final JsonObject uploaded) {
                if (!"ok".equals(uploaded.getString("status"))) {
                    handler.handle(new Either.Left(uploaded.getString("message")));
                    return;
                }
                // Récupération des élèves de la classe
                new DefaultClasseService().getEleveClasse(idClasse, idPeriode,
                        new Handler<Either<String, JsonArray>>() {
                            @Override
                            public void handle(Either<String, JsonArray> event) {
                                if(event.isLeft()) {
                                    handler.handle(new Either.Left(event.left()));
                                    return;
                                }
                                else {
                                    JsonArray students = event.right().getValue();
                                    // On lance la sauvegarde des données en focntion des ids récupérés dans Neo
                                    saveImportData(storage, uploaded, students, idPeriode, handler);
                                }
                            }
                        });
            }
        });
    }

    private void saveImportData(Storage storage, JsonObject uploaded, JsonArray students,Long idPeriode,
                                Handler<Either<String, JsonObject>> handler){
        // Vérification du format qui doit-être une image ou un pdf
        JsonObject metadata = uploaded.getJsonObject("metadata");
        String contentType = metadata.getString("content-type");

        if (Arrays.asList(".csv", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-excel").contains(contentType)) {
            storage.readFile(uploaded.getString("_id"), new Handler<Buffer>() {
                @Override
                public void handle(Buffer eventBuffer) {
                    if (eventBuffer != null) {
                        Reader reader = new InputStreamReader(
                                new ByteArrayInputStream(eventBuffer.getBytes()));
                        CSVReader csv = new CSVReader(reader);
                        String[] values;
                        int nbLignes = 0;
                        int nbInsert = 0;
                        Boolean withHour = false;
                        Boolean isValid = false;
                        JsonObject infos = new JsonObject();
                        JsonArray notInsertedEleves = new JsonArray();
                        SqlStatementsBuilder statements = new SqlStatementsBuilder();
                        Boolean isUTF8 = false;

                        try {
                            String [] colonnes = new String[16];
                            while ((values = csv.readNext()) != null) {
                                if(nbLignes == 0) {
                                    colonnes = values[0].split("\";\"");
                                    withHour = (colonnes.length == 16);
                                    isValid = (colonnes.length > 2) && (colonnes[1].contains("Nom")
                                            && colonnes[2].contains("Abs tot."));
                                    isUTF8 = "Nom Prénom".equals(colonnes[1]);
                                }
                                else {
                                    if (!isValid) {
                                        break;
                                    }
                                    for (String o : values) {
                                        String [] lines = o.split("\";\"");
                                        String displayName = lines[1];
                                        Long abs = Long.valueOf(lines[2]);
                                        Long abs_hour =  (withHour)?Long.valueOf(lines[3]) : null;

                                        Long notjustifiedAbs =(withHour)?Long.valueOf(lines[4]): Long.valueOf(lines[3]);
                                        Long notjustifiedAbsHour =(withHour)? Long.valueOf(lines[5]) : null;

                                        Long justifiedAbs = (withHour)? Long.valueOf(lines[6]): Long.valueOf(lines[4]);
                                        Long justifiedAbsHour =(withHour)? Long.valueOf(lines [7]): null;

                                        Long retard = (withHour)? Long.valueOf(lines[10]): Long.valueOf(lines[6]);

                                        // Recherche l'identifiant de l'élève
                                        JsonObject student = findStudent(students, displayName, isUTF8);
                                        String idEleve = null;
                                        if(student != null) {
                                            idEleve = student.getString("id");
                                        }
                                        else {
                                            notInsertedEleves.add(displayName);
                                        }

                                        if(idEleve != null ) {
                                            StringBuilder query = new StringBuilder()
                                                    .append(" INSERT INTO viesco.absences_et_retards( ")
                                                    .append(" id_periode, id_eleve, abs_totale, abs_totale_heure, abs_non_just,")
                                                    .append(" abs_non_just_heure, abs_just, abs_just_heure, retard) ")
                                                    .append(" VALUES ")
                                                    .append(" (?, ?, ?, ?, ?, ?, ?, ?, ?) ")
                                                    .append(" ON CONFLICT (id_periode, id_eleve) ")
                                                    .append(" DO UPDATE SET abs_totale=?, abs_totale_heure=?, ")
                                                    .append(" abs_non_just=?, abs_non_just_heure=?, abs_just=?, ")
                                                    .append(" abs_just_heure=?, retard=? ");

                                            JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

                                            params.add(idPeriode).add(idEleve).add(abs).add(abs_hour)
                                                    .add(notjustifiedAbs).add(notjustifiedAbsHour)
                                                    .add(justifiedAbs).add(justifiedAbsHour)
                                                    .add(retard).add(abs).add(abs_hour)
                                                    .add(notjustifiedAbs).add(notjustifiedAbsHour)
                                                    .add(justifiedAbs).add(justifiedAbsHour)
                                                    .add(retard);
                                            statements.prepared(query.toString(),params);
                                            nbInsert ++;
                                        }
                                    }
                                }
                                ++ nbLignes;
                            }
                            infos.put("nbLines", nbLignes);
                            infos.put("isValid", isValid);
                            infos.put("nbInsert", nbLignes - (notInsertedEleves.size() + 1));
                            infos.put("notInsertedUser", notInsertedEleves);
                            infos.put("_id", uploaded.getString("_id"));
                            infos.put("filename", metadata.getString("filename"));
                            if (nbInsert > 0 ) {
                                Sql.getInstance().transaction(statements.build(), (res) -> {
                                    if (!res.isSend()) {
                                        log.error("pb Lors de l'insertion des données importées");
                                        infos.put("error", "pb sql");
                                    }


                                    handler.handle(new Either.Right<>(infos));
                                });
                            }
                            else {
                                handler.handle(new Either.Right<>(infos));
                            }

                        } catch (IOException e) {
                            log.error("pb Lors de la lecture du fichier importé ");
                            throw new EmptyStackException();
                        }
                    } else {
                        log.error("pb Lors de l'insertion des données importées");
                        System.out.println("PB ");
                    }
                }
            });
        }
    }

    private JsonObject findStudent(JsonArray students, String displayName, Boolean isUTF8){
        JsonObject student = null;

        displayName = (isUTF8)? displayName : displayName.replaceAll("[^\\w\\s]","");

        for (int i=0; i<students.size(); i++){
            String _displayName = students.getJsonObject(i).getString("lastName") + " " +
                    students.getJsonObject(i).getString("firstName");

            _displayName = (isUTF8)? _displayName : _displayName.replaceAll("[^\\w\\s]","");

            if(displayName.equals(_displayName)){
                student = students.getJsonObject(i);
                break;
            }
        }
        return student;
    }
}
