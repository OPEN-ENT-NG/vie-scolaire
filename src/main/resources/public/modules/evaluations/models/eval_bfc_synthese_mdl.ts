import {model, http, IModel, Model, idiom as lang} from 'entcore/entcore';



export class BfcSynthese extends Model implements IModel {
    id: number;
    id_eleve: string;
    id_classe : string;
    id_cycle: number;
    texte: string;

    get api() {
        return {
            create: '/viescolaire/evaluations/BfcSynthese',
            update: '/viescolaire/evaluations/BfcSynthese?id=' + this.id,
            getBfcSynthese: '/viescolaire/evaluations/BfcSynthese?idEleve='
        }
    }

    constructor(id_eleve : string) {
        super();
        this.texte = "";
        this.id_eleve = id_eleve;
    }

    toJSON() {
        return {
            id_eleve: this.id_eleve,
            id_cycle : this.id_cycle,
            texte: this.texte
        }
    }

    createSynthese(): Promise<BfcSynthese> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.create, this.toJSON()).done((data) =>{
                this.id= data.id;
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
    updateSynthese(): Promise<BfcSynthese> {
        return new Promise((resolve,reject)=>{
            http().putJson(this.api.update,this.toJSON()).done((data)=>{
                if(resolve&&(typeof(resolve)==='function')){
                    resolve(data);
                }
            })
        })
    }

    saveSynthese():Promise<BfcSynthese> {
        return new Promise((resolve, reject) => {
            if(!this.id){
                this.createSynthese().then((data)=>{
                    resolve(data);
                });
            }else{
                this.updateSynthese().then((data)=>{
                    resolve(data);
                });
            }
        });
    }

    syncSynthese(): Promise<any> {
        return new Promise((resolve, reject) => {
            // var that = this;
            http().getJson(this.api.getBfcSynthese  + this.id_eleve ).done((data) => {
                if(data != {}){
                    this.updateData(data);
                }
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
}