import {ng} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {IFailure, IReport} from '../models/trombinoscope';

export interface ITrombinoscopeService {
    importTrombinoscope(structureId: string, file: File): Promise<AxiosResponse>;

    updateTrombinoscope(structureId: string, studentId: string, file: File): Promise<AxiosResponse>;

    deleteTrombinoscope(structureId: string, studentId: string): Promise<AxiosResponse>;

    getFailures(structureId: string): Promise<Array<IFailure>>;

    linkTrombinoscope(structureId: string, studentId: string, pictureId: string): Promise<AxiosResponse>;

    setStructureSettings(structureId: string, useAvatar: boolean): Promise<AxiosResponse>;

    getStructureSettings(structureId: string): Promise<boolean>;

    getReports(structureId: string, limit: number, offset: number): Promise<Array<IReport>>;

    deleteFailuresHistory(structureId: string): Promise<AxiosResponse>;
}

export const trombinoscopeService: ITrombinoscopeService = {

    /**
     * Import trombinoscope zip file
     *
     * @param structureId   structure identifier
     * @param file          trombinoscope file
     */
    importTrombinoscope: async (structureId: string, file: File): Promise<AxiosResponse> => {
        const formData = new FormData();
        formData.append('file', file);
        return http.post(`/viescolaire/structures/${structureId}/trombinoscope`, formData);
    },

    /**
     * Update a student picture.
     *
     * @param structureId   structure identifier
     * @param studentId     student identifier
     * @param file          picture file
     */
    updateTrombinoscope: async (structureId: string, studentId: string, file: File): Promise<AxiosResponse> => {
        const formData: FormData = new FormData();
        const headers = {'headers': {'Content-type': 'multipart/form-data'}};

        formData.append('file', file);

        return http.put(`/viescolaire/structures/${structureId}/students/${studentId}/trombinoscope`, formData, headers);
    },

    /**
     * delete student picture (its trombinoscope).
     *
     * @param structureId   structure identifier
     * @param studentId     student identifier
     */
    deleteTrombinoscope: async (structureId: string, studentId: string) => {
        return http.delete(`/viescolaire/structures/${structureId}/students/${studentId}/trombinoscope`);
    },

    /**
     * Get trombinoscope failures.
     *
     * @param structureId   structure identifier
     */
    getFailures(structureId: string): Promise<Array<IFailure>> {
        return http.get(`/viescolaire/structures/${structureId}/trombinoscope/failures`)
            .then((res: AxiosResponse) => {
            return res.data.all;
        });
    },

    /**
     * Link failure picture to a student
     *
     * @param structureId   structure identifier
     * @param studentId     student identifier
     * @param pictureId     picture identifier
     */
    linkTrombinoscope: async (structureId: string, studentId: string, pictureId: string): Promise<AxiosResponse> => {
        return http.post(`/viescolaire/structures/${structureId}/students/${studentId}/trombinoscope`, { pictureId: pictureId});
    },

    /**
     * Set structure trombinoscope setting.
     * @param structureId   structure identifier
     * @param useAvatar     enable/diable use of trombinoscope images
     */
    setStructureSettings: async (structureId: string, useAvatar: boolean): Promise<AxiosResponse> => {
        return http.post(`/viescolaire/structures/${structureId}/trombinoscope/setting`, {active: useAvatar});
    },

    /**
     * Get structure trombinoscope settings.
     * @param structureId   structure identifier
     */
    getStructureSettings: (structureId: string): Promise<boolean> => {
        return http.get(`/viescolaire/structures/${structureId}/trombinoscope/setting`)
            .then((res: AxiosResponse) => {
                return res.data.active;
            });
    },

    /**
     * Get reports from structure.
     * @param structureId   structure identifier
     * @param limit
     * @param offset
     */
    getReports: (structureId: string, limit: number, offset: number): Promise<Array<IReport>> => {
        return http.get(`/viescolaire/structures/${structureId}/trombinoscope/reports?limit=${limit}&offset=${offset}`)
            .then((res: AxiosResponse) => {
                return res.data.all;
            });
    },

    /**
     * Clear failures history
     *
     * @param structureId   structure identifier
     */
    deleteFailuresHistory: (structureId: string): Promise<AxiosResponse> => {
        return http.delete(`/viescolaire/structures/${structureId}/trombinoscope/failures`);
    }

};

export const TrombinoscopeService = ng.service('TrombinoscopeService', (): ITrombinoscopeService => trombinoscopeService);
