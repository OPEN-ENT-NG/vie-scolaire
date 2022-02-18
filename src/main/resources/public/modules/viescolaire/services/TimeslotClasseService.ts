import {ng} from "entcore";
import {TimeSlot} from "../models/common/TimeSlots";
import http, {AxiosResponse} from "axios";

interface TimeslotClasseService {
    getAudienceTimeslot(audienceId: string): Promise<TimeSlot>

    getAllClassFromTimeslot(timeslotId: string): Promise<string[]>

    createOrUpdateClassTimeslot(timeslotId: string, classId: string): Promise<AxiosResponse>

    deleteClassTimeslot(classId: string): Promise<AxiosResponse>

    deleteAllAudienceFromTimeslot(timeslotId: string): Promise<AxiosResponse>
}

export const timeslotClasseService: TimeslotClasseService =  {
    async createOrUpdateClassTimeslot(timeslotId: string, classId: string): Promise<AxiosResponse> {
        return http.post(`/viescolaire/timeslot/audience`, {timeslot_id: timeslotId, class_id: classId});
    },

    async deleteAllAudienceFromTimeslot(timeslotId: string): Promise<AxiosResponse> {
        return http.delete(`/viescolaire/timeslot/${timeslotId}`);
    },

    async deleteClassTimeslot(classId: string): Promise<AxiosResponse> {
        return http.delete(`/viescolaire/timeslot/audience/${classId}`);
    },

    async getAllClassFromTimeslot(timeslotId: string): Promise<string[]> {
        const {data}: AxiosResponse = await http.get(`/viescolaire/timeslot/${timeslotId}`);
        return data as string[];
    },

    async getAudienceTimeslot(audienceId: string): Promise<TimeSlot> {
        const {data}: AxiosResponse = await http.get(`/viescolaire/timeslot/audience/${audienceId}`);
        return data as TimeSlot;
    }
}

export const TimeslotClasseService = ng.service('TimeslotClasseService', (): TimeslotClasseService => timeslotClasseService);