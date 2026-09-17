jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {http} from 'entcore-toolkit';
import {mockHttpResponse} from '@test-utils/httpMock';
import {TimeSlot} from "../../models/common/TimeSlots";
import {timeslotClasseService} from "../TimeslotClasseService";

describe('TimeslotClasseService', () => {

    it('verification of createOrUpdateClassTimeslot method', done => {
        let dataGraph = {
            status: "ok"
        };
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(dataGraph));

        timeslotClasseService.createOrUpdateClassTimeslot("timeslotId", "classeId")
            .then(response => {
                expect(http.post).toHaveBeenCalledWith(`/viescolaire/timeslot/audience`, {timeslot_id: "timeslotId", class_id: "classeId"});
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })

    it('verification of getAudienceTimeslot method', done => {
        let audienceId = "audienceId";

        let timeslot: TimeSlot = {
            _id: "",
            classes: [],
            endOfHalfDay: "",
            name: "",
            schoolId: "",
            slots: [],
            save: undefined,
            saveEndHalfDay: undefined,
            syncClasseAssociation: undefined,
            toJson: undefined
        };

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(timeslot));

        timeslotClasseService.getAudienceTimeslot("audienceId")
            .then(response => {
                expect(http.get).toHaveBeenCalledWith(`/viescolaire/timeslot/audience/${audienceId}`);
                expect(response).toEqual(timeslot);
                done();
            });
    })

    it('verification of getAllClassFromTimeslot method', done => {
        let timeslotId = "timeslotId";

        let dataGraph = ["data1", "data2"];
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(dataGraph));

        timeslotClasseService.getAllClassFromTimeslot("timeslotId")
            .then(response => {
                expect(http.get).toHaveBeenCalledWith(`/viescolaire/timeslot/${timeslotId}`);
                expect(response).toEqual(dataGraph);
                done();
            });
    })

    it('verification of deleteClassTimeslot method', done => {
        let dataGraph = {
            status: "ok"
        };

        let classId = "classeId";
        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(dataGraph, {status: 204}));

        timeslotClasseService.deleteClassTimeslot("classeId")
            .then(response => {
                expect(http.delete).toHaveBeenCalledWith(`/viescolaire/timeslot/audience/${classId}`);
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })

    it('verification of deleteAllAudienceFromTimeslot method', done => {
        let dataGraph = {
            status: "ok"
        };

        let timeslotId = "timeslotId";

        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(dataGraph, {status: 204}));

        timeslotClasseService.deleteAllAudienceFromTimeslot("timeslotId")
            .then(response => {
                expect(http.delete).toHaveBeenCalledWith(`/viescolaire/timeslot/${timeslotId}`);
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })
})
