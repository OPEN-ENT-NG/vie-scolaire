import {notify, idiom as lang, angular, ng, _, toasts, moment} from 'entcore';
import * as utils from '../../utils/services';
import {TypeSubTopic, Service, Services, TypeSubTopics, ServiceClasse, MultiTeaching} from "../models/services";
import {safeApply} from "../../utils/services";
import {SubjectService,GroupService,UserService} from "../services";
import http from "axios";
import {Teacher} from "../models/common/Teacher";

export let evalAcuTeacherController = ng.controller('ServiceController',[
    '$scope','SubjectService','UserService','GroupService',
    async function ($scope,subjectService:SubjectService,userServices:UserService,groupServices:GroupService ) {
        $scope.sortBy = "topicName";
        $scope.sortByAsc = true;
        $scope.filter = "classes=true&groups=true&manualGroups=true";
        $scope.servicesTemp = new Services([]);
        function getGroupsName(service, groups, groups_name: string) {
            service.id_groups.forEach(
                id => {
                    let group = _.findWhere($scope.columns.classe.data, {id: id});
                    groups.push(group);
                }
            );
            groups.sort((group1, group2) => {
                if (group1.name > group2.name) {
                    return 1;
                }
                if (group1.name < group2.name) {
                    return -1;
                }
                return 0;
            });
            groups_name = groups.join(",");
            return groups_name;
        }

        function setServices(data) {
            $scope.services = _.reject(_.map(data, service => {
                let enseignant = _.findWhere($scope.columns.enseignant.data, {id: service.id_enseignant});
                let groupe = _.findWhere($scope.columns.classe.data, {id: service.id_groupe});
                let groups = [];
                let subTopics = [];
                let allCoTeachers = service.coTeachers;

                let matiere = _.findWhere($scope.columns.matiere.data, {id: service.id_matiere});
                if (matiere && matiere.sous_matieres && matiere.sous_matieres.length > 0)
                    matiere.sous_matieres.forEach(sm => {
                        $scope.subTopics.all.map(sb => {
                            if (sm.id_type_sousmatiere == sb.id) {
                                subTopics.push(sb)
                            }
                        });
                    });


                    let groups_name = "";
                    groups_name = getGroupsName(service, groups, groups_name);

                    if(allCoTeachers){
                        _.each(allCoTeachers , (coTeacher)=> {
                            coTeacher.displayName = (_.findWhere($scope.columns.enseignant.data,
                                {id: coTeacher.second_teacher_id}) != undefined)? _.findWhere($scope.columns.enseignant.data,
                                {id: coTeacher.second_teacher_id}).displayName : "";

                        });
                    }
                    let coTeachers = _.reject(allCoTeachers, coTeacher => {return !coTeacher.is_coteaching});
                    let secondTeacher = _.reject(allCoTeachers, coTeacher => {return coTeacher.is_coteaching});
                    let missingParams = {
                        id_etablissement: $scope.idStructure,
                        nom_enseignant: enseignant ? enseignant.displayName : null,
                        topicName: matiere ? matiere.name  + " (" + matiere.externalId + ")"  : null,
                        nom_groupe: groupe ? groupe.name : null,
                        groups: groups ? groups : null,
                        groups_name: groups_name ? groups_name : null,
                        subTopics: subTopics ? subTopics : [],
                        multiTeaching: new MultiTeaching($scope.idStructure, enseignant,  matiere,
                            true, coTeachers),
                        substituteTeacher: new MultiTeaching($scope.idStructure,enseignant, matiere,
                            false, secondTeacher)
                    };
                    return new Service(_.defaults(service, missingParams));
                }), service => service.hasNullProperty());
        }

        async function getAndsetServices() {
            if(!$scope.displayMessageLoader)
                await $scope.runArrayLoader();

            $scope.servicesTemp.getServices($scope.structure.id,$scope.filter).then(async ({data}) => {

                setServices(data);

                await $scope.stopArrayLoader();
                await $scope.stopMessageLoader();
                utils.safeApply($scope);
            }).catch(async (error) => {
                console.error(error);
                await $scope.stopArrayLoader();
                await $scope.stopMessageLoader();

            });
        }

        async function initServices () {
            await $scope.runMessageLoader();
            getAndsetServices();
            console.log("plpo")
            $scope.classesSelected = [];
        }

        $scope.filterSearch = () => {
            return (service) => {
                let isInSearched = true;
                if($scope.searchToFilter.length !=0){
                    $scope.searchToFilter.forEach(search =>{
                        if( !(service.groups_name.toUpperCase().includes(search.toUpperCase())
                            || service.nom_enseignant.toUpperCase().includes(search.toUpperCase())
                            || service.topicName.toUpperCase().includes(search.toUpperCase()))){
                            isInSearched = false;
                        }
                    });
                }else{
                    isInSearched = true;

                }
                return isInSearched;
            }
        };

        $scope.deploySubtopics = (service) =>{
            if(service.subTopics.length != 0)
                service.deploy = !service.deploy
        };
        $scope.saveSearch = async (event) =>{
            if (event && (event.which === 13 || event.keyCode === 13 )) {
                if (!_.contains($scope.searchToFilter, event.target.value)
                    && event.target.value.length > 0
                    && event.target.value.trim()){
                    $scope.searchToFilter.push(event.target.value);
                }
                event.target.value = '';
            }
        };
        $scope.dropSearchFilter = (search) =>{
            $scope.searchToFilter = _.without($scope.searchToFilter, search);
        };
        $scope.init = async () =>{
            $scope.idStructure = $scope.structure.id;
            $scope.services = [];
            $scope.searchToFilter = [];
            $scope.matiereSelected = "";
            $scope.typeGroupes= {
                classes: {isSelected: true, filterName:"classes", name: "evaluation.service.filter.classes", type: 0},
                groupes: {isSelected: true, filterName:"groups", name: "evaluation.service.filter.groupes", type: 1},
                manualGroupes : {isSelected: true, filterName:"manualGroups", name:"evaluation.service.filter.manualGroupes", type:2}
            };

            $scope.subTopics = new TypeSubTopics([]);
            $scope.columns = {
                matiere: {size: "four", data: [], name: "evaluation.service.columns.matiere", filtered: false},
                classe: {size: "two", data: [], name: "evaluation.service.columns.classGroup", filtered: false},
                enseignant: {size: "two", data: [], name: "evaluation.service.columns.teacher", filtered: false},
                remplacement: {size: "two", data: [], name: "evaluation.service.columns.remplacement", filtered: false},
                actions: {size: "one", name: "evaluation.service.columns.actions", filtered: false},
            };

            $scope.lightboxes = {
                switchEval: false,
                confirm: false,
                create: false,
                subEducationCreate:false,
                switchEvaluation:false,
                addTeacher:false,
            };
            await $scope.runMessageLoader();
            $scope.classesSelected = [];

            Promise.all([groupServices.getClasses($scope.idStructure),
                subjectService.getMatieres($scope.idStructure),
                userServices.getTeachers($scope.idStructure),
                $scope.servicesTemp.getServices($scope.structure.id,$scope.filter),
                $scope.subTopics.get()
            ])
                .then(async function([aClasses, aMatieres, aTeachers,aServices]) {
                    $scope.columns.classe.data = _.map(aClasses.data,
                        (classe) => {

                            classe.type_groupe_libelle = ServiceClasse.get_type_groupe_libelle(classe);
                            classe = new ServiceClasse(classe);
                            return classe;
                        });
                    $scope.columns.enseignant.data = _.map(aTeachers.data, teacher => new Teacher(teacher.u.data));
                    $scope.columns.matiere.data = aMatieres.data;
                    setServices(aServices.data);
                    await $scope.stopArrayLoader();
                    await $scope.stopMessageLoader();
                    utils.safeApply($scope);
                });
        };
        $scope.runMessageLoader = async () => {
            $scope.displayMessageLoader = true;
            await utils.safeApply($scope);
        };
        $scope.stopMessageLoader = async  () => {
            $scope.displayMessageLoader = false;
            await utils.safeApply($scope);
        };
        $scope.runArrayLoader = async () => {
            $scope.displayArrayLoader = true;
            await utils.safeApply($scope);
        };
        $scope.stopArrayLoader = async  () => {
            $scope.displayArrayLoader = false;
            await utils.safeApply($scope);
        };
        $scope.updateFilterEvaluable = (selectedHeader) =>{
            _.each($scope.headers, header => header.isSelected = false);
            selectedHeader.isSelected = true;
        };


        $scope.updateFilter = (selectedHeader)  =>{
            selectedHeader.isSelected = !selectedHeader.isSelected;
            let filtersArray = _.values($scope.typeGroupes);
            if(filtersArray.length != 0 ){
                $scope.filter= "";
                filtersArray.forEach(filter =>{
                    $scope.filter  += filter.filterName + "=" + filter.isSelected + "&";
                })
                $scope.filter.substring(0,$scope.filter.length-1);
            }
            getAndsetServices();
        };


        $scope.checkIfExistsAndValid =  (service) =>{
            let exist = false;
            if($scope.classesSelected && $scope.classesSelected.length>0 && service.id_matiere != undefined &&
                service.id_matiere != "" && service.id_enseignant != "") {
                $scope.classesSelected.forEach(classe => {
                    if (_.findWhere($scope.services, {
                        id_matiere: service.id_matiere, id_enseignant: service.id_enseignant,
                        id_groupe: classe
                    }))
                        exist = true;
                });
                return exist;
            }else{
                return true;
            }
        };

        $scope.checkDevoirsService = async (service, callback) =>{
            service.getDevoirsService().then(async function ({data}) {
                if (data.length == 0) {
                    await callback();
                    return false;
                } else {
                    $scope.service = service;
                    $scope.devoirs = data;
                    $scope.callback = callback;
                    $scope.error = lang.translate("evaluations.service.devoir.error").replace("[nbDevoir]", $scope.devoirs.length);
                    $scope.lightboxes.switchEval = true;
                    await utils.safeApply($scope);
                    return true;
                }
            })
        };
        $scope.initMatieresForSelect = () =>{
            let isAlreadyIn = false;
            $scope.matieresForSelect = [];
            $scope.columns.matiere.data.map(matiere =>{
                    $scope.matieres.forEach(mm =>{
                        if(mm.id === matiere.id)
                            isAlreadyIn = true;
                    });
                    if(!isAlreadyIn)
                        $scope.matieresForSelect.push(matiere);
                    isAlreadyIn = false;
                }
            )

        };
        $scope.openCreationSubTopicCreationInput =() =>{
            $scope.subTopicCreationForm = true;
            safeApply($scope);
        };
        $scope.saveNewSubTopic = async () =>{
            $scope.subTopicCreationForm = false;
            let subTopic = new TypeSubTopic();
            if ($scope.newSubTopic) {
                subTopic.libelle = $scope.newSubTopic;
                let isSaved = await subTopic.save();
                if (isSaved === false) {
                    $scope.lightboxes.subEducationCreate = false;
                    toasts.warning("viesco.subTopic.creation.error");
                } else {
                    subTopic.selected = true;
                    $scope.subTopics.all.push(subTopic);
                }
            }
            safeApply($scope)
        };
        $scope.closeUpdatingSubtopic = () =>{
            $scope.subTopics.all.map(topic => {
                if(topic.updating)
                    topic.save();
                topic.updating = false;
            });
        };
        $scope.openUpdateForm =(matiere) =>{
            $scope.closeUpdatingSubtopic();
            safeApply($scope);
            matiere.updating = true;
            matiere.oldLibelle = matiere.libelle;

        };
        $scope.updateMatiere = async (matiere) =>{
            if( matiere.libelle === "") {
                matiere.libelle = matiere.oldLibelle;
            }
            matiere.updating = false;
            await matiere.save();
        };
        $scope.closeUpdateLibelle = (event,matiere) =>{
            if (event.which === 13) {
                matiere.updating = false;
            }
        };
        $scope.updateSubTopic= (newSubTopic) =>{
            $scope.newSubTopic = newSubTopic;
        };
        $scope.openSubEducationLightBoxCreation = (id_matiere) =>{
            $scope.matieres =[];
            $scope.subTopicCreationForm = false;
            $scope.newSubTopic="";
            $scope.columns.matiere.data.map(matiere => {
                if (matiere.id === id_matiere && !$scope.matieres.find(m => matiere.id === m.id)) {
                    matiere.selected = true;
                    let hasNoSubTopic = true;
                    $scope.matieres.push(matiere);
                    matiere.sous_matieres.forEach(sm => {
                        $scope.subTopics.all.map(sb =>{
                            if(sm.id_type_sousmatiere == sb.id){
                                sb.selected = true;
                                hasNoSubTopic = false;
                            }
                        });
                    })
                }
            });
            $scope.initMatieresForSelect();
            $scope.lightboxes.subEducationCreate = true;

        };
        $scope.addMatiereToCreateSubTopic = async (matiereToAdd) =>{
            matiereToAdd.selected = true;
            $scope.matieres.push(matiereToAdd);
            $scope.matieresForSelect.map((matiere,index) =>{
                if (matiere.id === matiereToAdd.id){
                    $scope.matieresForSelect.splice(index,1)
                }
            });
            await utils.safeApply($scope);
        };
        $scope.cancelSwitchEvaluation = () =>{
            $scope.lightboxes.switchEvaluation = false;
            $scope.lightboxes.subEducationCreate = true;
            safeApply($scope);

        };
        $scope.saveNewRelationsSubTopics = async  () =>{
            if($scope.matieres.find(matiere => matiere.selected)) {
                let matieresUpdated =[];
                $scope.matieres.forEach( matiere =>{
                    if(matiere.selected){
                        matieresUpdated.push(matiere);
                    }
                });
                let isSaved = await $scope.subTopics.saveTopicSubTopicRelation($scope.matieres);
                if (!isSaved) {
                    toasts.warning("viesco.subTopic.relation.creation.error");
                }
                $scope.services.map(service => {
                    matieresUpdated.forEach(matiere => {
                        if(service.id_matiere === matiere.id){
                            service.subTopics = $scope.subTopics.selected;
                            if($scope.subTopics.selected.length === 0){
                                service.deploy = false;
                            }
                        }
                    });
                });
            }

            toasts.confirm("viesco.subTopic.creation.confirm");
            if( $scope.lightboxes.subEducationCreate)
                $scope.lightboxes.subEducationCreate  = false;

            if( $scope.lightboxes.switchEvaluation)
                $scope.lightboxes.switchEvaluation  = false;

            await utils.safeApply($scope);

        };
        $scope.openSwitchEvaluation = () =>{
            $scope.lightboxes.subEducationCreate = false;
            $scope.matieresWithoutSubTopic =[];
            $scope.matieres.forEach(matiere =>{
                if(matiere.selected && matiere.sous_matieres.length === 0){
                    $scope.matieresWithoutSubTopic.push(matiere)
                }
            });
            $scope.defaultSubTopic = $scope.subTopics.all.find(subtopic => subtopic.selected)

            $scope.lightboxes.switchEvaluation = true;
            safeApply($scope);
        };
        $scope.cancelSubEducationCreate = () =>{
            $scope.lightboxes.subEducationCreate = false;
            $scope.services.map(service => {
                service.selected = false;
            });
            $scope.subTopics.forEach(subTopics=>{
                if(subTopics.selected){
                    subTopics.selected = false;
                }
            });
            utils.safeApply($scope);
        };
        $scope.checkBeforeSavingNewRelations = () =>{
            $scope.closeUpdatingSubtopic();
            if($scope.matieres.find(matiere => matiere.selected && matiere.sous_matieres.length === 0)
                && $scope.subTopics.selected.length !== 0 ){
                $scope.openSwitchEvaluation()
            }
            else{
                $scope.saveNewRelationsSubTopics();
                $scope.lightboxes.subEducationCreate  = false;

            }
        };
        $scope.getSelectedDisciplines = () =>{
            let selectedDisciplines = [];
            $scope.services.forEach(service =>{
                if(service.selected)
                    selectedDisciplines.push(service);
            });
            return selectedDisciplines;
        };
        $scope.oneDisicplineSelected = () =>{
            let service = $scope.services.find(service => service.selected)
            return service !== undefined;
        };

        $scope.deleteService = async (service) => {
            await  $scope.checkDevoirsService(service, async () => {
                await service.deleteService();
                toasts.confirm('evaluation.service.delete');
                await initServices();
            });
        };
        $scope.doUpdateOrDelete = (updateOrDelete, devoirs, service) =>{
            let id_devoirs = _.pluck(devoirs, "id");
            switch (updateOrDelete){
                case "update": {
                    if($scope.matiereSelected) {
                        let matiere = $scope.matiereSelected;
                        if(matiere === service.id_matiere){
                            toasts.confirm('evaluation.service.choose.another.subject');
                            break;
                        }
                        service.updateDevoirsService(id_devoirs, matiere).then(async () => {
                            let topicName = _.findWhere($scope.columns.matiere.data, {id : matiere}).name;
                            let newService = new Service({...service.toJson(),
                                id_matiere: matiere, topicName: topicName, evaluable: true});
                            await newService.createService($scope.classesSelected, $scope.lightboxes.create);
                            $scope.services.push(newService);
                            await $scope.callback();
                            $scope.lightboxes.switchEval = false;
                            toasts.confirm('evaluation.service.update');
                            await initServices();
                        });
                    }
                    else {
                        toasts.info('evaluation.service.choose.a.subject');
                    }
                }
                    break;
                case "delete" : {
                    service.deleteDevoirsService(id_devoirs).then(async () => {
                        try {
                            await $scope.callback();
                            $scope.lightboxes.switchEval = false;
                            toasts.confirm('evaluation.service.delete');
                            await initServices();
                        }
                        catch (e) {
                            console.error(e);
                            toasts.warning('evaluation.service.delete.error');
                        }

                    });
                }
            }
        };

        $scope.setMatiere = (matiere) => {
            $scope.matiereSelected = matiere;
        };

        $scope.deleteClasse = (classe) => {
            for (let i = 0; i < $scope.classesSelected.length; i++) {
                if ($scope.classesSelected[i] == classe) {
                    $scope.classesSelected = _.without($scope.classesSelected,
                        $scope.classesSelected[i]);
                }
            }
        };

        $scope.pushData  = (classe,listClasses) =>{
            utils.pushData(classe,listClasses);
        };

        $scope.openCreateLightbox = () => {
            console.log("im in")
            $scope.service = new Service(
                {
                    id_etablissement: $scope.idStructure,
                    id_matiere: "",
                    id_enseignant: "",
                    id_groupe: "",
                    topicName: "",
                    nom_enseignant: "",
                    nom_groupe: "",
                    isManual: true,
                    evaluable: false
                });
            $scope.lightboxes.create = true;
        };


        $scope.openAddTeacherLightbox = (service, isCoteaching, isUpdate ?) => {
            $scope.lightboxes.addTeacher = true;
            let teachers = $scope.columns.enseignant.data;
            $scope.teachersLihtboxTeacher = _.reject(teachers, teacher => {return teacher.id == service.id_enseignant});
            if(isCoteaching){
                $scope.coTeaching = service.multiTeaching;
                if($scope.coTeaching.coTeachers.length > 0 ){
                    $scope.teachersLihtboxTeacher =
                        _.reject($scope.teachersLihtboxTeacher,
                                teacher => {return _.findWhere($scope.coTeaching.coTeachers, {second_teacher_id : teacher.id}) != undefined});
                }

            }else{
                $scope.coTeaching = service.substituteTeacher;

                if(isUpdate){
                    $scope.isUpate = isUpdate;
                    $scope.coTeaching.selectedTeacher = _.findWhere(teachers,
                        {id : $scope.coTeaching.coTeachers[0].second_teacher_id});
                    $scope.coTeaching.classesCoteaching =  service.groups;
                }

            }

            $scope.classesLightboxAddTeacher = service.groups;
            $scope.errorAddCoteaching = {
                errorStartDate:false,
            }
        };


        $scope.createService= async (service) => {
            try {
                await service.createService($scope.classesSelected, $scope.lightboxes.create);
            } catch (e) {
                notify.error("evaluation.service.create.err")
            }
            $scope.lightboxes.create = false;
            await initServices();
        };

        $scope.changeSort = (nameSort) => {
            if($scope.sortBy === nameSort)
                $scope.sortByAsc = !$scope.sortByAsc;
            else
                $scope.sortByAsc = true;
            $scope.sortBy = nameSort;

        };
        $scope.checkErrorAddcoteaching = () => {

            let coTeaching = $scope.coTeaching
            if(moment(coTeaching.start_date).diff(coTeaching.end_date) === 0){
                coTeaching.end_date = moment(coTeaching.end_date).add(1,'day');
            }
            if( moment(coTeaching.entered_end_date).isBefore(coTeaching.end_date)){
                coTeaching.entered_end_date = coTeaching.end_date;
            }

            $scope.errorAddCoteaching.errorStartDate =
                moment(coTeaching.start_date).isAfter(coTeaching.end_date) ;

        };

        $scope.checkIfValid = () => {

            let coTeaching = $scope.coTeaching;
            let conditionsWithoutDates = (
                coTeaching &&
                coTeaching.newCoTeachers &&
                coTeaching.newCoTeachers.length > 0 &&
                coTeaching.classesCoteaching &&
                coTeaching.classesCoteaching.length > 0 &&
                coTeaching.mainTeacher &&
                coTeaching.subject) ? true : false;

            let condtionsDate = (
                !_.some($scope.errorAddCoteaching) &&
                coTeaching != undefined &&
                coTeaching.end_date != undefined &&
                coTeaching.start_date != undefined ) ? true : false;

            if (coTeaching != undefined && coTeaching.isCoteaching != undefined && coTeaching.isCoteaching){
                return !conditionsWithoutDates
            }else{
                return !(conditionsWithoutDates && condtionsDate)
            }

        };

        $scope.deleteSelection= (object, array) => {

            for (let i = 0; i < $scope.coTeaching[array].length; i++) {
                if ($scope.coTeaching[array][i] == object) {
                    $scope.coTeaching[array] = _.without($scope.coTeaching[array],
                        $scope.coTeaching[array][i]);
                }
            }
            if($scope.coTeaching[array].length === 0){
                if(object instanceof ServiceClasse) $scope.coTeaching.selectedClass = undefined;
                if(object instanceof Teacher) $scope.coTeaching.selectedTeacher = undefined;

            }
        };

        $scope.addCoTeachers = () => {
            if($scope.coTeaching.isCoteaching){
                utils.pushData($scope.coTeaching.selectedTeacher,$scope.coTeaching.newCoTeachers);
            }else{
                if($scope.coTeaching.newCoTeachers.length === 0){
                    $scope.coTeaching.newCoTeachers.push($scope.coTeaching.selectedTeacher);
                }else{
                    $scope.coTeaching.newCoTeachers.splice(_.first($scope.coTeaching.newCoTeachers), 1, $scope.coTeaching.selectedTeacher);
                }
            }

        }

        $scope.addCoTeaching = async () => {
            try{
                await $scope.coTeaching.addCoTeaching();
            }catch (e){
                notify.error("evaluation.coteaching.create.error");
            }
            $scope.lightboxes.addTeacher = false;
            await initServices();
        };

        $scope.deleteCoTeacher = async(multiTeaching, index) => {

                let ids = _.pluck(multiTeaching.coTeachers[index].idsAndIdsGroups, "id");
                await multiTeaching.deleteCoTeaching(ids);
                await initServices();

        };

        await $scope.init();
        utils.safeApply($scope);

    }
]);