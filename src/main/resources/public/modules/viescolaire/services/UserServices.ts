import {ng,notify} from 'entcore';
import http, {AxiosResponse} from 'axios';

export interface UserService {
    getTeachers(structureId: string): Promise<AxiosResponse>

}

export const userService: UserService = {
    async getTeachers  (idStructure) {
        try {
            return http.get(`/viescolaire/teachers?idEtablissement=${idStructure}`)
        } catch (e) {
            notify.error('evaluations.service.error.teacher');
        }
    },

};

export const UserService = ng.service('UserService', (): UserService => userService);
