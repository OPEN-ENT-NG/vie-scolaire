import {ng,notify} from 'entcore';
import http, {AxiosResponse} from 'axios';

export interface GroupService {
    getClasses(structureId: string): Promise<AxiosResponse>
}

export const groupService: GroupService = {
     async  getClasses  (idStructure) {
        try {
            return http.get(`/viescolaire/classes?idEtablissement=${
                idStructure}&forAdmin=true&classOnly=true`);
        } catch (e) {
            notify.error('evaluations.service.error.classe');
        }
    }
};

export const GroupService = ng.service('GroupService', (): GroupService => groupService);
