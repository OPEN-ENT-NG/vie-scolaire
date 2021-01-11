import {ng} from 'entcore';
import http, {AxiosResponse} from 'axios';
import {IUser} from '../models/common/User';
import {IGroup} from '../models/common/Group';


export interface ISearchService {

    searchStudents(structureId: string, value: string): Promise<IUser[]>;

    searchGroups(structureId: string, value: string): Promise<IGroup[]>;
}

export const SearchService: ISearchService = {

    searchStudents: async (structureId: string, value: string): Promise<IUser[]> => {
        try {
            value = value.replace('\\s', '').toLowerCase();
            const {data}: AxiosResponse = await http.get(`/viescolaire/user/search?q=${value}&structureId=${structureId}&field=lastName&field=firstName&field=displayName&profile=Student`);
            data.forEach((user) => {
                if (user.idClasse && user.idClasse != null) {
                    let idClass = user.idClasse;
                    user.idClasse = idClass.map(id => id.split('$')[1]);
                    user.toString = () => user.displayName + ' - ' + user.idClasse;
                } else user.toString = () => user.displayName;
            });

            return data;
        } catch (err) {
            throw err;
        }
    },

    searchGroups: async (structureId: string, value: string): Promise<IGroup[]> => {
        try {
            value = value.replace('\\s', '').toLowerCase();
            const {data}: AxiosResponse = await http.get(`/viescolaire/group/search?q=${value}&structureId=${structureId}&field=name`);
            data.forEach((user) => {
                if (user.idClasse && user.idClasse != null) {
                    let idClass = user.idClasse;
                    user.idClasse = idClass.map(id => id.split('$')[1]);
                    user.toString = () => user.displayName + ' - ' + user.idClasse;
                } else user.toString = () => user.displayName;
            });

            return data;
        } catch (err) {
            throw err;
        }
    }
};

export const searchService = ng.service('SearchService', (): ISearchService => SearchService);