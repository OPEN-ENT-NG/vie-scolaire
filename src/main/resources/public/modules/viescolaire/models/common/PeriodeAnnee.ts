import {moment, notify} from 'entcore';
import http from 'axios';
import {Utils} from "../../utils/Utils";
import {Mix} from "toolkit";

export class PeriodeAnnee {
    id: number;
    start_date: string;
    end_date: string;
    structure: string;
    description: string;
    isOpening: boolean;
    isExist: boolean;
    code: string;
    loading: boolean;

    constructor(id_structure?: string) {
        this.loading = false;
        this.isExist = false;
        this.description = '';
        if (id_structure) this.structure = id_structure;
    }

    get api() {
        return {
            POST_INCLUSION: '/viescolaire/settings/periode',
            POST_EXCLUSION: '/viescolaire/settings/exclusion'
        };
    }

    setIsOpening(isOpening: boolean) {
        this.isOpening = isOpening;
    }

    isLoading (): boolean {
        return this.loading || false;
    }

    setIsExist(isExist: boolean) {
        this.isExist = isExist;
    }

    toJson() {
        return {
            id: this.id,
            description: this.description,
            start_date: moment(this.start_date).format('YYYY-MM-DD 00:00:00'),
            end_date: moment(this.end_date).format('YYYY-MM-DD 23:59:59'),
            id_structure: this.structure
        };
    }

    async sync () {
        try {
            let {data} = await http.get(`/viescolaire/settings/periode?structure=${this.structure}`);
            if(data.id !== undefined) {
                this.id = data.id;
                this.start_date = data.start_date;
                this.end_date = data.end_date;
                this.code = data.code;
                this.isOpening = data.isOpening;
            }
        } catch (e) {
            notify.error('viescolaire.error.sync');
        }
    }

    async save (): Promise<void> {
        if (this.id) {
            return await this.update();
        } else {
            return await this.create();
        }
    }

    async create (): Promise<void> {
        if (this.isOpening) {
            let response = await http.post(this.api.POST_INCLUSION, this.toJson());
            return Utils.setToastMessage(response, 'viescolaire.creation.periode.annee.scolaire','viescolaire.error.sauvegarde');
        }
        else {
            notify.error("viescolaire.error.sauvegarde")
        }
    }

    async update (): Promise<void> {
        if (this.isOpening) {
            let response = await http.put(`/viescolaire/settings/periode/${this.id}`, this.toJson());
            return Utils.setToastMessage(response, 'viescolaire.modification.periode.annee.scolaire','viescolaire.error.sauvegarde');
        }
        else {
            notify.error("viescolaire.error.update")
        }
    }

}



