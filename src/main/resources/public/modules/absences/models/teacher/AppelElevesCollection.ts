import { http } from 'entcore/entcore';

import { Cours } from './Cours';
import { Eleve } from './Eleve';

export class AppelElevesCollection {
    sync: any;
    load: (data: Eleve[], cb?: (item: Eleve) => void, refreshView?: boolean) => void;
    findWhere: (props: any) => Eleve;
    all: Eleve[];
    trigger: (eventName: string) => void;
    cours: Cours;
    composer: any;

    constructor () {
    }

}
