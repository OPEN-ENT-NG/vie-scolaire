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
                        try {
                            String [] colonnes = new String[16];
                            while ((values = csv.readNext()) != null) {
                               if(nbLignes == 0) {
                                   colonnes = values[0].split("\";\"");
                                   withHour = (colonnes.length == 16);
                                   isValid = colonnes[1].contains("Nom")
                                           && colonnes[2].contains("Abs tot.");

                               }
                                else {
                                   if (!isValid) {
                                       break;
                                   }
                                    for (String o : values) {
                                        String [] lines = o.split("\";\"");
                                        String displayName = lines[1];
                                        Long abs = Long.valueOf(lines[2]);
                                        Long abs_hour =  Long.valueOf(lines[3]);

                                        Long notjustifiedAbs = Long.valueOf(lines[4]);
                                        Long notjustifiedAbsHour = Long.valueOf(lines[5]);

                                        Long justifiedAbs = Long.valueOf(lines[6]);
                                        Long justifiedAbsHour = Long.valueOf(lines [7]);

                                        Long retard = Long.valueOf(lines[10]);

                                        System.out.println("  ");
                                        System.out.println(" ------------------------ ");
                                        System.out.println(colonnes[1] + " -> "  + lines[1]);
                                        System.out.println(colonnes[2] + " -> "  + lines[2]);
                                        System.out.println(colonnes[3] + " -> "  + lines[3]);
                                        System.out.println(colonnes[4] + " -> "  + lines[4]);
                                        System.out.println(colonnes[5] + " -> "  + lines[5]);
                                        System.out.println(colonnes[6] + " -> "  + lines[6]);
                                        System.out.println(colonnes[7] + " -> "  + lines[7]);
                                        System.out.println(colonnes[10] + " -> "  + lines[10]);
                                        JsonObject student = findStudent(students, displayName);
                                        String idEleve = null;
                                        if(student != null) {
                                            System.out.println("Find !!!! ");
                                            idEleve = student.getString("id");
                                        }
                                        else {
                                            notInsertedEleves.add(displayName);
                                            // TODO REMOVE
                                            idEleve = students.getJsonObject(Math.min(nbInsert, students.size() -1))
                                                    .getString("id");
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
                            final int _nbLines = nbLignes;
                            final  Boolean _isValid = isValid;
                            Sql.getInstance().transaction(statements.build(), (res)->{
                                JsonArray exclusions;
                                if (!res.isSend()) {
                                    handler.handle(new Either.Left<>(
                                            "pb Lors de l'insertion des données importées"));
                                } else {

                                    infos.put("nbLines", _nbLines);
                                    infos.put("isValid", _isValid);
                                    infos.put("notInsertedUser", notInsertedEleves);
                                    handler.handle(new Either.Right<>(infos));
                                }
                            });

                        } catch (IOException e) {
                            throw new EmptyStackException();
                        }
                    } else {
                        System.out.println("PB ");
                    }
                }
            });
        }
    }

    private JsonObject findStudent(JsonArray students, String displayName){
        JsonObject student = null;
        for (int i=0; i<students.size(); i++){
           String _displayName = students.getJsonObject(i).getString("lastName") +
                   students.getJsonObject(i).getString("firstName");
            if(displayName.equals(_displayName)){
                student = students.getJsonObject(i);
                break;
            }
        }
        return student;
    }
}
