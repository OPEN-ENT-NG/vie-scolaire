import {ng} from 'entcore';
import { http, HttpResponse } from 'entcore-toolkit';
import {MementoAccess} from "../models/memento.model";

export interface IMementoService {
    saveComment(studentId: string, comment: string): Promise<HttpResponse>;

    updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<HttpResponse>;
    checkAccess(): Promise<MementoAccess>;
}

export const mementoService: IMementoService = {

    async saveComment(studentId: string, comment: string): Promise<HttpResponse> {
        return http.post(`/viescolaire/memento/students/${studentId}/comments`, {comment});
    },

    async updateRelativePriorities(studentId: string, relativeIds: Array<string>): Promise<HttpResponse> {
        return http.put(`/viescolaire/memento/students/${studentId}/relatives/priority`, {'relativeIds': relativeIds});
    },

    async checkAccess(): Promise<MementoAccess> {
        return http.get(`/viescolaire/memento/access`).then((res: HttpResponse) => res.data);
    },
};

export const MementoService = ng.service('MementoService', (): IMementoService => mementoService);
