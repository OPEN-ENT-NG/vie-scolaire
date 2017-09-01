package fr.openent.evaluations.controller;


import fr.openent.Viescolaire;
import fr.openent.evaluations.bean.lsun.*;
import fr.openent.evaluations.service.BFCService;
import fr.openent.evaluations.service.DomainesService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultBFCService;
import fr.openent.evaluations.service.impl.DefaultDomaineService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.UserService;
import fr.openent.viescolaire.service.impl.DefaultUserService;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by agnes.lapeyronnie on 30/06/2017.
 */
public class LSUController extends ControllerHelper {

    protected static final Logger log = LoggerFactory.getLogger(LSUController.class);
    private ObjectFactory objectFactory = new ObjectFactory();
    private UserService userService;
    private UtilsService utilsService;
    private BFCService bfcService;
    public LSUController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        userService = new DefaultUserService();
        utilsService = new DefaultUtilsService();
        bfcService = new DefaultBFCService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_TABLE);
    }


    private void getBaliseEntete(final LsunBilans lsunBilans, final String idStructure, final Handler<LsunBilans> handler) {
        userService.getUAI(idStructure, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> response) {
                if (response.isRight()) {
                    JsonObject valueUAI = response.right().getValue();
                    lsunBilans.setEntete(objectFactory.createEntete("EDITEUR", "LOGICIEL", valueUAI.getString("uai")));
                    handler.handle(lsunBilans);
                } else {

                    log.error("An error occured when collecting UAI for " + idStructure + " structure");
                }
            }
        });
    }

    //récupère chaque responsable d'établissement et les ajoute à la balise responsables-etab puis à la balise donnees
    private void getBaliseResponsables(final Donnees donnees, final List<String> idsResponsable, final Handler<Donnees> handler) {
        userService.getResponsablesEtabl(idsResponsable, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {
                    JsonArray value = response.right().getValue();
                    Donnees.ResponsablesEtab responsablesEtab = objectFactory.createDonneesResponsablesEtab();
                    ResponsableEtab responsableEtab = objectFactory.createResponsableEtab();

                    for (int i = 0; i < value.size(); i++) {
                        JsonObject responsableJson = value.get(i);
                        responsableEtab.setId(responsableJson.getString("externalId"));
                        responsableEtab.setLibelle(responsableJson.getString("displayName"));
                        responsablesEtab.getResponsableEtab().add(responsableEtab);
                    }
                    donnees.setResponsablesEtab(responsablesEtab);
                    handler.handle(donnees);

                } else {

                    log.error("An error occured when collecting Responsable " + idsResponsable);
                }
            }
        });
    }



    private void getBaliseEleves(final Donnees donnees, final List<String> Classids, final Handler<Donnees> handler) {

        userService.getElevesRelatives(Classids, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if (response.isRight()) {
                    JsonArray jsonElevesRelatives = response.right().getValue();

                    Eleve eleve;
                    Responsable responsable;
                    Donnees.Eleves eleves = objectFactory.createDonneesEleves();

                    for (int i = 0; i < jsonElevesRelatives.size(); i++) {
                        JsonObject o = jsonElevesRelatives.get(i);
                        //
                        if (!eleves.containIdEleve(o.getString("idNeo4j"))) {
                            eleve = objectFactory.createEleve();
                            eleve.setCodeDivision(o.getString("nameClass"));
                            eleve.setPrenom(o.getString("firstName"));
                            eleve.setNom(o.getString("lastName"));
                            eleve.setIdBe(new BigInteger(o.getString("attachmentId")));
                            eleve.setIdNeo4j(o.getString("idNeo4j"));
                            eleve.setId(o.getString("externalId"));
                            eleve.setId_Class(o.getString("idClass"));
                            eleves.add(eleve);
                        } else {
                            eleve = eleves.getEleveById(o.getString("idNeo4j"));
                        }
                        responsable = objectFactory.createResponsable();
                        Adresse adresse = objectFactory.createAdresse();
                        responsable.setExternalId(o.getString("externalIdRelative"));
                        responsable.setNom(o.getString("lastNameRelative"));
                        responsable.setPrenom(o.getString("firstNameRelative"));
                                        /*if(o.getString("title").equals("M.")){
                                        responsable.setCivilite(Civilite.M);}
                                        else{responsable.setCivilite(Civilite.MME);}sur NG1 seulement*/
                        adresse.setCodePostal(o.getString("zipCode"));
                        adresse.setCommune(o.getString("city"));
                        adresse.setLigne1(o.getString("address"));
                        responsable.setAdresse(adresse);
                        //création du tableau des responsables avec leurs propriétés joinKey,type de relation,responsableFinancier..
                        JsonArray relativesJson = o.getArray("relative");
                        for (int j = 0; j < relativesJson.size(); j++) {
                            String[] relative = relativesJson.get(j).toString().split("\\$");
                            if (responsable.getExternalId().equals(relative[0])) {
                                switch (relative[1]) {
                                    case "1":
                                        responsable.setLienParente("PERE");
                                        break;
                                    case "2":
                                        responsable.setLienParente("MERE");
                                        break;
                                    case "3":
                                        responsable.setLienParente("TUTEUR");
                                        break;
                                    case "4":
                                        responsable.setLienParente("AUTRE MEMBRE DE LA FAMILLE");
                                        break;
                                    case "5":
                                        responsable.setLienParente("DDASS");
                                        break;
                                    case "6":
                                        responsable.setLienParente("AUTRE CAS");
                                        break;
                                    case "7":
                                        responsable.setLienParente("ELEVE LUI-MEME");
                                        break;
                                    default:
                                        break;
                                }
                                switch (relative[3]) {
                                    case "0":
                                        responsable.setLegal1(false);
                                        responsable.setLegal2(false);
                                        break;
                                    case "1":
                                        responsable.setLegal1(true);
                                        responsable.setLegal2(false);
                                        break;
                                    case "2":
                                        responsable.setLegal1(false);
                                        responsable.setLegal2(true);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        eleve.getResponsableList().add(responsable);
                    }
                    donnees.setEleves(eleves);
                    handler.handle(donnees);

                } else {
                    log.error("An error occured when collecting Eleves " + response.left().getValue());
                }
            }
        });
    }

  //méthodes à mettre dans la classe bean Responsable

    private String nameRelative(String codeParent){

        String nameParent=null;
         Responsable responsable = objectFactory.createResponsable();
        switch (codeParent) {
            case "1":

                return "PERE";
            case "2":
                return "MERE";
            case "3":
                return "TUTEUR";
            case "4":
                return "AUTRE MEMBRE DE LA FAMILLE";
            case "5":
                return "DDASS";
            case "6":
                return "AUTRE CAS";
            case "7":
                return "ELEVE LUI-MEME";
            default:
                return null;
        }
    }

    private void getCycles(List<String> classIds, final Handler<Map<String, String>> handler) {
        utilsService.getCycle(classIds, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if(response.isRight()) {
                    JsonArray cycles = response.right().getValue();
                    Map<String, String> cycleMap = new HashMap<>();
                   // System.out.println("CYCLES JSON "+ cycles);
                    for (int i = 0; i < cycles.size(); i++) {
                        JsonObject o = cycles.get(i);
                        cycleMap.put(o.getString("id_groupe"), o.getString("libelle"));
                    }
                    //System.out.println("mapClassCycle " + cycleMap.get("3e2800cd-7756-4d3e-af76-402de0cf1b14"));
                    handler.handle(cycleMap);
                }else{
                    log.error("An error occured when collecting Cycles "+response.left().getValue());
                }
            }
        });
    }

    private void getMapCodeDomaineById(String IdClass, final Handler<Map<Long,String>> handler){
        userService.getCodeDomaine(IdClass, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if(response.isRight()){
                    JsonArray domainesJson =response.right().getValue();
                    //System.out.println(domainesJson);
                    Map<Long,String> mapDomaines = new HashMap<>();
                    for (int i = 0; i < domainesJson.size(); i++) {
                        JsonObject o = domainesJson.get(i);
                       // System.out.println(o);
                        mapDomaines.put(o.getLong("id_domaine"), o.getString("code_domaine"));
                    }
                    //System.out.println("mapDomaines "+ mapDomaines);
                    handler.handle(mapDomaines);
                }else{
                    log.error("An error occured when collecting CodeDomaineById "+response.left().getValue());
                }
            }
        });
    }


    private void getBaliseBilansCycle(final Donnees donnees, final List<String> idsClass,final String idStructure, final Long idPeriode, final Handler<Donnees> handler) {
        final Date millesime = new Date();
        final SimpleDateFormat formatYear = new SimpleDateFormat("yyyy");
        final Date dateVerrou = new Date();
        final SimpleDateFormat formatDateVerrou = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        final Donnees.BilansCycle bilansCycle = objectFactory.createDonneesBilansCycle();
        final Donnees.Eleves eleves = donnees.getEleves();
        Map<String,List<String>> mapIdClassIdsEleve = eleves.getMapIdClassIdsEleve();

        for(Map.Entry<String,List<String>> stringListEntry:mapIdClassIdsEleve.entrySet()){
            String[] idsEleve = stringListEntry.getValue().toArray(new String[stringListEntry.getValue().size()]);
            final String idClass = stringListEntry.getKey();

            bfcService.buildBFC(idsEleve, idClass, idStructure, idPeriode, new Handler<Either<String, Map<String, Map<Long, Integer>>>>() {
                @Override
                public void handle(Either<String, Map<String, Map<Long, Integer>>> repBuildBFC) {
                    if(repBuildBFC.isRight()) {
                       final Map<String, Map<Long, Integer>> mapIdEleveIdDomainePosition = repBuildBFC.right().getValue();

                        getMapCodeDomaineById(idClass, new Handler<Map<Long, String>>() {
                            @Override
                            public void handle(final Map<Long, String> mapCodeDomaineByIdDomaine) {

                                getCycles(idsClass, new Handler<Map<String, String>>() {
                                    @Override
                                    public void handle(Map<String, String> mapIdClassCycle) {

                                        for (Eleve eleve : eleves.getEleve()) {
                                            final BilanCycle bilanCycle = objectFactory.createBilanCycle();
                                            final BilanCycle.Responsables responsables = objectFactory.createBilanCycleResponsables();
                                            final BilanCycle.Socle socle = objectFactory.createBilanCycleSocle();
                                            ResponsableEtab responsableEtabRef = donnees.getResponsablesEtab().getResponsableEtab().get(0);

                                            bilanCycle.setResponsableEtabRef(responsableEtabRef);
                                            bilanCycle.setEleveRef(eleve);
                                            bilanCycle.setMillesime(formatYear.format(millesime));
                                            bilanCycle.setDateVerrou(formatDateVerrou.format(dateVerrou));
                                            //System.out.println("libelle cycle "+mapIdClassCycle.get(eleve.getId_Class()));
                                            bilanCycle.setCycle(new BigInteger(mapIdClassCycle.get(eleve.getId_Class())));

                                            responsables.getResponsable().addAll(eleve.getResponsableList());
                                            bilanCycle.setResponsables(responsables);
                                           // System.out.println(" mapIdEleveMapIdDomainePosition "+mapIdEleveIdDomainePosition);
                                            for(Map.Entry<Long,String> idDomaineCode : mapCodeDomaineByIdDomaine.entrySet()){
                                                //System.out.println(idDomaineCode);
                                                DomaineSocleCycle domaineSocleCycle = objectFactory.createDomaineSocleCycle();
                                                if(mapIdEleveIdDomainePosition.containsKey(eleve.getIdNeo4j())){
                                                    Map<Long,Integer> mapIdDomainePosition= mapIdEleveIdDomainePosition.get(eleve.getIdNeo4j());
                                                    //System.out.println(mapIdDomainePosition);
                                                    if(mapIdDomainePosition.containsKey(idDomaineCode.getKey())) {
                                                        domaineSocleCycle.setPositionnement(BigInteger.valueOf(mapIdDomainePosition.get(idDomaineCode.getKey())));
                                                        domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                        socle.getDomaine().add(domaineSocleCycle);
                                                    } else{domaineSocleCycle.setPositionnement(BigInteger.valueOf(0));
                                                        domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                        socle.getDomaine().add(domaineSocleCycle);
                                                    }

                                                }else{
                                                    domaineSocleCycle.setPositionnement(BigInteger.valueOf(0));
                                                    domaineSocleCycle.setCode(CodeDomaineSocle.fromValue(idDomaineCode.getValue()));
                                                    socle.getDomaine().add(domaineSocleCycle);
                                                }

                                            }
                                            bilanCycle.setSocle(socle);
                                            bilansCycle.getBilanCycle().add(bilanCycle);
                                        }
                                        donnees.setBilansCycle(bilansCycle);
                                        handler.handle(donnees);
                                    }
                                });
                            }
                        });
                    }else{
                        log.error("bfcService.buildBFC "+repBuildBFC.left().getValue());
                    }
                }
            });
        }
    }


    //génère le fichier xml
    private void returnResponse(HttpServerRequest request, LsunBilans lsunBilans) {
        StringWriter response = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(LsunBilans.class);
            Marshaller marshaller = jc.createMarshaller();

            //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "urn:fr:edu:scolarite:lsun:bilans:import import-bilan-complet.xsd");// //

            marshaller.marshal(lsunBilans, response);
            request.response().putHeader("content-type", "text/xml");
            request.response().putHeader("charset", "utf-8");
//          request.response().putHeader("Content-Disposition", "attachment; filename=file.xml");
            request.response().end(new Buffer(response.toString()));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Get("/exportLSU/lsu")
    // @ApiDoc("")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getXML(final HttpServerRequest request) {

        final String idStructure = request.params().get("idStructure");
        log.error("idStructure = " + idStructure);
        System.out.println("idStructure = " + idStructure);
       // final Long idPeriode = Long.parseLong(request.params().get("idPeriode"));
        final List<String> idsClasse = request.params().getAll("idClasse");
        final List<String> idsResponsable = request.params().getAll("idResponsable");

        //instancier le lsunBilans qui sera composé de entete,donnees et version
        final LsunBilans lsunBilans = objectFactory.createLsunBilans();
        lsunBilans.setSchemaVersion("2.0");


        getBaliseEntete(lsunBilans, idStructure, new Handler<LsunBilans>() {
            @Override
            public void handle(final LsunBilans lsunBilans) {
                getBaliseResponsables(objectFactory.createDonnees(), idsResponsable, new Handler<Donnees>() {
                    @Override
                    public void handle(Donnees donnees) {
                        getBaliseEleves(donnees, idsClasse, new Handler<Donnees>() {
                            @Override
                            public void handle(Donnees donnees) {
                                getBaliseBilansCycle(donnees, idsClasse, idStructure,null,new Handler<Donnees>() {
                                    @Override
                                    public void handle(Donnees donnees) {
                                        lsunBilans.setDonnees(donnees);
                                        returnResponse(request, lsunBilans);
                                    }
                                });


                            }
                        });
                    }
                });
            }
        });


            /*Donnees donnees = object.createDonnees();
            //instancier les objects de la balise donnees
            Donnees.ResponsablesEtab responsablesEtab = object.createDonneesResponsablesEtab();
            Donnees.Periodes periodes = object.createDonneesPeriodes();
            Donnees.Eleves eleves = object.createDonneesEleves(); //new Donnees.Eleves();
            Donnees.Enseignants enseignants = object.createDonneesEnseignants();
            Donnees.BilansCycle bilansCycle =object.createDonneesBilansCycle();*/


         /*   //instancier entete et donnees
            Entete entete = object.createEntete("EDITEUR","LOGICIEL","0770002J");


            //Responsables
            ResponsableEtab responsableEtab1 = object.createResponsableEtab();
            responsableEtab1.setId("RESP_01");
            responsableEtab1.setLibelle("Joseph SERGI");

            ResponsableEtab responsableEtab2 = object.createResponsableEtab();
            responsableEtab2.setId("RESP_02");
            responsableEtab2.setLibelle("Catherine NAOWEL");

            responsablesEtab.getResponsableEtab().add(responsableEtab1);
            responsablesEtab.getResponsableEtab().add(responsableEtab2);

            donnees.setResponsablesEtab(responsablesEtab);

            //Periodes
            Periode periode = object.createPeriode();
            periode.setId("P_01");
            periode.setNbPeriodes(3);
            periode.setIndice(1);
            periode.setMillesime("2015");

            periodes.getPeriode().add(periode);

            donnees.setPeriodes(periodes);

            //Eleves
            Eleve eleve1 = object.createEleve();
            eleve1.setId("EL_01");
            eleve1.setIdBe(new BigInteger("1"));
            eleve1.setNom("Grenelle");
            eleve1.setPrenom("Victor");
            eleve1.setCodeDivision("6EME_4");

            Eleve eleve2 = object.createEleve();
            eleve2.setId("EL_02");
            eleve2.setIdBe(new BigInteger("2"));
            eleve2.setNom("Clerc");
            eleve2.setPrenom("Aline");
            eleve2.setCodeDivision("6EME_4");

            eleves.getEleve().add(eleve1);
            eleves.getEleve().add(eleve2);

            donnees.setEleves(eleves);
            //Enseignants

                //Enseignant
            Enseignant enseignant1 = object.createEnseignant();

            enseignant1.setId("ENS_0123456789ABC");
            enseignant1.setPrenom("Hélène");
            enseignant1.setNom("Sand");
            enseignant1.setCivilite(Civilite.MME);
            enseignant1.setIdSts(new BigInteger("123456"));
            enseignant1.setType(TypeEnseignant.EPP);

            enseignants.getEnseignant().add(enseignant1);

            donnees.setEnseignants(enseignants);

            //BilansCycle
                //BilanCycle=socle-domaine /synthese/responsables-responsable-adresse
            BilanCycle bilanCycle1 = object.createBilanCycle();

            bilanCycle1.setMillesime("2015");
            bilanCycle1.setResponsableEtabRef(responsableEtab1);
            bilanCycle1.setDateVerrou("2016-06-23T06:00:00");
            bilanCycle1.setEleveRef(eleve1);
            bilanCycle1.getProfPrincRefs().add(enseignant1);
            XMLGregorianCalendar dateCreation = DatatypeFactory.newInstance().newXMLGregorianCalendar();
            dateCreation.setYear(2016);
            dateCreation.setMonth(06);
            dateCreation.setDay(23);
            bilanCycle1.setDateCreation(dateCreation);
            bilanCycle1.setCycle(new BigInteger("3"));


                //socle
            BilanCycle.Socle socle = object.createBilanCycleSocle();
                //domaine
            DomaineSocleCycle domaineSocleCycle1 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle2 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle3 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle4 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle5 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle6 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle7 = object.createDomaineSocleCycle();
            DomaineSocleCycle domaineSocleCycle8 = object.createDomaineSocleCycle();

            domaineSocleCycle1.setCode(CodeDomaineSocle.CPD_FRA);
            domaineSocleCycle1.setPositionnement(new BigInteger("1"));
            domaineSocleCycle2.setCode(CodeDomaineSocle.CPD_SCI);
            domaineSocleCycle2.setPositionnement(new BigInteger("3"));
            domaineSocleCycle3.setCode(CodeDomaineSocle.CPD_ART);
            domaineSocleCycle3.setPositionnement(new BigInteger("4"));
            domaineSocleCycle4.setCode(CodeDomaineSocle.MET_APP);
            domaineSocleCycle4.setPositionnement(new BigInteger("1"));
            domaineSocleCycle5.setCode(CodeDomaineSocle.FRM_CIT);
            domaineSocleCycle5.setPositionnement(new BigInteger("2"));
            domaineSocleCycle6.setCode(CodeDomaineSocle.SYS_NAT);
            domaineSocleCycle6.setPositionnement(new BigInteger("3"));
            domaineSocleCycle7.setCode(CodeDomaineSocle.REP_MND);
            domaineSocleCycle7.setPositionnement(new BigInteger("4"));
            domaineSocleCycle8.setCode(CodeDomaineSocle.CPD_ETR);
            domaineSocleCycle8.setPositionnement(new BigInteger("2"));
            //ajouter les domaines au socle
            socle.getDomaine().add(domaineSocleCycle1);
            socle.getDomaine().add(domaineSocleCycle2);
            socle.getDomaine().add(domaineSocleCycle3);
            socle.getDomaine().add(domaineSocleCycle4);
            socle.getDomaine().add(domaineSocleCycle5);
            socle.getDomaine().add(domaineSocleCycle6);
            socle.getDomaine().add(domaineSocleCycle7);
            socle.getDomaine().add(domaineSocleCycle8);

            bilanCycle1.setSocle(socle);
                //synthese
            bilanCycle1.setSynthese("Synthèse des acquis scolaires pour victor");

                //responsables
            BilanCycle.Responsables responsables = object.createBilanCycleResponsables();
                    //responsable
            Responsable responsable1 =object.createResponsable();
            responsable1.setPrenom("Prénom responsable 1");
            responsable1.setNom("Nom responsable 1");
            responsable1.setCivilite(Civilite.M);
            responsable1.setLegal2(false);
            responsable1.setLegal1(true);
            responsable1.setLienParente("PERE");
                        //adresse
            Adresse adresse = object.createAdresse();
            adresse.setCommune("GRENOBLE Cedex 2");
            adresse.setCodePostal("38036");
            adresse.setLigne4("BP 241");
            adresse.setLigne3("GRENOBLE");
            adresse.setLigne2("GALERIE DES ARLEQUINS");
            adresse.setLigne1("68");

            responsable1.setAdresse(adresse);
            responsables.getResponsable().add(responsable1);//ajout responsable1 à responsables

            bilanCycle1.setResponsables(responsables);

            bilansCycle.getBilanCycle().add(bilanCycle1);//ajouter le bilanCycle1 aux BilansCycle

            donnees.setBilansCycle(bilansCycle);

            lsuBilans.setDonnees(donnees);
            lsuBilans.setEntete(entete);
            lsuBilans.setSchemaVersion("2.0");

            StringWriter response = new StringWriter();
            JAXBContext jc = JAXBContext.newInstance(LsunBilans.class);
            Marshaller marshaller = jc.createMarshaller();

            //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION,"urn:fr:edu:scolarite:lsun:bilans:import import-bilan-complet.xsd");// //

            marshaller.marshal(lsuBilans, response);

            //Vérification du fichier xml généré par rapport au xsd
            File schemaFile = new File(Viescolaire.LSUN_CONFIG.getString("xsd_path"));
            InputStream us = new ByteArrayInputStream(response.toString().getBytes());
            Source xmlFile = new StreamSource(us);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);

            // CASE WHEN IS VALID
            request.response().putHeader("content-type", "text/xml");
            request.response().putHeader("charset", "utf-8");
            request.response().putHeader("Content-Disposition", "attachment; filename=file.xml");
            request.response().end(new Buffer(response.toString()));
        } catch (JAXBException | DatatypeConfigurationException | SAXException | IOException e) {
            Renders.renderJson(request, new JsonObject().putString("status", "validation.error"), 500);
            e.printStackTrace();
        }*/

    }

}
//récupère chaque élève de la classe demandée et les ajoute à la balise eleves puis à la balise donnees
  /*  private void getBaliseEleves(final Donnees donnees,final List<String> idsClass,final Handler<Donnees> handler){
        userService.getEleves(idsClass, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> response) {
                if(response.isRight()){
                    JsonArray result = response.right().getValue();
                    Donnees.Eleves eleves = objectFactory.createDonneesEleves();
                    Eleve eleve;
                    for(int i=0;i< result.size();i++ ){
                        eleve = objectFactory.createEleve();
                        JsonObject jsonObject = result.get(i);
                        eleve.setId(jsonObject.getString("externalId"));
                        eleve.setIdBe(new BigInteger(jsonObject.getString("attachmentId")));
                        eleve.setPrenom(jsonObject.getString("firstName"));
                        eleve.setNom(jsonObject.getString("lastName"));
                        eleve.setCodeDivision(jsonObject.getString("nameClass"));
                        eleves.getEleve().add(eleve);
                    }
                    donnees.setEleves(eleves);
                    handler.handle(donnees);
                }else{
                    log.error("An error occured when collecting Responsable " + idsClass);
                }

            }
        });
    }*/
/*private void getMapEleveRelatives(final List<String> idsClass,final Handler<Map<Eleve,List<Responsable>>> handler){

    userService.getElevesRelatives(idsClass, new Handler<Either<String, JsonArray>>() {
        @Override
        public void handle(Either<String, JsonArray> response) {
            if(response.isRight()) {
                JsonArray jsonElevesRelatives = response.right().getValue();
                Map<Eleve,List<Responsable>> mapEleveRelatives = new LinkedHashMap<>();
                Eleve eleve;

                for (int i = 0; i < jsonElevesRelatives.size(); i++) {
                    JsonObject o = jsonElevesRelatives.get(i);
                    eleve = objectFactory.createEleve();
                    Responsable responsable = objectFactory.createResponsable();
                    Adresse adresse =objectFactory.createAdresse();

                    eleve.setCodeDivision(o.getString("nameClass"));
                    eleve.setPrenom(o.getString("firstName"));
                    eleve.setNom(o.getString("lastName"));
                    eleve.setIdBe(new BigInteger(o.getString("attachmentId")));
                    eleve.setIdNeo4j(o.getString("idNeo4j"));
                    eleve.setId(o.getString("externalId"));
                    eleve.setId_Class(o.getString("idClass"));

                    responsable.setNom(o.getString("lastNameRelative"));
                    responsable.setPrenom(o.getString("firstNameRelative"));
                    responsable.setExternalId(o.getString("externalIdRelative"));
                            /*if(o.getString("title").equals("M.")){
                            responsable.setCivilite(Civilite.M);}
                            else{responsable.setCivilite(Civilite.MME);}sur NG1 seulement*/
   /*                 adresse.setCodePostal(o.getString("zipCode"));
                    adresse.setCommune(o.getString("city"));
                    adresse.setLigne1(o.getString("address"));
                    responsable.setAdresse(adresse);

                    //création du tableau des responsables avec leurs propriétés joinKey,type de relation,responsableFinancier..
                    JsonArray relativesJson = o.getArray("relative");
                    // String[][] relatives = new String[relativesJson.size()][6];
                    for (int j = 0; j < relativesJson.size(); j++) {
                        String[] relative = relativesJson.get(j).toString().split("\\$");
                        if(responsable.getExternalId().equals(relative[0])) {
                            //for (int k = 0; k < relative.length; k++) {
                            //relatives[j][k] = relative[k];
                            switch (relative[1]) {
                                case "1":
                                    responsable.setLienParente("PERE");
                                    break;
                                case "2":
                                    responsable.setLienParente("MERE");
                                    break;
                                case "3":
                                    responsable.setLienParente("TUTEUR");
                                    break;
                                case "4":
                                    responsable.setLienParente("AUTRE MEMBRE DE LA FAMILLE");
                                    break;
                                case "5":
                                    responsable.setLienParente(("DDASS"));
                                    break;
                                case "6":
                                    responsable.setLienParente("AUTRE CAS");
                                    break;
                                case "7":
                                    responsable.setLienParente("ELEVE LUI-MEME");
                                    break;
                                default:
                                    break;
                            }
                            switch (relative[3]) {
                                case "0":
                                    responsable.setLegal1(false);
                                    responsable.setLegal2(false);
                                    break;
                                case "1":
                                    responsable.setLegal1(true);
                                    responsable.setLegal2(false);
                                    break;
                                case "2":
                                    responsable.setLegal1(false);
                                    responsable.setLegal2(true);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    if(mapEleveRelatives.get(eleve) != null) {
                        mapEleveRelatives.get(eleve).add(responsable);
                    } else {
                        List<Responsable> responsables = objectFactory.createBilanCycleResponsables().getResponsable();
                        responsables.add(responsable);
                        mapEleveRelatives.put(eleve, responsables);
                    }
                }
                handler.handle(mapEleveRelatives);
            }else{

                log.error("An error occured when collecting Eleves "+response.left().getValue());
            }

        }

    });
}

    final Map<Eleve,List<Responsable>> mapEleveRelatives = new LinkedHashMap<>();

    private void getBaliseEleves(final Donnees donnees,final List<String> Classids,final Handler<Donnees> handler){
        final Donnees.Eleves eleves = objectFactory.createDonneesEleves();

        getMapEleveRelatives(Classids, new Handler<Map<Eleve,List<Responsable>>>() {
            @Override
            public void handle(Map<Eleve,List<Responsable>> eleveMap) {
                //parcourrir la map et ajouter chaque élève à la list eleves.getEleve()
                mapEleveRelatives.putAll(eleveMap);
                for ( Map.Entry<Eleve,List<Responsable>> eleveEntry: mapEleveRelatives.entrySet() ){
                    eleves.getEleve().add(eleveEntry.getKey());
                }
                donnees.setEleves(eleves);
                handler.handle(donnees);
            }
        });
    }*/
