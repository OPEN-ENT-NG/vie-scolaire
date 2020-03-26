

import {Mix, Selectable,Selection} from "entcore-toolkit";
import http from "axios";
import {toasts} from "entcore";
import {Utils} from "../../utils/Utils";

export class Service implements Selectable{
    id_etablissement: string;
    id_enseignant: string;
    id_groupe: string;
    id_matiere: string;
    nom_enseignant: object;
    nom_groupe: object;
    topicName: object;
    modalite: string;
    previous_modalite: string;
    evaluable: boolean;
    previous_evaluable: boolean;
    isManual: boolean;
    coefficient: number;
    selected:boolean;
    groups: any;
    groups_name:string;
    id_groups: any;
    groupsWithoutCoTeacher: any;
    subTopics: any;
    coTeachers; any;



    constructor(service) {
        _.extend(this, service);
        this.previous_modalite = this.modalite;
        this.previous_evaluable = this.evaluable;
        this.coefficient = (Utils.isNull(this.coefficient)? 1 : this.coefficient);
        this.groups = service.groups;
        this.groups_name = service.groups_name;
        this.subTopics = service.subTopics;
    }

    hasNullProperty(){
        return !this.nom_enseignant || !this.nom_groupe || !this.topicName;
    }

    async createService(classesSelected, creation){
        try {
            let {status} = await http.post('/viescolaire/service', this.toJson(classesSelected,creation));
            if(status === 200)
            {
                if(classesSelected.length >1)
                    toasts.confirm('evaluation.services.create');
                else
                    toasts.confirm('evaluation.service.create');
            }else {
                toasts.warning("evaluation.service.create.err")
            }
        } catch (e) {
            toasts.warning('evaluation.service.error.create');
        }
    }
    updateServiceModalite( classesSelected){

        let request = () => {
            try {
                return http.put('/viescolaire/service', this.toJson());
            } catch (e) {
                toasts.warning('evaluation.service.error.update');
            }
        };

        classesSelected = [this.id_groupe];

        if(this.modalite == this.previous_modalite) {
            return;
        } else {
            request().then(() => {
                this.previous_modalite = this.modalite;
            }, () => {
                this.modalite = this.previous_modalite;
            })
        }
    }
    updateServiceCoefficient () {
        try {
            return http.put('/viescolaire/service', this.toJson());
        } catch (e) {
            toasts.warning('evaluation.service.error.update');
        }
    }
    updateServiceEvaluable() {

        let request = () => {
            try {
                return http.put('/viescolaire/service', this.toJson());
            } catch (e) {
                toasts.warning('evaluation.service.error.update');
            }
        };

        if (this.evaluable == this.previous_evaluable) {
            return;
        } else {
            request().then(() => {
                this.previous_evaluable = this.evaluable;
            }, () => {
                this.evaluable = this.previous_evaluable;
            })
        }
    }

    deleteService(){
        try {
            return http.delete("/viescolaire/service"+
                `?id_matiere=${this.id_matiere}`+
                `&id_groupe=${this.id_groupe}`+
                `&id_enseignant=${this.id_enseignant}`);
        } catch (e) {
            toasts.warning('evaluation.service.error.delete');
        }
    }

    getDevoirsService(){
        try {
            return http.get("/competences/devoirs/service"+
                `?id_matiere=${this.id_matiere}`+
                `&id_groupe=${this.id_groupe}`+
                `&id_enseignant=${this.id_enseignant}`);
        } catch (e) {
            toasts.warning("evaluations.service.devoir.error");
        }
    }

    updateDevoirsService(devoirs, matiere) {
        try {
            return http.put("/competences/devoirs/service", {
                id_devoirs: devoirs,
                id_matiere: matiere
            });
        } catch (e) {
            toasts.warning('evaluations.service.devoir.update.error');
        }
    }

    deleteDevoirsService(devoirs) {
        try {
            return http.put("/competences/devoirs/delete", {
                id_devoirs: devoirs
            });
        } catch (e) {
            toasts.warning('evaluations.service.devoir.delete.error');
        }
    }

    public toJson(classesSelected?,creation?) {
        if(classesSelected && classesSelected.length == 0 || !creation)
            return {
                id_etablissement: this.id_etablissement,
                id_enseignant: this.id_enseignant,
                id_matiere: this.id_matiere,
                id_groupes: [this.id_groupe],
                modalite: this.modalite,
                evaluable: this.evaluable,
                coefficient: this.coefficient
            };
        else
            return {
                id_etablissement: this.id_etablissement,
                id_enseignant: this.id_enseignant,
                id_matiere: this.id_matiere,
                id_groupes: _.map(classesSelected,(classe) => {return classe.id;}),
                modalite: this.modalite,
                evaluable: this.evaluable,
                coefficient: this.coefficient
            };
    }

}

export class Services extends Selection<Service>{
    public getServices = (idStructure , filter?) =>{
        try {
            if(filter)
                return http.get(`/viescolaire/services?idEtablissement=${idStructure}&${filter}`);
            else{
                return http.get(`/viescolaire/services?idEtablissement=${idStructure}`);
            }
        } catch (e) {
            toasts.warning('evaluation.service.error.get');
        }
    };
}