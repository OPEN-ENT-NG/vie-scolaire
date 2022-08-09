// tricks to fake "mock" entcore ng class in order to use service
import axios, {AxiosResponse} from "axios";
import MockAdapter from "axios-mock-adapter";
import {groupingService} from "../grouping.service";

describe('GroupingService', () => {

    const id = "id";
    const structureId = "structureId";
    const name = "test";
    const classOrGroupId = "classOrGroupdId"

    it('should returns data when createGrouping request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPost(`/viescolaire/grouping/structure/${structureId}`)
            .reply(200, data);

        groupingService.createGrouping(structureId, name).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when updateGrouping request is correctly called', done => {
        const mock = new MockAdapter(axios);

        const data = {response: true};
        mock.onPut(`/viescolaire/grouping/${id}`)
            .reply(200, data);

        groupingService.updateGrouping(id, name).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when deleteGrouping request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onDelete(`/viescolaire/grouping/${id}`)
            .reply(200, data);

        groupingService.deleteGrouping(id).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when addGroupingAudience request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPost(`/viescolaire/grouping/${id}/add`)
            .reply(200, data);

        groupingService.addGroupingAudience(id, classOrGroupId).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when deleteGroupingAudience request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPut(`/viescolaire/grouping/${id}/delete`)
            .reply(200, data);

        groupingService.deleteGroupingAudience(id, classOrGroupId).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when getGroupingList request is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = [{
            id: "id",
            name: "name",
            strucutreId: "structureId",
            class: [],
            group: [],
        }]
        mock.onGet(`/viescolaire/grouping/list/`)
            .reply(200, data);
        groupingService.getGroupingList()
            .then(response => {
                expect(response).toEqual(data);
                done();
            });
    });
});