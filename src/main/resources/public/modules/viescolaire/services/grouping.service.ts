import http, {AxiosResponse} from "axios";
import {Grouping, IGroupingItemResponse} from "../models/common/grouping";
import {ng} from "entcore";

export interface GroupingService {

    createGrouping(structureId: string, name: string): Promise<AxiosResponse>;

    updateGrouping(id: string, name: string): Promise<AxiosResponse>;

    deleteGrouping(id: string): Promise<AxiosResponse>;

    addGroupingAudience(id: string, studentDivisionId: string): Promise<AxiosResponse>;

    deleteGroupingAudience(id: string, studentDivisionId: string): Promise<AxiosResponse>;

    getGroupingList(structureId: string): Promise<Grouping[]>;
}

export const groupingService: GroupingService = {

    createGrouping: async (structureId: string, name: string): Promise<AxiosResponse> => {
        return http.post(`/viescolaire/grouping/structure/${structureId}`,{name : name});
    },

    updateGrouping: async (id: string, name: string): Promise<AxiosResponse> => {
        return http.put(`/viescolaire/grouping/${id}`,{name : name});
    },

    deleteGrouping: async (id: string): Promise<AxiosResponse> => {
        return http.delete(`/viescolaire/grouping/${id}`);
    },

    addGroupingAudience: async (id: string, studentDivisionId: string): Promise<AxiosResponse> => {
        return http.post(`/viescolaire/grouping/${id}/add`,{student_division_id : studentDivisionId});
    },

    deleteGroupingAudience: async (id: string, studentDivisionId: string): Promise<AxiosResponse> => {
        return http.delete(`/viescolaire/grouping/${id}/delete`,{data : {student_division_id : studentDivisionId}});
    },

    getGroupingList: async (structureId: string): Promise<Grouping[]> => {
        return http.get(`/viescolaire/grouping/structure/${structureId}/list`)
            .then((res: AxiosResponse) =>
                res.data.map((grouping: IGroupingItemResponse) => new Grouping().build(grouping))
            );
    },
}

export const GroupingService = ng.service('GroupingService', (): GroupingService => groupingService);