import {ng} from 'entcore';
import http, {AxiosResponse} from 'axios';

export interface IMementoService {
    saveComment(studentId: string, comment: string): Promise<AxiosResponse>;

    updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<AxiosResponse>;
}

export const mementoService: IMementoService = {

    async saveComment(studentId: string, comment: string): Promise<AxiosResponse> {
        return http.post(`/viescolaire/memento/students/${studentId}/comments`, {comment});
    },

    async updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<AxiosResponse> {
        return http.put(`/viescolaire/memento/students/${studentId}/relatives/priority`, {'relativeIds': relativeIds});
    }
};

export const MementoService = ng.service('MementoService', (): IMementoService => mementoService);
