import {ng} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {MementoAccess} from "../models/memento.model";

export interface IMementoService {
    saveComment(studentId: string, comment: string): Promise<AxiosResponse>;

    updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<AxiosResponse>;
    checkAccess(): Promise<MementoAccess>;
}

export const mementoService: IMementoService = {

    async saveComment(studentId: string, comment: string): Promise<AxiosResponse> {
        return http.post(`/viescolaire/memento/students/${studentId}/comments`, {comment});
    },

    async updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<AxiosResponse> {
        return http.put(`/viescolaire/memento/students/${studentId}/relatives/priority`, {'relativeIds': relativeIds});
    },

    async checkAccess(): Promise<MementoAccess> {
        return http.get(`/viescolaire/memento/access`).then((res: AxiosResponse) => res.data);
    },
};

export const MementoService = ng.service('MementoService', (): IMementoService => mementoService);
