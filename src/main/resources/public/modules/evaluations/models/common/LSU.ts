/**
 * Created by agnes.lapeyronnie on 19/09/2017.
 */
import { notify,Collection } from 'entcore/entcore';
import {Responsable, Classe, Structure} from '../eval_teacher_mdl';


export class LSU {
    responsables :Collection<Responsable>;
    classes : Collection<Classe>;
    structureId : string;


    constructor (structureId : string, classes : Collection<Classe>, responsables : Collection<Responsable>){
        this.structureId = structureId ;
        this.classes = _.clone(classes);
        this.responsables =_.clone(responsables) ;

    }

    export () {
        let url = "/viescolaire/evaluations/exportLSU/lsu?idStructure=" + this.structureId;

            for(var i=0; i<this.classes.all.length;i++) {
                if(this.classes.all[i].selected){
                    url += "&idClasse=" + this.classes.all[i].id;
                }
            }

         /*   _.each(_.where(this.classes.all, {selected: true}), (classe) => {
                    url += "&idClasse=" + this.classes.all[i].id;

            });*/

            for(var i=0 ; i < this.responsables.all.length ; i++){
                if(this.responsables.all[i].selected){
                url+="&idResponsable=" + this.responsables.all[i].id;
                }
            }

        location.replace(url);
    }

}