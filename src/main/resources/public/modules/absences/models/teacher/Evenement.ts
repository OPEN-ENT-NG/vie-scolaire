import {  http } from 'entcore/entcore';
import { DefaultEvenement } from '../common/DefaultEvenement';
import {IModel} from "../../../entcore/modelDefinitions";
export class Evenement extends DefaultEvenement implements IModel {

    get api () {
        return {
            POST : '/viescolaire/presences/evenement',
            PUT : '/viescolaire/presences/evenement',
            DELETE : '/viescolaire/presences/evenement?evenementId='
        };
    }

    create (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.POST, this).done((data) => {
                this.id = data.id;
                resolve({id : data.id, bool : true});
            });
        });
    }

    update (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.PUT, this).done((data) => {
                resolve({id : data.id, bool : false});
            });
        });
    }

    save (): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.id) {
                this.update().then((data) => {
                    resolve(data);
                });
            } else {
                this.create().then((data) => {
                    resolve(data);
                });
            }
        });
    }

    delete (): Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.DELETE + this.id).done(() => {
                this.id = undefined;
                resolve();
            });
        });
    }
}