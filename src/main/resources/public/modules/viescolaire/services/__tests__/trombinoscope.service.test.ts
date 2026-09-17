jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {trombinoscopeService} from "../TrombinoscopeService";
import {http, HttpResponse} from 'entcore-toolkit';
import {mockHttpResponse} from '@test-utils/httpMock';


describe('TrombinoscopeService', () => {

    const structure: string = "structureId";
    const studentId: string = "studentId";
    const picture: string = "pictureId";

    const file: File = new File([new Blob()], "");

    it('should return data when API importTrombinoscope request is correctly called', done => {
        const data = {response: true};
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.importTrombinoscope(structure, file).then((response: HttpResponse) => {
            expect(http.post).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope`, expect.any(FormData));
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API updateTrombinoscope request is correctly called', done => {
        const data = {response: true};
        (http.put as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.updateTrombinoscope(structure, studentId, file).then((response: HttpResponse) => {
            expect(http.put).toHaveBeenCalledWith(
                `/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`,
                expect.any(FormData),
                {'headers': {'Content-type': 'multipart/form-data'}}
            );
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API deleteTrombinoscope request is correctly called', done => {
        const data = {response: true};
        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.deleteTrombinoscope(structure, studentId).then((response: HttpResponse) => {
            expect(http.delete).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`);
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API getFailures request is correctly called', done => {
        const data = [{
            id: "id",
            structureId: "structure",
            pictureId: "pictureId",
            path: "path",
            createdAt: "createdAt",
            message: "message1"
        }];
        const respApi = {
            all: data
        };
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(respApi));
        trombinoscopeService.getFailures(structure).then(res => {
            expect(http.get).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope/failures`);
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API linkTrombinoscope request is correctly called', done => {
        const data = {response: true};
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.linkTrombinoscope(structure, studentId, picture).then((response: HttpResponse) => {
            expect(http.post).toHaveBeenCalledWith(
                `/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`,
                {pictureId: picture}
            );
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API setStructureSettings request is correctly called', done => {
        const data = {response: true};
        (http.post as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.setStructureSettings(structure, true).then((response: HttpResponse) => {
            expect(http.post).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope/setting`, {active: true});
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API getStructureSettings request is correctly called', done => {
        const data = true;

        const respApi = {
            active: data
        };
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(respApi));
        trombinoscopeService.getStructureSettings(structure).then((res) => {
            expect(http.get).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope/setting`);
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API getReports request is correctly called', done => {
        const data = [{
            _id: "id",
            UAI: "UAI",
            createdAt: "createdAt",
            structureId: "structure",
            content: "content"
        }];

        const respApi = {
            all: data
        };

        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(respApi));
        trombinoscopeService.getReports(structure, 5, 5).then((res) => {
            expect(http.get).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope/reports?limit=${5}&offset=${5}`);
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API deleteFailuresHistory request is correctly called', done => {
        const data = {response: true};
        (http.delete as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        trombinoscopeService.deleteFailuresHistory(structure).then((response: HttpResponse) => {
            expect(http.delete).toHaveBeenCalledWith(`/viescolaire/structures/${structure}/trombinoscope/failures`);
            expect(response.data).toEqual(data);
            done();
        });
    });

});
