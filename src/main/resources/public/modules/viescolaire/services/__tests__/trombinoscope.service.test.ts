import {trombinoscopeService} from "../TrombinoscopeService";
import axios, {AxiosRequestConfig, AxiosResponse} from 'axios';
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

    it('should return data when API getFailures request is correctly called', () => {
        let mock = new MockAdapter(axios);
        const data = [{
            id: "id",
            structureId: "structure",
            pictureId: "pictureId",
            path: "path",
            createdAt: "createdAt",
            message: "message"
        }];
        let correctData;
        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/failures`).reply(
            (_: AxiosRequestConfig) => new Promise(() => correctData = data)
        );
        trombinoscopeService.getFailures(structure).then(() => {
            expect(correctData).toEqual(data);
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

    it('should return data when API getStructureSettings request is correctly called', () => {
        let mock = new MockAdapter(axios);
        const data = true;
        let correctData;
        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/setting`).reply(
            (_: AxiosRequestConfig) => new Promise(() => correctData = data)
        );
        trombinoscopeService.getStructureSettings(structure).then(() => {
            expect(correctData).toEqual(data);
        });
    });

    it('should return data when API getReports request is correctly called',  () => {
        let mock = new MockAdapter(axios);
        const data = [{
            _id: "id",
            UAI: "UAI",
            createdAt: "createdAt",
            structureId: "structure",
            content: "content"
        }];

        let correctData;
        mock.onGet(`/viescolaire/structures/${structure}/trombinoscope/reports?limit=${5}&offset=${5}`).reply(
            (_: AxiosRequestConfig) => new Promise(() => correctData = data)
        );
        trombinoscopeService.getReports(structure, 5, 5).then(() => {
            expect(correctData).toEqual(data);
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