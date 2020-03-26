import {ng,notify} from 'entcore';
import http, {AxiosResponse} from 'axios';

export interface SubjectService {
    getMatieres(structureId: string): Promise<AxiosResponse>
}

export const subjectService: SubjectService = {
    async  getMatieres (idStructure) {
        try {
            return http.get(`/viescolaire/matieres?idEtablissement=${idStructure}`)
        } catch (e) {
            notify.error('evaluations.service.error.matiere');
        }
    }
};

export const SubjectService = ng.service('SubjectService', (): SubjectService => subjectService);
