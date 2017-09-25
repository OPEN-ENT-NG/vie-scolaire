/**
 * Created by agnes.lapeyronnie on 19/09/2017.
 */
import { notify } from 'entcore/entcore';
import {Responsable, Classe, Structure,} from '../eval_teacher_mdl';


export class LSU {
    responsables : Responsable[];
    classes : Classe[];
    structureId : string;

    constructor (structureId : string, classes : Classe[] = [], responsables : Responsable[] = []){
        this.structureId = structureId ;
        this.classes = classes ;
        this.responsables = responsables ;

    }

    export () {
        let url = "/viescolaire/evaluations/exportLSU/lsu?idStructure=" + this.structureId;

             for(var i=0 ; i < this.classes.length ; i++) {
                 url += "&idClasse=" + this.classes[i].id;
             }
            for(var i=0 ; i < this.responsables.length ; i++){
                url+="&idResponsable=" + this.responsables[i].id;
            }

        location.replace(url);
    }

}