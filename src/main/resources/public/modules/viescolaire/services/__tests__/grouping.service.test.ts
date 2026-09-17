jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {http, HttpResponse} from "entcore-toolkit";
import {mockHttpResponse} from '@test-utils/httpMock';
import {groupingService} from "../grouping.service";

describe('GroupingService', () => {

    const id = "id";
    const structureId = "structureId";
    const name = "test";
    const studentDivisionId = "studentDivisionId";

    it('should returns data when createGrouping request is correctly called', done => {
        const data = {response: true};
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        groupingService.createGrouping(structureId, name).then((response: HttpResponse) => {
            expect(http.post).toHaveBeenCalledWith(`/viescolaire/grouping/structure/${structureId}`, {name: name});
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when updateGrouping request is correctly called', done => {
        const data = {response: true};
        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        groupingService.updateGrouping(id, name).then((response: HttpResponse) => {
            expect(http.put).toHaveBeenCalledWith(`/viescolaire/grouping/${id}`, {name: name});
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when deleteGrouping request is correctly called', done => {
        const data = {response: true};
        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        groupingService.deleteGrouping(id).then((response: HttpResponse) => {
            expect(http.delete).toHaveBeenCalledWith(`/viescolaire/grouping/${id}`);
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when addGroupingAudience request is correctly called', done => {
        const data = {response: true};
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        groupingService.addGroupingAudience(id, studentDivisionId).then((response: HttpResponse) => {
            expect(http.post).toHaveBeenCalledWith(`/viescolaire/grouping/${id}/add`, {student_division_id: studentDivisionId});
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when deleteGroupingAudience request is correctly called', done => {
        const data = {response: true};
        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));

        groupingService.deleteGroupingAudience(id, studentDivisionId).then((response: HttpResponse) => {
            expect(http.delete).toHaveBeenCalledWith(`/viescolaire/grouping/${id}/delete`, {data: {student_division_id: studentDivisionId}});
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should returns data when getGroupingList request is correctly called', done => {
        const structure = "structureId";
        const data = [{
            id: "id",
            name: "name",
            student_divisions: [{
                id: "student_division_id",
                name: "name"
            }],
            structure_id: "structure_id"
        }];
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        groupingService.getGroupingList(structure)
            .then(response => {
                expect(http.get).toHaveBeenCalledWith(`/viescolaire/grouping/structure/${structureId}/list`);
                expect(response).toEqual(data);
                done();
            });
    });
});
