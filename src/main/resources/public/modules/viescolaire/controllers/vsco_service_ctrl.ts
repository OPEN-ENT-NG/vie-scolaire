import {notify, idiom as lang, ng, _, toasts, moment, workspace} from 'entcore';
import * as utils from '../../utils/services';
import {TypeSubTopic, Service, Services, TypeSubTopics, ServiceClasse, MultiTeaching} from "../models/services";
import {safeApply} from "../../utils/services";
import {SubjectService,GroupService,UserService} from "../services";
import {Teacher} from "../models/common/Teacher";
import service = workspace.v2.service;

export let evalAcuTeacherController = ng.controller('ServiceController',[
    '$scope','SubjectService','UserService','GroupService',
    async function ($scope,subjectService:SubjectService,userServices:UserService,groupServices:GroupService ) {
        $scope.sortBy = "topicName";
        $scope.sortByAsc = true;
        $scope.filter = "classes=true&groups=true&manualGroups=true";
        $scope.servicesTemp = new Services([]);
        function getGroupsName(service, groups) {
            if(service.id_groups && service.id_groups.length > 0){
                service.id_groups.forEach(id => {
                        let group = _.findWhere($scope.columns.classe.data, {id: id});
                        if(group && !groups.includes(group))
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
                return groups.join(", ");
            }
            else {
                let group = _.findWhere($scope.columns.classe.data, {id: service.id_groupe});
                if(group && !groups.includes(group)){
                    groups.push(group);
                    return group.name;
                }
            }
            return null;
        }

        function setServices(data) {
            $scope.services = _.reject(_.map(data, service => {
                let enseignant = _.findWhere($scope.columns.enseignant.data, {id: service.id_enseignant});
                let groupe = _.findWhere($scope.columns.classe.data, {id: service.id_groupe});
                let groups = [];
                let subTopics = [];
                let coTeachers = service.coTeachers;
                let substituteTeachers = service.substituteTeachers;
                let matiere = _.findWhere($scope.columns.matiere.data, {id: service.id_matiere});
                if (matiere && matiere.sous_matieres && matiere.sous_matieres.length > 0)
                    matiere.sous_matieres.forEach(sm => {
                        $scope.subTopics.all.map(sb => {
                            if (sm.id_type_sousmatiere == sb.id) {
                                subTopics.push(sb)
                            }
                        });
                    });
                let groups_name = getGroupsName(service, groups);

                let coTeachers_name = "";
                let substituteTeachers_name = "";

                if(coTeachers){
                    _.each(coTeachers , (coTeacher) => {
                        coTeacher.displayName = (_.findWhere($scope.columns.enseignant.data,
                            {id: coTeacher.second_teacher_id}) != undefined)? _.findWhere($scope.columns.enseignant.data,
                            {id: coTeacher.second_teacher_id}).displayName : "";
                        coTeachers_name += coTeacher.displayName + " ";
                    });
                }
                if(substituteTeachers){
                    _.each(substituteTeachers , (substituteTeacher) => {
                        substituteTeacher.displayName = (_.findWhere($scope.columns.enseignant.data,
                            {id: substituteTeacher.second_teacher_id}) != undefined)? _.findWhere($scope.columns.enseignant.data,
                            {id: substituteTeacher.second_teacher_id}).displayName : "";
                        substituteTeachers_name += substituteTeacher.displayName + " ";
                    });
                }

                service = _.omit(service,'coTeachers','substituteTeacher');
                let missingParams = {
                    id_etablissement: $scope.idStructure,
                    nom_enseignant: enseignant ? enseignant.displayName : null,
                    topicName: matiere ? matiere.name  + " (" + matiere.externalId + ")"  : null,
                    nom_groupe: groupe ? groupe.name : null,
                    groups: groups ? groups : null,
                    groups_name: groups_name ? groups_name : null,
                    coTeachers_name : coTeachers_name,
                    substituteTeachers_name : substituteTeachers_name,
                    subTopics: subTopics ? subTopics : [],
                    coTeachers: (_.isEmpty(coTeachers))? [] : _.map(coTeachers, (coTeacher) => { return new MultiTeaching(coTeacher) }) ,
                    substituteTeacher:  (_.isEmpty(substituteTeachers))? [] : _.map(substituteTeachers, (substituteTeacher) => { return new MultiTeaching(substituteTeacher) })
                };
                return new Service(_.defaults(service, missingParams));
            }), service => service.hasNullProperty());
        }

        async function getAndSetServices() {
            if(!$scope.displayMessageLoader)
                await $scope.runArrayLoader();

            $scope.servicesTemp.getServices($scope.structure.id, $scope.filter).then(async ({data}) => {
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
            await $scope.runArrayLoader();
            getAndSetServices();
            $scope.classesSelected = [];
        }

        $scope.filterSearch = () => {
            return (service) => {
                let isInClassSearched = false;
                let isInSearched = true;
                if($scope.searchToFilter.length > 0) {
                    for(let search of $scope.searchToFilter) {
                        isInSearched = (service.nom_enseignant.toUpperCase().includes(search.toUpperCase())
                            || service.topicName.toUpperCase().includes(search.toUpperCase())
                            || service.coTeachers_name.toUpperCase().includes(search.toUpperCase())
                            || service.substituteTeachers_name.toUpperCase().includes(search.toUpperCase()));
                        if(!isInSearched)
                            break;
                    }
                }
                if($scope.searchForClasse.length > 0) {
                    let classesSearched = [];
                    for(let search of $scope.searchForClasse) {
                        service.groups.forEach(group => {
                            if(group.name.toUpperCase().includes(search.toUpperCase())
                                && !_.contains(classesSearched, group)){
                                classesSearched.push(group);
                                isInClassSearched = true;
                            }
                        });
                    }
                    if(classesSearched.length > 0) {
                        service.id_groups = classesSearched.map(classe => classe.id);
                        service.groups = [];
                        service.groups_name = getGroupsName(service, service.groups);
                    }
                }
                else {
                    isInClassSearched = true;
                }
                return isInClassSearched && isInSearched;
            }
        };

        $scope.deploySubtopics = (service) =>{
            if(service.subTopics.length != 0)
                service.deploy = !service.deploy
        };

        $scope.saveSearch = async (event) => {
            if (event && (event.which === 13 || event.keyCode === 13) && event.target.value.length > 0) {
                let value = event.target.value.trim().toUpperCase();
                let searchAdded = false;
                $scope.services.forEach(service => {
                    if(!searchAdded) {
                        if(service.groups_name != null && service.groups_name.toUpperCase().includes(value) ||
                            service.nom_groupe != null && service.nom_groupe.toUpperCase().includes(value)){
                            if(!_.contains($scope.searchForClasse, value)){
                                $scope.searchForClasse.push(value);
                                searchAdded = true;
                            }
                        }
                    }
                });
                if (!searchAdded && !_.contains($scope.searchToFilter, value)){
                    $scope.searchToFilter.push(value);
                }
                await initServices();
                event.target.value = '';
            }
        };

        $scope.dropSearchFilter = async (search) => {
            $scope.searchToFilter = _.without($scope.searchToFilter, search);
            await initServices();
        };

        $scope.dropSearchClass = async (search) => {
            $scope.searchForClasse = _.without($scope.searchForClasse, search);
            await initServices();
        };

        $scope.init = async () =>{
            $scope.idStructure = $scope.structure.id;
            $scope.services = [];
            $scope.searchToFilter = [];
            $scope.searchForClasse = [];
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
                deleteService: false,
            };
            await $scope.runMessageLoader();
            $scope.classesSelected = [];

            Promise.all([groupServices.getClasses($scope.idStructure),
                subjectService.getMatieres($scope.idStructure),
                userServices.getTeachers($scope.idStructure),
                $scope.servicesTemp.getServices($scope.structure.id, $scope.filter),
                $scope.subTopics.get($scope.idStructure)
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

        $scope.updateFilter = (selectedHeader)  =>{
            selectedHeader.isSelected = !selectedHeader.isSelected;
            let filtersArray = _.values($scope.typeGroupes);
            if(filtersArray.length != 0){
                $scope.filter = "";
                filtersArray.forEach(filter =>{
                    $scope.filter += filter.filterName + "=" + filter.isSelected + "&";
                });
                $scope.filter = $scope.filter.substring(0, $scope.filter.length - 1);
            }
            getAndSetServices();
        };

        $scope.checkIfExistsAndValid =  (service) =>{
            let exist = false;
            if($scope.classesSelected && $scope.classesSelected.length > 0 && service.id_matiere != undefined &&
                service.id_matiere != "" && service.id_enseignant != "") {
                $scope.classesSelected.forEach(classe => {
                    if (_.findWhere($scope.services, {
                        id_matiere: service.id_matiere, id_enseignant: service.id_enseignant,
                        id_groupe: classe
                    }))
                        exist = true;
                });
                return exist;
            } else {
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
                subTopic.id_structure = $scope.structure.id;
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

        $scope.tryDeleteService = (service) => {
            $scope.service = service;
            $scope.lightboxes.deleteService = true;
        }

        $scope.deleteService = async () => {
            await $scope.checkDevoirsService($scope.service, async () => {
                await $scope.service.deleteService();
                $scope.lightboxes.deleteService = false;
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

        $scope.createService = async (service) => {
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
            $scope.service = new Service(
                {
                    id_etablissement: $scope.idStructure,
                    id_matiere: "",
                    id_enseignant: "",
                    id_groupe: "",
                    topicName: "",
                    nom_enseignant: "",
                    nom_groupe: "",
                    is_manual: true,
                    evaluable: false
                });
            $scope.lightboxes.create = true;
        };

//MultiTeaching
        $scope.openAddTeacherLightbox = (service, isCoteaching, substituteTeacherUpdated ?) => {
            $scope.lightboxes.addTeacher = true;

            let teachers = $scope.columns.enseignant.data;
            let mainTeacher = _.findWhere($scope.columns.enseignant.data, {id : service.id_enseignant});
            let subject = _.findWhere($scope.columns.matiere.data, {id : service.id_matiere});
            let multiTeaching = {
                structure_id : service.id_etablissement,
                main_teacher_id : mainTeacher.id,
                subject_id : subject.id,
                isCoteaching : isCoteaching
            }
            $scope.multiTeaching = new MultiTeaching(multiTeaching);
            $scope.multiTeaching.mainTeacher = mainTeacher;
            $scope.multiTeaching.subject = subject;
            $scope.multiTeaching.newCoTeachers = [];
            $scope.multiTeaching.competencesParams = service.competencesParams;
            $scope.multiTeaching.evaluable = service.evaluable;

            $scope.teachersLihtboxTeacher = _.reject(teachers, teacher => {return teacher.id == service.id_enseignant})
            if(service.coTeachers.length > 0){
                $scope.teachersLihtboxTeacher = _.reject(teachers, teacher =>
                {return _.findWhere(service.coTeachers, {second_teacher_id : teacher.id}) != undefined});
            }
            /*if(service.substituteTeachers.length > 0){
                $scope.teachersLihtboxTeacher = _.reject(teachers, teacher =>
                {return _.findWhere(service.substituteTeachers, {second_teacher_id : teacher.id}) != undefined});
            }*/

            $scope.multiTeaching.classesCoteaching = [];
            $scope.classesLightboxAddTeacher = service.groups;
            $scope.isUpdate = substituteTeacherUpdated != undefined;
            if(substituteTeacherUpdated){
                $scope.multiTeaching.oldSubstituteTeacher = substituteTeacherUpdated;
                $scope.multiTeaching.selectedTeacher = _.findWhere(teachers,
                    {id : $scope.multiTeaching.oldSubstituteTeacher.second_teacher_id});
                $scope.multiTeaching.newCoTeachers.push($scope.multiTeaching.selectedTeacher);
                $scope.multiTeaching.classesCoteaching = service.groups;
                $scope.multiTeaching.start_date = new Date(substituteTeacherUpdated.start_date);
                $scope.multiTeaching.end_date = new Date(substituteTeacherUpdated.end_date);
                $scope.multiTeaching.entered_end_date = new Date(substituteTeacherUpdated.entered_end_date);
            }

            $scope.warningClassesNonEvaluables = "";
            $scope.errorAddCoteaching = {
                errorStartDate:false,
            }
        };

        $scope.checkErrorAddCoTeaching = () => {
            let coTeaching = $scope.multiTeaching;
            if(moment(coTeaching.start_date).diff(coTeaching.end_date) === 0){
                coTeaching.end_date = moment(coTeaching.end_date).add(1,'day');
            }
            if(moment(coTeaching.entered_end_date).isBefore(coTeaching.end_date)){
                coTeaching.entered_end_date = coTeaching.end_date;
            }
            $scope.errorAddCoteaching.errorStartDate = moment(coTeaching.start_date).isAfter(coTeaching.end_date);
        };

        $scope.checkIfValid = () => {
            let multiTeaching = $scope.multiTeaching;

            let conditionsWithoutDates = !!(
                multiTeaching &&
                multiTeaching.newCoTeachers &&
                multiTeaching.newCoTeachers.length > 0 &&
                multiTeaching.classesCoteaching &&
                multiTeaching.classesCoteaching.length > 0 &&
                multiTeaching.mainTeacher &&
                multiTeaching.subject &&
                $scope.warningClassesNonEvaluables.length === 0
            );

            let condtionsDate = (
                !_.some($scope.errorAddCoteaching) &&
                multiTeaching != undefined &&
                multiTeaching.end_date != undefined &&
                multiTeaching.start_date != undefined);

            if (multiTeaching != undefined && multiTeaching.isCoteaching != undefined && multiTeaching.isCoteaching){
                return !conditionsWithoutDates;
            }else{
                if($scope.isUpdate){
                    return !($scope.disabledDeleteButton() && conditionsWithoutDates && condtionsDate);
                }else{
                    return !(conditionsWithoutDates && condtionsDate);
                }
            }
        };

        $scope.disabledDeleteButton = ()=> {
            //newCoteacher est vide ou le newCoTeacher = coTeacher
            let multiTeaching = $scope.multiTeaching;

            if(multiTeaching){
                let compareTeacher = !!(_.isEmpty(multiTeaching.newCoTeachers) || (multiTeaching.oldSubstituteTeacher != undefined &&
                    multiTeaching.oldSubstituteTeacher.second_teacher_id === multiTeaching.newCoTeachers[0].id));

                let idsClasseCoteachers = (multiTeaching.oldSubstituteTeacher && multiTeaching.oldSubstituteTeacher.idsAndIdsGroups) ?
                    _.pluck(multiTeaching.oldSubstituteTeacher.idsAndIdsGroups,"idGroup") : [];
                let idsClassSeleted = (multiTeaching.classesCoteaching)?_.pluck(multiTeaching.classesCoteaching,"id") : [];
                let compareClasses = !!(_.isEmpty(_.difference(idsClasseCoteachers, idsClassSeleted)));

                let compareDates = (multiTeaching.start_date.getTime() === new Date(multiTeaching.oldSubstituteTeacher.start_date).getTime()
                    && multiTeaching.end_date.getTime() === new Date(multiTeaching.oldSubstituteTeacher.end_date).getTime()
                    && multiTeaching.entered_end_date.getTime() === new Date(multiTeaching.oldSubstituteTeacher.entered_end_date).getTime());

                return !(compareTeacher && compareClasses && compareDates);
            } else {
                return true;
            }
        };


        $scope.deleteSelection = (object, array) => {
            for (let i = 0; i < $scope.multiTeaching[array].length; i++) {
                if ($scope.multiTeaching[array][i] == object) {
                    $scope.multiTeaching[array] = _.without($scope.multiTeaching[array],
                        $scope.multiTeaching[array][i]);
                }
            }
            if($scope.multiTeaching[array].length === 0){
                if(object instanceof ServiceClasse) $scope.multiTeaching.selectedClass = undefined;
                if(object instanceof Teacher) $scope.multiTeaching.selectedTeacher = undefined;
            }
            $scope.checkClassesEvaluables();
        };

        $scope.addCoTeachers = () => {
            if($scope.multiTeaching.isCoteaching){
                utils.pushData($scope.multiTeaching.selectedTeacher,$scope.multiTeaching.newCoTeachers);
            }else{
                if($scope.multiTeaching.newCoTeachers.length === 0){
                    $scope.multiTeaching.newCoTeachers.push($scope.multiTeaching.selectedTeacher);
                }else{
                    $scope.multiTeaching.newCoTeachers.splice(_.first($scope.multiTeaching.newCoTeachers), 1, $scope.multiTeaching.selectedTeacher);
                }
            }
        }

        $scope.addCoTeaching = async (newCoTeachers, classesCoteaching) => {
            if($scope.isUpdate){
                $scope.isUpdate = false;
                console.log(newCoTeachers);
                console.log(classesCoteaching);
                console.log($scope.multiTeaching.oldSubstituteTeacher);
                await $scope.multiTeaching.updateSubstituteTeacher(newCoTeachers, classesCoteaching,
                    $scope.multiTeaching.oldSubstituteTeacher);
            }else{
                await $scope.multiTeaching.addCoTeaching(newCoTeachers, classesCoteaching);
            }

            delete $scope.multiTeaching;
            $scope.lightboxes.addTeacher = false;
            await initServices();
        };

        $scope.deleteCoTeacher = async(oldSubstituteTeacherOrCoteacher) => {
            if(!oldSubstituteTeacherOrCoteacher.isCoteaching){
                $scope.lightboxes.addTeacher = false;
                $scope.isUpdate = false;
            }
            await new MultiTeaching(oldSubstituteTeacherOrCoteacher).deleteCoTeaching();
            delete $scope.multiTeaching;
            await initServices();
        };

        $scope.checkClassesEvaluables = () => {
            if($scope.multiTeaching != undefined) {
                let classesNonEvaluables = _.filter($scope.multiTeaching.classesCoteaching, (classe) => {
                    let competencesParams = _.findWhere($scope.multiTeaching.competencesParams, {id_groupe: classe.id});
                    return competencesParams ? !competencesParams.evaluable : !$scope.multiTeaching.evaluable;
                });
                if (classesNonEvaluables.length > 0) {
                    $scope.warningClassesNonEvaluables = lang.translate("viescolaire.service.warning.classesNonEvaluables")
                        + classesNonEvaluables.map(c => c.name).join(", ");
                } else {
                    $scope.warningClassesNonEvaluables = "";
                }
            }
        };

        $scope.filterValidDateSubstituteTeacher = (substituteTeacher) => {
            return moment(new Date()).isBetween(moment(substituteTeacher.start_date),
                moment(substituteTeacher.entered_end_date), 'days', '[]');
        };

        await $scope.init();
        utils.safeApply($scope);
    }
]);