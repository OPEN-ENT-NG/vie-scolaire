import http, {AxiosResponse} from "axios";
import {Grouping} from "../models/common/Grouping";
import {Mix} from "entcore-toolkit";
import {ng} from "entcore";

export interface GroupingService {

    createGrouping(structureId: string, name: string): Promise<AxiosResponse>;

    updateGrouping(id: string, name: string): Promise<AxiosResponse>;

    deleteGrouping(id: string): Promise<AxiosResponse>;

    addGroupingAudience(id: string, classOrGroupId: string): Promise<AxiosResponse>;

    deleteGroupingAudience(id: string, classOrGroupId: string): Promise<AxiosResponse>;

    getGroupingList(): Promise<Grouping[]>;
}

export const groupingService: GroupingService = {

    createGrouping: async (structureId: string, name: string): Promise<AxiosResponse> => {

        return {config: undefined, data: {id: 4}, headers: undefined, request: undefined, status: 200, statusText: ""};
        //return http.post(`/viescolaire/grouping/structure/${structureId}`, name);
    },

    updateGrouping: async (id: string, name: string): Promise<AxiosResponse> => {
        return {config: undefined, data: {id: 4}, headers: undefined, request: undefined, status: 200, statusText: ""};
        //return http.put(`/viescolaire/grouping/${id}`, name);
    },

    deleteGrouping: async (id: string): Promise<AxiosResponse> => {
        return {config: undefined, data: {id: 4}, headers: undefined, request: undefined, status: 200, statusText: ""};
        //return http.delete(`/viescolaire/grouping/${id}`);
    },

    addGroupingAudience: async (id: string, classOrGroupId: string): Promise<AxiosResponse> => {
        return {config: undefined, data: {id: 4}, headers: undefined, request: undefined, status: 200, statusText: ""};
        //return http.post(`/viescolaire/grouping/${id}/add`, classOrGroupId);
    },

    deleteGroupingAudience: async (id: string, classOrGroupId: string): Promise<AxiosResponse> => {
        return {config: undefined, data: {id: 4}, headers: undefined, request: undefined, status: 200, statusText: ""};
        //return http.put(`/viescolaire/grouping/${id}/delete`, classOrGroupId);
    },

    getGroupingList: async (): Promise<Grouping[]> => {
        try {
            let {data} = await http.get(`/viescolaire/grouping/list/`);
            return Mix.castArrayAs(Grouping, data);
        } catch (err) {
            throw err;
        }
    },
}

export const GroupingService = ng.service('GroupingService', (): GroupingService => groupingService);