import {notify} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {Mix} from "toolkit";
import {Utils} from "../../utils/Utils";
import {Classe} from "../personnel/Classe";
import {DefaultClasse} from "./DefaultClasse";
import {Structure} from "../personnel/Structure";
import {timeslotClasseService} from "../../services/TimeslotClasseService";

export interface Slot {
    id: string;
    name: string;
    startHour: string;
    endHour: string;
}

export interface TimeslotClass {
    timeSlot: TimeSlot;
    classes: Classe[];
    savedClasses: Classe[];
    errorClasses: Classe[];
}

export class TimeSlot {
    _id: string;
    name: string;
    schoolId: string;
    endOfHalfDay: string;
    slots: Slot[];
    classes: string[]

    constructor(id_structure?: string) {
        if (id_structure) this.schoolId = id_structure;
        this.classes = []
    }

    toJson() {
        return {
            id: this._id,
            id_structure: this.schoolId
        };
    }

    async save(): Promise<AxiosResponse> {
        let response = await http.post('/viescolaire/time-slots', this.toJson());
        return Utils.setToastMessage(response, 'viescolaire.save.time.slot.profil','viescolaire.error.sauvegarde');
    }

    async saveEndHalfDay(): Promise<AxiosResponse> {
        let bodyRequest = {time: this.endOfHalfDay, structureId: this.schoolId};
        let response = await http.put(`/viescolaire/time-slots?id=${this._id}`, bodyRequest);
        return Utils.setToastMessage(response, 'viescolaire.save.end.of.half.day','viescolaire.error.sauvegarde');
    }

    async syncClasseAssociation() {
        try {
            this.classes = await timeslotClasseService.getAllClassFromTimeslot(this._id);
        } catch (e) {
            notify.error('viescolaire.error.sync.time.slots');
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

    async syncAll() {
        try {
            let {data} = await http.get<TimeSlot[]>(`/viescolaire/time-slots?structureId=${this.structure}`);
            this.all = Mix.castArrayAs(TimeSlot, Utils.toCamelCase(data));
        } catch (e) {
            notify.error('viescolaire.error.sync.time.slots');
        }
    }
}