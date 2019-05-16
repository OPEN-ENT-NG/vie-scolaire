import {notify} from 'entcore';
import http from 'axios';
import {Mix} from "toolkit";
import {Utils} from "../../utils/Utils";

export class TimeSlot {
    _id: string;
    name: string;
    schoolId: string

    constructor(id_structure?: string) {
        if (id_structure) this.schoolId = id_structure;
    }

    toJson() {
        return {
            id: this._id,
            id_structure: this.schoolId
        };
    }

    async save (): Promise<void> {
        let response = await http.post('/viescolaire/time-slots', this.toJson());
        return Utils.setToastMessage(response, 'viescolaire.save.time.slot.profil','viescolaire.error.sauvegarde');
    }

    async sync () {
        try {
            let {data} = await http.get(`/viescolaire/time-slot?structureId=${this.schoolId}`);
            for(let i= 0; i < data.length ; i++) {
                if (data[i].id !== undefined) {
                    this._id = data[i].id;
                }
            }
        } catch (e) {
            notify.error('viescolaire.error.sync.slot.profile');
        }
    }
}

export class TimeSlots {
    all: TimeSlot[];
    id: string;
    structure: string;

    constructor(id_structure?: string) {
        if (id_structure) this.structure = id_structure;
        this.all = [];
    }

    async syncAll () {
        try {
            let {data} = await http.get(`/viescolaire/time-slots?structureId=${this.structure}`);
            this.all = Mix.castArrayAs(TimeSlot, data);
        } catch (e) {
            notify.error('viescolaire.error.sync.time.slots');
        }
    }

}