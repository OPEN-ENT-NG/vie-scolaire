import { http, HttpResponse } from 'entcore-toolkit';
import {Grouping, IGroupingItemResponse} from "../models/common/grouping";
import {ng} from "entcore";

export interface GroupingService {

    createGrouping(structureId: string, name: string): Promise<HttpResponse>;

    updateGrouping(id: string, name: string): Promise<HttpResponse>;

    deleteGrouping(id: string): Promise<HttpResponse>;

    addGroupingAudience(id: string, studentDivisionId: string): Promise<HttpResponse>;

    deleteGroupingAudience(id: string, studentDivisionId: string): Promise<HttpResponse>;

    getGroupingList(structureId: string): Promise<Grouping[]>;
}

export const groupingService: GroupingService = {

    createGrouping: async (structureId: string, name: string): Promise<HttpResponse> => {
        return http.post(`/viescolaire/grouping/structure/${structureId}`,{name : name});
    },

    updateGrouping: async (id: string, name: string): Promise<HttpResponse> => {
        return http.put(`/viescolaire/grouping/${id}`,{name : name});
    },

    deleteGrouping: async (id: string): Promise<HttpResponse> => {
        return http.delete(`/viescolaire/grouping/${id}`);
    },

    addGroupingAudience: async (id: string, studentDivisionId: string): Promise<HttpResponse> => {
        return http.post(`/viescolaire/grouping/${id}/add`,{student_division_id : studentDivisionId});
    },

    deleteGroupingAudience: async (id: string, studentDivisionId: string): Promise<HttpResponse> => {
        return http.delete(`/viescolaire/grouping/${id}/delete`,{data : {student_division_id : studentDivisionId}});
    },

    getGroupingList: async (structureId: string): Promise<Grouping[]> => {
        return http.get(`/viescolaire/grouping/structure/${structureId}/list`)
            .then((res: HttpResponse) =>
                res.data.map((grouping: IGroupingItemResponse) => new Grouping().build(grouping))
            );
    },
}

export const GroupingService = ng.service('GroupingService', (): GroupingService => groupingService);