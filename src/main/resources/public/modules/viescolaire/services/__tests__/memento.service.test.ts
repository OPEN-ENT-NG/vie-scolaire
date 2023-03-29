import axios from 'axios';
import {mementoService} from "../MementoService";
import MockAdapter from "axios-mock-adapter";
import {MementoAccess} from "../../models/memento.model";


describe('Memento Service', () => {

    it('should return correct endpoint API for memento access within access endpoint FALSE', done => {
        const mock = new MockAdapter(axios);
        const data = {access: false};
        mock.onGet(`/viescolaire/memento/access`).reply(200, data);
        mementoService.checkAccess().then((response: MementoAccess) => {
            expect(response.access).toEqual(data.access);
            done();
        });
    });

});