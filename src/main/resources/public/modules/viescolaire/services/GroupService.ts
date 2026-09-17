import {ng,notify} from 'entcore';
import { http, HttpResponse } from 'entcore-toolkit';

export interface GroupService {
    getClasses(structureId: string): Promise<HttpResponse>
}

export const groupService: GroupService = {
     async  getClasses  (idStructure) {
        try {
            return http.get(`/viescolaire/classes?idEtablissement=${
                idStructure}&forAdmin=true`);
        } catch (e) {
            notify.error('evaluations.service.error.classe');
        }
    }
};

export const GroupService = ng.service('GroupService', (): GroupService => groupService);
