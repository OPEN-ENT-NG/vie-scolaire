import MockAdapter from "axios-mock-adapter";
import {TimeSlot} from "../../models/common/TimeSlots";
import axios from 'axios';
import {timeslotClasseService} from "../TimeslotClasseService";

describe('TimeslotClasseService', () => {

    it('verification of createOrUpdateClassTimeslot method', done => {
        const mock = new MockAdapter(axios);

        let dataGraph = {
            status: "ok"
        };
        mock.onPost(`/viescolaire/timeslot/audience`, {timeslot_id: "timeslotId", class_id: "classeId"})
            .reply(200, dataGraph);

        timeslotClasseService.createOrUpdateClassTimeslot("timeslotId", "classeId")
            .then(response => {
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })

    it('verification of getAudienceTimeslot method', done => {
        const mock = new MockAdapter(axios);

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

        let dataGraph = ["data1", "data2"];
        mock.onGet(`/viescolaire/timeslot/audience/${audienceId}`)
            .reply(200, timeslot);

        timeslotClasseService.getAudienceTimeslot("audienceId")
            .then(response => {
                expect(response).toEqual(timeslot);
                done();
            });
    })

    it('verification of getAllClassFromTimeslot method', done => {
        const mock = new MockAdapter(axios);

        let timeslotId = "timeslotId";

        let dataGraph = ["data1", "data2"];
        mock.onGet(`/viescolaire/timeslot/${timeslotId}`)
            .reply(200, dataGraph);

        timeslotClasseService.getAllClassFromTimeslot("timeslotId")
            .then(response => {
                expect(response).toEqual(dataGraph);
                done();
            });
    })

    it('verification of deleteClassTimeslot method', done => {
        const mock = new MockAdapter(axios);

        let dataGraph = {
            status: "ok"
        };

        let classId = "classeId"
        mock.onDelete(`/viescolaire/timeslot/audience/${classId}`)
            .reply(204, dataGraph);

        timeslotClasseService.deleteClassTimeslot("classeId")
            .then(response => {
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })

    it('verification of deleteAllAudienceFromTimeslot method', done => {
        const mock = new MockAdapter(axios);

        let dataGraph = {
            status: "ok"
        };

        let timeslotId = "timeslotId"

        mock.onDelete(`/viescolaire/timeslot/${timeslotId}`)
            .reply(204, dataGraph);

        timeslotClasseService.deleteAllAudienceFromTimeslot("timeslotId")
            .then(response => {
                expect(response.data).toEqual(dataGraph);
                done();
            });
    })
})