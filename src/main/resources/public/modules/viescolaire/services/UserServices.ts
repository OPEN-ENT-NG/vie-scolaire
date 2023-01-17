import {ng, notify} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {IUser} from '../models/common/User';



declare const window: any;

export interface UserService {
    getTeachers(structureId: string): Promise<AxiosResponse>;

    getStudents(structureId: string, page?: number, studentId?: Array<string>, groupName?: Array<string>, crossFilter?: boolean): Promise<Array<IUser>>;
}

export const userService: UserService = {
    async getTeachers (idStructure: string): Promise<AxiosResponse> {
        try {
            return http.get(`/viescolaire/teachers?idEtablissement=${idStructure}`);
        } catch (e) {
            notify.error('evaluations.service.error.teacher');
        }
    },

    async getStudents(structureId: string, page?: number, studentId?: Array<string>, groupName?: Array<string>, crossFilter?: boolean): Promise<Array<IUser>> {
        let url: URL = new URL(window.location);

        if (crossFilter !== undefined && crossFilter !== null) {
            url.searchParams.append('crossFilter', crossFilter.toString());
        }

        if (page !== undefined && page !== null) {
            url.searchParams.append('page', page.toString());
        }

        if (studentId.length > 0) {
            let studentParams: string = '';

            for (let i = 0 ; i < studentId.length; i++) {
                studentParams += studentId[i];
                if (i !== studentId.length - 1) {
                    studentParams += ',';
                }
            }
            url.searchParams.append('studentId', studentParams);
        }

        if (groupName.length > 0) {
            let groupParams: string = '';

            for (let j = 0 ; j < groupName.length; j++) {
                groupParams += groupName[j];
                if (j !== groupName.length - 1) {
                    groupParams += ',';
                }
            }
            url.searchParams.append('groupName', groupParams);
        }
        let pathName: string = `/viescolaire/structures/${structureId}/students`;

        return http.get(pathName + url.search).then((res: AxiosResponse) => {
            return res.data;
        });
    }

};

export const UserService = ng.service('UserService', (): UserService => userService);
