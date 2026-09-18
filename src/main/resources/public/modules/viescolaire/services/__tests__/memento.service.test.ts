jest.mock('entcore-toolkit', () => Object.assign({}, (jest as any).requireActual('entcore-toolkit'), {
    http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()},
}));

import {http} from 'entcore-toolkit';
import {mockHttpResponse} from '@test-utils/httpMock';
import {mementoService} from "../MementoService";
import {MementoAccess} from "../../models/memento.model";


describe('Memento Service', () => {

    it('should return correct endpoint API for memento access within access endpoint FALSE', done => {
        const data = {access: false};
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        mementoService.checkAccess().then((response: MementoAccess) => {
            expect(http.get).toHaveBeenCalledWith(`/viescolaire/memento/access`);
            expect(response.access).toEqual(data.access);
            done();
        });
    });

});
