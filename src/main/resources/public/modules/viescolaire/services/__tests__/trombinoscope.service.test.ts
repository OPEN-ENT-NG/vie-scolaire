import {trombinoscopeService} from "../TrombinoscopeService";
import axios, {AxiosResponse} from 'axios';
import MockAdapter from 'axios-mock-adapter';


describe('TrombinoscopeService', () => {

    const structure: string = "structureId";
    const studentId: string = "studentId";
    const picture: string = "pictureId";

    const file: File = new File([new Blob()], "");

    it('should return data when API importTrombinoscope request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPost(`/viescolaire/structures/${structure}/trombinoscope`).reply(200, data);
        trombinoscopeService.importTrombinoscope(structure, file).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API updateTrombinoscope request is correctly called', done => {
        let mock = new MockAdapter(axios);

        const data = {response: true};
        mock.onPut(`/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`)
            .reply(200, data);
        trombinoscopeService.updateTrombinoscope(structure, studentId, file).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API deleteTrombinoscope request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onDelete(`/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`).reply(200, data);
        trombinoscopeService.deleteTrombinoscope(structure, studentId).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API getFailures request is correctly called', done => {
        const mock = new MockAdapter(axios);
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
        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/failures`)
            .reply(200, respApi);
        trombinoscopeService.getFailures(structure).then(res => {
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API linkTrombinoscope request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPost(`/viescolaire/structures/${structure}/students/${studentId}/trombinoscope`,
            { pictureId: picture}).reply(200, data);
        trombinoscopeService.linkTrombinoscope(structure, studentId, picture).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API setStructureSettings request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onPost(`/viescolaire/structures/${structure}/trombinoscope/setting`, {active: true})
            .reply(200, data);
        trombinoscopeService.setStructureSettings(structure, true).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

    it('should return data when API getStructureSettings request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = true;

        const respApi = {
            active: data
        };
        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/setting`).reply(200, respApi);
        trombinoscopeService.getStructureSettings(structure).then((res) => {
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API getReports request is correctly called',  done => {
        let mock = new MockAdapter(axios);
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

        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/reports?limit=${5}&offset=${5}`)
            .reply(200, respApi);
        trombinoscopeService.getReports(structure, 5, 5).then((res) => {
            expect(res).toEqual(data);
            done();
        });
    });

    it('should return data when API deleteFailuresHistory request is correctly called', done => {
        let mock = new MockAdapter(axios);
        const data = {response: true};
        mock.onDelete(`/viescolaire/structures/${structure}/trombinoscope/failures`).reply(200, data);
        trombinoscopeService.deleteFailuresHistory(structure).then((response: AxiosResponse) => {
            expect(response.data).toEqual(data);
            done();
        });
    });

});