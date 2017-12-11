package fr.openent.evaluations.controller;


import fr.openent.Viescolaire;
import fr.openent.evaluations.bean.lsun.*;
import fr.openent.evaluations.service.*;
import fr.openent.evaluations.service.impl.DefaultBFCService;
import fr.openent.evaluations.service.impl.DefaultBfcSyntheseService;
import fr.openent.evaluations.service.impl.DefaultNiveauEnseignementComplementService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.UserService;
import fr.openent.viescolaire.service.impl.DefaultUserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;


/**
 * Created by agnes.lapeyronnie on 30/06/2017.
 */
public class LSUController extends ControllerHelper {

    protected static final Logger log = LoggerFactory.getLogger(LSUController.class);
    private ObjectFactory objectFactory = new ObjectFactory();
    private UserService userService;
    private UtilsService utilsService;
    private BFCService bfcService;
    private BfcSyntheseService bfcSynthseService;
    private NiveauEnseignementComplementService niveauEnsCpl;

    public LSUController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        userService = new DefaultUserService();
        utilsService = new DefaultUtilsService();
        bfcService = new DefaultBFCService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_TABLE);
        bfcSynthseService = new DefaultBfcSyntheseService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_SYNTHESE_TABLE);
        niveauEnsCpl = new DefaultNiveauEnseignementComplementService(Viescolaire.EVAL_SCHEMA,Viescolaire.EVAL_ELEVE_ENSEIGNEMENT_COMPLEMENT);
    }

    /**
     * complete la balise entete et la set a lsunBilans
     * @param lsunBilans
     * @param idStructure
     * @param handler
     */
    private void getBaliseEntete(final LsunBilans lsunBilans, final String idStructure, final Handler<String> handler) {
        userService.getUAI(idStructure, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> response) {
                if (response.isRight()) {
                    JsonObject valueUAI = response.right().getValue();
                    if (valueUAI != null) {
                    Entete entete = objectFactory.createEntete("EDITEUR","LOGICIEL",valueUAI.getString("uai"));
                        lsunBilans.setEntete(entete);
                        handler.handle("success");
                    } else {
                        handler.handle("UAI de l'établissement null");
                        log.error("UAI etablissement null");
                    }
                } else {
                    handler.handle("getBaliseEntete : error when collecting UAI  " + response.left().getValue());
                    log.error("An error occured when collecting UAI for " + idStructure + " structure");
                }
            }
        });
    }

    //récupère chaque responsable d'établissement et les ajouter à la balise responsables-etab puis à la balise donnees
    private void getBaliseResponsables(final Donnees donnees, final List<String> idsResponsable, final Handler<String> handler) {
        userService.getResponsablesEtabl(idsResponsable, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {
                    JsonArray value = response.right().getValue();
                    Donnees.ResponsablesEtab responsablesEtab = objectFactory.createDonneesResponsablesEtab();
                    try {
                        for (int i = 0; i < value.size(); i++) {
                            JsonObject responsableJson = value.get(i);
                            if (!responsableJson.getString("externalId").isEmpty()  && !responsableJson.getString("displayName").isEmpty()) {
                                ResponsableEtab responsableEtab = objectFactory.createResponsableEtab(responsableJson.getString("externalId"),responsableJson.getString("displayName"));
                                responsablesEtab.getResponsableEtab().add(responsableEtab);
                            } else {
                                throw new Exception("attributs responsableEtab null");
                            }
                        }
                        donnees.setResponsablesEtab(responsablesEtab);
                        handler.handle("success");
                    }catch (Throwable e){
                        handler.handle("getBaliseResponsable : " +e.getMessage());
                        log.error("getBaliseResponsable : " +e.getMessage());
                    }
                } else {
                    handler.handle("getBaliseResponsable : error when collecting Responsable " + response.left().getValue());
                    log.error("An error occured when collecting Responsable " + idsResponsable);
                }
            }
        });
    }

    /**
     * pour une liste de classe mise a jour des attributs de l'eleve et de son responsable.
     *
     * @param donnees la liste des eleves est ajoutee a la balise donnees
     * @param Classids liste des classes pour lesquelles le fichier xml doit etre genere
     * @param handler  renvoie  "success" si tout c'est bien passe
     */

    private void getBaliseEleves(final Donnees donnees, final List<String> Classids, final Handler<String> handler) {

        userService.getElevesRelatives(Classids, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {
                    JsonArray jsonElevesRelatives = response.right().getValue();
                    Eleve eleve;
                    Responsable responsable;
                    Donnees.Eleves eleves = objectFactory.createDonneesEleves();
                    if (jsonElevesRelatives.size() > 0) {
                        try {
                            for (int i = 0; i < jsonElevesRelatives.size(); i++) {
                                JsonObject o = jsonElevesRelatives.get(i);
                                if (!eleves.containIdEleve(o.getString("idNeo4j"))) {
                                    eleve = objectFactory.createEleve(o.getString("externalId"), o.getString("attachmentId"), o.getString("firstName"),
                                    o.getString("lastName"),o.getString("nameClass"),o.getString("idNeo4j"),o.getString("idClass"));
                                    eleves.add(eleve);
                                } else {
                                    eleve = eleves.getEleveById(o.getString("idNeo4j"));
                                }
                                Adresse adresse = objectFactory.createAdresse(o.getString("address"),o.getString("zipCode"),o.getString("city"));
                                if (!o.getString("externalIdRelative").isEmpty()&& !o.getString("lastNameRelative").isEmpty()&&
                                        !o.getString("firstNameRelative").isEmpty()&& o.getArray("relative").size() > 0) {
                                   //création d'un responsable Eleve /*Attention responsable sans sa civilite car non present sur NG2 mais obligatoire*/
                                    responsable = objectFactory.createResponsable(o.getString("externalIdRelative"),o.getString("lastNameRelative"),
                                            o.getString("firstNameRelative"),o.getArray("relative"),adresse);
                                } else {
                                    throw new Exception("responsable Eleve non renseigné ");
                                }
                                eleve.getResponsableList().add(responsable);
                            }
                            donnees.setEleves(eleves);
                            handler.handle("success");

                        }catch (Exception e){
                            handler.handle( e.getMessage());
                            log.error("getBaliseEleve : attribut relative est null " + e.getMessage());
                        }
                     } else {
                    handler.handle("getBaliseEleves : error when collecting Eleves " + response.left().getValue());
                    log.error("An error occured when collecting Eleves " + response.left().getValue());
                     }
                }else{
                    handler.handle("getBaliseEleves : error when collecting Eleves " + response.left().getValue());
                    log.error("An error occured when collecting Eleves " + response.left().getValue());
                }
            }
        });
    }

    /**
     *  M
     * @param classIds liste des idsClass dont on recherche le cycle auquel elles appartiennent
     * @param handler retourne une liste de 2 map : map<idClass,idCycle> et map<idCycle,value_cycle>
     */

    private void getIdClassIdCycleValue(List<String> classIds, final Handler<Either<String, List<Map>>> handler) {
        utilsService.getCycle(classIds, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {
                    JsonArray cycles = response.right().getValue();
                    Map mapIclassIdCycle = new HashMap<>();
                    Map mapIdCycleValue_cycle = new HashMap<>();
                    List<Map> mapArrayList = new ArrayList<>();
                    try {
                        for (int i = 0; i < cycles.size(); i++) {
                            JsonObject o = cycles.get(i);
                            if(o.getString("id_groupe")!=null &&o.getLong("id_cycle")!=null&&o.getLong("value_cycle")!=null) {
                                mapIclassIdCycle.put(o.getString("id_groupe"), o.getLong("id_cycle"));
                                mapIdCycleValue_cycle.put(o.getLong("id_cycle"), o.getLong("value_cycle"));
                            }else {
                                throw new Exception ("Erreur idGroupe, idCycle et ValueCycle null");
                            }
                        }
                        mapArrayList.add(mapIclassIdCycle);
                        mapArrayList.add(mapIdCycleValue_cycle);
                    }catch(Exception e){
                        handler.handle(new Either.Left<String, List<Map>>(" Exception " + e.getMessage()));
                        log.error("catch Exception in getCycle" + e.getMessage());
                    }
                    handler.handle(new Either.Right<String, List<Map>>(mapArrayList));
                } else {
                    handler.handle(new Either.Left<String, List<Map>>(" getValueCycle : error when collecting Cycles " + response.left().getValue()));
                    log.error("An error occured when collecting Cycles " + response.left().getValue());
                }
            }
        });
    }

    /**
     * méthode qui permet de construire une Map avec id_domaine et son code_domaine (domaine de hérarchie la plus haute)
     * @param IdClass liste des idsClass
     * @param handler contient la map<IdDomaine,Code_domaine> les codes domaines : codes des socles communs au cycle
     */
    private void getMapCodeDomaineById(String IdClass, final Handler<Either<String, Map<Long, String>>> handler) {
        userService.getCodeDomaine(IdClass, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {

                    JsonArray domainesJson = response.right().getValue();
                    Map<Long, String> mapDomaines = new HashMap<>();

                   try {
                        for (int i = 0; i < domainesJson.size(); i++) {
                            JsonObject o = domainesJson.get(i);
                            if (CodeDomaineSocle.valueOf(o.getString("code_domaine")) != null) {
                                mapDomaines.put(o.getLong("id_domaine"), o.getString("code_domaine"));
                            }
                        }
                       //la mapDomaines n'est renvoyee que si elle contient les 8 codes domaine du socle commun
                       if (mapDomaines.size() == CodeDomaineSocle.values().length) {
                           handler.handle(new Either.Right<String, Map<Long, String>>(mapDomaines));
                       }
                       else{
                           throw new Exception("getMapCodeDomaine : map incomplete" );
                       }
                   }catch (Exception e) {

                        if(e instanceof IllegalArgumentException){
                            handler.handle(new Either.Left<String,Map<Long,String>>("code_domaine en base de données non valide"));
                        }else{
                            handler.handle(new Either.Left<String, Map<Long, String>>("getMapCodeDomaineById : "));
                            log.error("getMapCodeDomaineById : "+e.getMessage());
                        }
                    }
                } else {
                    handler.handle(new Either.Left<String, Map<Long, String>>("getMapCodeDomaineById : error when collecting codeDomaineById : " + response.left().getValue()));
                    log.error("An error occured when collecting CodeDomaineById " + response.left().getValue());
                }
            }
        });
    }


    private JsonObject  GetJsonObject(JsonArray rep ,String idEleve){
        JsonObject repSyntheseIdEleve = new JsonObject();
        for (int i=0; i<rep.size();i++){
            JsonObject o = rep.get(i);
            if((o.getString("id_eleve")).equals(idEleve)){
                repSyntheseIdEleve = o;
            }
        }
        return repSyntheseIdEleve;
    }

    /**
     * permet de completer tous les attributs de la balise BilanCycle et de la setter à donnees
     * sauf les attributs de date, synthese et enseignements de complement
     * @param donnees permet de recuperer les eleves
     * @param idsClass
     * @param idStructure
     * @param handler
     */

    private void getBaliseBilansCycle(final Donnees donnees, final List<String> idsClass, final String idStructure, final Handler<String> handler) {
        final Date millesime = new Date();
        final SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
        final SimpleDateFormat formatDateVerrou = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final SimpleDateFormat dfYYYYMMdd = new SimpleDateFormat("yyyy-MM-dd");
        final Donnees.BilansCycle bilansCycle = objectFactory.createDonneesBilansCycle();
        final Donnees.Eleves eleves = donnees.getEleves();
        final Map<String, List<String>> mapIdClassIdsEleve = eleves.getMapIdClassIdsEleve();

        getIdClassIdCycleValue(idsClass, new Handler<Either<String, List<Map>>>() {
                    @Override
            public void handle(Either<String, List<Map>> repIdClassIdCycleValue) {

                if (repIdClassIdCycleValue.isRight()) {
                    final List<Map> mapIdClassIdCycleValue = repIdClassIdCycleValue.right().getValue();
                    final Map mapIdClassIdCycle = mapIdClassIdCycleValue.get(0);//map<IdClass,IdCycle>
                    final Map mapIdCycleValue = mapIdClassIdCycleValue.get(1);//map<IdCycle,ValueCycle>
                    final AtomicInteger nbIdEleve = new AtomicInteger(0);
                    for (Map.Entry<String, List<String>> stringListEntry : mapIdClassIdsEleve.entrySet()) {
                        final String[] idsEleve = stringListEntry.getValue().toArray(new String[stringListEntry.getValue().size()]);
                        final String idClass = stringListEntry.getKey();

                        bfcService.buildBFC(idsEleve, idClass, idStructure, null, (Long) mapIdClassIdCycle.get(idClass), new Handler<Either<String, Map<String, Map<Long, Integer>>>>() {
                            @Override
                            public void handle(Either<String, Map<String, Map<Long, Integer>>> repBuildBFC) {
                                System.out.println("idCycle : "+mapIdClassIdCycle.get(idClass));
                                if (repBuildBFC.isRight()) {
                                    final Map<String, Map<Long, Integer>> mapIdEleveIdDomainePosition = repBuildBFC.right().getValue();
                                    getMapCodeDomaineById(idClass, new Handler<Either<String, Map<Long, String>>>() {
                                        @Override
                                        public void handle(Either<String, Map<Long, String>> repMapCodeDomaineId) {
                                            if (repMapCodeDomaineId.isRight()) {
                                                final Map<Long, String> mapCodeDomaineByIdDomaine = repMapCodeDomaineId.right().getValue();

                                                bfcSynthseService.getBfcSyntheseByIdsEleve(idsEleve, (Long) mapIdClassIdCycle.get(idClass), new Handler<Either<String, JsonArray>>() {
                                                    @Override
                                                    public void handle(Either<String, JsonArray> repSynthese) {
                                                        if (repSynthese.isRight()) {
                                                            final JsonArray syntheseIdsEleveIdCycle = repSynthese.right().getValue();
                                                            niveauEnsCpl.listNiveauCplByEleves(idsEleve, new Handler<Either<String, JsonArray>>() {
                                                                @Override
                                                                public void handle(Either<String, JsonArray> repEleveEnsCpl) {

                                                                   if(repEleveEnsCpl.isRight()) {
                                                                        final JsonArray listEnsCplDesEleve = repEleveEnsCpl.right().getValue();

                                                                       nbIdEleve.addAndGet(idsEleve.length);//compteur
                                                                       for (String idEleve : idsEleve) {
                                                                           Eleve eleve = eleves.getEleveById(idEleve);
                                                                           //synthese et domaine et enscpl ok alors ajouter le bilanCycle à l'élève sinon stocké l'élève
                                                                           final BilanCycle bilanCycle = objectFactory.createBilanCycle();
                                                                           final BilanCycle.Responsables responsables = objectFactory.createBilanCycleResponsables();
                                                                           final BilanCycle.Socle socle = objectFactory.createBilanCycleSocle();

                                                                           ResponsableEtab responsableEtabRef = donnees.getResponsablesEtab().getResponsableEtab().get(0);
                                                                           bilanCycle.setResponsableEtabRef(responsableEtabRef);
                                                                           bilanCycle.setEleveRef(eleve);
                                                                           bilanCycle.setMillesime(formatYear.format(millesime));
                                                                           bilanCycle.setCycle(new BigInteger(String.valueOf(mapIdCycleValue.get((Long) mapIdClassIdCycle.get(idClass)))));
                                                                           responsables.getResponsable().addAll(eleve.getResponsableList());
                                                                           bilanCycle.setResponsables(responsables);
                                                                           //ens de complément
                                                                           JsonObject ensCplEleve = GetJsonObject(listEnsCplDesEleve, idEleve);
                                                                           if(ensCplEleve.size()>0){
                                                                               EnseignementComplement enseignementComplement = new EnseignementComplement(ensCplEleve.getString("code"),ensCplEleve.getInteger("niveau"));
                                                                               bilanCycle.setEnseignementComplement(enseignementComplement);
                                                                           }//else{} sinon je stock l'élève
                                                                           //setter la synthese et les dates creation et verrou
                                                                           JsonObject jsonObjectSyntheseByEleve = GetJsonObject(syntheseIdsEleveIdCycle, idEleve);
                                                                           if (jsonObjectSyntheseByEleve.size() > 0) {
                                                                               bilanCycle.setSynthese(jsonObjectSyntheseByEleve.getString("texte"));
                                                                               if (jsonObjectSyntheseByEleve.getString("modified") != null) {
                                                                                   bilanCycle.setDateVerrou(jsonObjectSyntheseByEleve.getString("modified"));
                                                                               } else {
                                                                                   bilanCycle.setDateVerrou(jsonObjectSyntheseByEleve.getString("date_creation"));
                                                                               }
                                                                               try {
                                                                                   Date date = dfYYYYMMdd.parse(jsonObjectSyntheseByEleve.getString("date_creation"));
                                                                                   GregorianCalendar cal = new GregorianCalendar();
                                                                                   cal.setTime(date);
                                                                                   // XMLGregorianCalendar dateCreation = DatatypeFactory.newInstance().newXMLGregorianCalendar(jsonObjectSyntheseByEleve.getString("date_creation"));
                                                                                   XMLGregorianCalendar dateCreation = DatatypeFactory.newInstance().newXMLGregorianCalendar();
                                                                                   dateCreation.setYear(cal.get(Calendar.YEAR));
                                                                                   dateCreation.setMonth(cal.get(Calendar.MONTH) + 1);
                                                                                   dateCreation.setDay(cal.get(Calendar.DATE));
                                                                                   bilanCycle.setDateCreation(dateCreation);
                                                                               } catch (DatatypeConfigurationException | ParseException e) {
                                                                                   e.printStackTrace();
                                                                               }
                                                                           }/*else{
                                                                                        handler.handle("La synthèse de l'élève "+eleve.getNom() +" "+eleve.getPrenom()+" de la classe "+eleve.getCodeDivision()+" n'a pas été renseignée");
                                                                                    }//sinon l'élève n'a pas de synthèse à retourner??*/


                                                                           //setter socle : domaine et positionnement
                                                                           //balise socle
                                                                           for (Map.Entry<Long, String> idDomaineCode : mapCodeDomaineByIdDomaine.entrySet()) {
                                                                               DomaineSocleCycle domaineSocleCycle = objectFactory.createDomaineSocleCycle();
                                                                               //cas où les positionnement des domaines peuvent être différents de zéro
                                                                               if (mapIdEleveIdDomainePosition.containsKey(eleve.getIdNeo4j())) {
                                                                                   Map<Long, Integer> mapIdDomainePosition = mapIdEleveIdDomainePosition.get(eleve.getIdNeo4j());
                                                                                   //cas où les positionnements des domaines doivent tous être différents de zéro sauf "CPD_ETR"
                                                                                /*    if (mapIdDomainePosition.containsKey(idDomaineCode.getKey())) {
                                                                                        domaineSocleCycle.setPositionnement(BigInteger.valueOf(mapIdDomainePosition.get(idDomaineCode.getKey())));
                                                                                        domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                                                        socle.getDomaine().add(domaineSocleCycle);
                                                                                    }else if(idDomaineCode.getValue().equals("CPD_ETR")){
                                                                                        domaineSocleCycle.setPositionnement(BigInteger.valueOf(0));
                                                                                        domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                                                        socle.getDomaine().add(domaineSocleCycle);
                                                                                    }else{
                                                                                        handler.handle("getBaliseBilansEleve : Domaine non evalue idDomaine : " +idDomaineCode.getKey()+" codeDomaine : "+idDomaineCode.getValue());
                                                                                        log.error("getBaliseBilansEleve : Domaine non evalue idDomaine " + idDomaineCode.getKey()+" codeDomaine : "+idDomaineCode.getValue());
                                                                                    }
                                                                                }else{
                                                                                    //l'élève n'a aucune note dans aucun des domaines
                                                                                    handler.handle("getBaliseBilansEleve :  l'eleve n'a aucune note dans aucun des domaines idEleve : " + eleve.getIdNeo4j());
                                                                                    log.error("getBaliseBilansEleve : l'eleve n'a aucune note dans aucun des domaines idEleve : " + eleve.getIdNeo4j());
                                                                                }*///Comment remonter remonter le pb

                                                                                   //cas où les positionnements des domaines peuvent être égales à zéro
                                                                                   if (mapIdDomainePosition.containsKey(idDomaineCode.getKey())) {
                                                                                       //rajouter la condition mapIdDomainePostion.size()==CodeDomaineSocle.values()pour verifier que tous les codes domaines ont été évalué
                                                                                       domaineSocleCycle.setPositionnement(BigInteger.valueOf(mapIdDomainePosition.get(idDomaineCode.getKey())));
                                                                                       domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                                                       socle.getDomaine().add(domaineSocleCycle);
                                                                                   } else {
                                                                                       domaineSocleCycle.setPositionnement(BigInteger.valueOf(0));
                                                                                       domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                                                       socle.getDomaine().add(domaineSocleCycle);
                                                                                   }
                                                                               } else {//si l'élève n'est pas ds la mapIdEleveIdDomainePosition = il n'a aucune évaluation
                                                                                   domaineSocleCycle.setPositionnement(BigInteger.valueOf(0));
                                                                                   domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                                                   socle.getDomaine().add(domaineSocleCycle);
                                                                               }
                                                                           }
                                                                           // if(socle.getDomaine().size()==mapCodeDomaineByIdDomaine.size()) {
                                                                           bilanCycle.setSocle(socle);
                                                                           //donnees.getBilansCycle().getBilanCycle().add(bilanCycle);
                                                                           bilansCycle.getBilanCycle().add(bilanCycle);
                                                                           // }

                                                                       }
                                                                       donnees.setBilansCycle(bilansCycle);
                                                                       if (bilansCycle.getBilanCycle().size() == idsEleve.length) {//eleves.getEleve().size()){
                                                                           handler.handle("success");
                                                                       } else if (bilansCycle.getBilanCycle().size() != eleves.getEleve().size() && eleves.getEleve().size() == nbIdEleve.intValue()) {
                                                                           handler.handle("Bilancycle no completed");
                                                                       }
                                                                   }//repEleveEnsCpl else
                                                                }
                                                            });
                                                        }// repSynthese.isRight() else{}
                                                    }
                                                });


                                            } else {
                                                handler.handle("getBaliseBilansCycle :  " + repMapCodeDomaineId.left().getValue());
                                                log.error("getBaliseBilansCycle :  " + repMapCodeDomaineId.left().getValue());
                                            }

                                        }
                                    });
                                } else {
                                    handler.handle("getBaliseBilansEleve : bfcService.buidBFC : " + repBuildBFC.left().getValue());
                                    log.error("bfcService.buildBFC " + repBuildBFC.left().getValue());
                                }
                            }
                        });
                    }
                } else {
                    handler.handle("getBaliseBilansEleve : getValueCycle : " + repIdClassIdCycleValue.left().getValue());
                    log.error("getBaliseBilansCycle : getValueCycle " + repIdClassIdCycleValue.left().getValue());
                }
            }

        });
    }

    void finbfc(int nbClassesTraitees, int nbClasses, Handler<String> handler ){
        if(nbClassesTraitees == nbClasses){
            handler.handle("success");
        }
    }


    /**
     * génère le fichier xml et le valide
     * @param request
     * @param lsunBilans
     */

    private void returnResponse(HttpServerRequest request, LsunBilans lsunBilans) {
        StringWriter response = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(LsunBilans.class);
            Marshaller marshaller = jc.createMarshaller();
           // écriture de la réponse
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "urn:fr:edu:scolarite:lsun:bilans:import import-bilan-complet.xsd");
            marshaller.marshal(lsunBilans, response);

            /* Vérification du fichier xml généré par rapport au xsd */
            File schemaFile = new File(Viescolaire.LSUN_CONFIG.getString("xsd_path"));
            InputStream us = new ByteArrayInputStream(response.toString().getBytes());
            Source xmlFile = new StreamSource(us);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
           // validator.validate(xmlFile);
            //préparation de la requête
            request.response().putHeader("content-type", "text/xml");
            request.response().putHeader("charset", "utf-8");
            request.response().putHeader("Content-Disposition", "attachment; filename=import_lsun_"+ new Date().getTime() +".xml");
            request.response().end(new Buffer(response.toString()));

        } catch (JAXBException |  SAXException /*| IOException */ e) {
            Renders.renderJson(request, new JsonObject().putString("status", "validation.error"), 500);
            e.printStackTrace();
        }

    }

    /**
     * Methode qui contruit le xml pour le LSU
     * @param request contient la list des idsClasse et des idsResponsable ainsi que idStructure sur laquelle sont les responsables
     */
    @Get("/exportLSU/lsu")
    @ApiDoc("Export data to LSUN xml format")
    @SecuredAction("viesco.lsun.export")
    public void getXML(final HttpServerRequest request) {
        //instancier le lsunBilans qui sera composé de entete,donnees et version
        final LsunBilans lsunBilans = objectFactory.createLsunBilans();
        //donnees composée de responsables-etab, eleves et bilans-cycle
        final Donnees donnees = objectFactory.createDonnees();

        final String idStructure = request.params().get("idStructure");
        log.error("idStructure = " + idStructure);
        System.out.println("idStructure = " + idStructure);
        final List<String> idsClasse = request.params().getAll("idClasse");
        final List<String> idsResponsable = request.params().getAll("idResponsable");

        lsunBilans.setSchemaVersion("2.0");

        if(!idsClasse.isEmpty() && !idsResponsable.isEmpty()) {
            getBaliseEntete(lsunBilans, idStructure, new Handler<String>() {
                @Override
                public void handle(String event) {
                    if (event.equals("success")) {

                        getBaliseResponsables(donnees, idsResponsable, new Handler<String>() {
                            @Override
                            public void handle(String event) {
                                if (event.equals("success")) {

                                    getBaliseEleves(donnees, idsClasse, new Handler<String>() {
                                        @Override
                                        public void handle(String event) {
                                            if (event.equals("success")) {

                                                getBaliseBilansCycle(donnees, idsClasse, idStructure, new Handler<String>() {
                                                    @Override
                                                    public void handle(String event) {
                                                        if (event.equals("success")) {

                                                            lsunBilans.setDonnees(donnees);
                                                            returnResponse(request, lsunBilans);
                                                        } else {
                                                            leftToResponse(request, new Either.Left<>(event));
                                                            log.error("getXML : getBaliseBilansCycle" + event);
                                                        }
                                                    }
                                                });
                                            } else {
                                                leftToResponse(request, new Either.Left<>(event));
                                                log.error("getXML : getBaliseEleves " + event);
                                            }
                                        }
                                    });
                                } else {
                                    leftToResponse(request, new Either.Left<>(event));
                                    log.error("getXML : getBaliseResponsable " + event);
                                }
                            }
                        });
                    } else {
                        leftToResponse(request, new Either.Left<>(event));
                        log.error("getXML : getBaliseEntete " + event);

                    }
                }
            });
        }else{
            badRequest(request);
        }

    }

    /**
     * méthode qui récupère les responsables de direction à partir de idStructure
     * @param request
     */
    @Get("/responsablesDirection")
    @ApiDoc("Retourne les responsables de direction de l'établissement passé en paramètre")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getResponsablesDirection(final HttpServerRequest request){
    UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null && request.params().contains("idStructure")) {
                    userService.getResponsablesDirection(request.params().get("idStructure"), arrayResponseHandler(request));
                } else {
                    badRequest(request);
                }
            }
        });

    }

}

