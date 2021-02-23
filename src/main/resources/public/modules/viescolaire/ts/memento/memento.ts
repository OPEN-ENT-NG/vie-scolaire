import http, {AxiosError, AxiosResponse} from 'axios';
import {Behaviours, idiom, toasts} from 'entcore';
import {mementoService} from '../../services';
import {DateUtils} from '../../utils/dateUtils';

declare let window: any;

export interface IStudentSearchObject {
    id: string;
    displayName: string;
    lastName: string;
    firstName: string;
    idClasse: Array<string>;
}

export type MementoRelative = {
    id: string;
    name: string;
    title: string;
    externalId: string;
    phone: string;
    email: string;
    address: string;
    activated: boolean;
    primary: boolean;
};

interface IMementoViewModel {
    show: boolean;
    studentId: string;
    focusedSection: string;
    student: {
        id: string
        name: string
        birth_date: string
        groups: Array<String>
        classes: Array<String>
        relatives: Array<MementoRelative>,
        class_id: string
    };
    config: {
        competences: boolean
        presences: boolean
    };
    that: any;
    $apply: any;
    $eval: any;
    search: {
        value: string
        list: Array<IStudentSearchObject>
    };

    setApplier(obj: any): void;

    saveComment(comment: string): Promise<void>;

    formatBirthDate(date: string): string;

    updateRelativePriorities(): Promise<void>;

    loadMemento(studentId: string): void;

    closeMemento(): void;

    init(): Promise<void>;

    loadConfig(): Promise<void>;

    searchForStudent(): Promise<void>;

    selectStudent(model: string, option: IStudentSearchObject): void;

    scrollToSection(target: string): void;
}

const vm: IMementoViewModel = {
    show: false,
    studentId: null,
    student: null,
    $apply: null,
    $eval: null,
    that: null,
    focusedSection: null,
    search: {
        value: null,
        list: null
    },
    loadMemento(studentId: string): void {
        document.getElementsByTagName('html')[0].style['overflowY'] = 'hidden';
        vm.show = true;
        vm.studentId = studentId;
        http.get(`/viescolaire/memento/students/${studentId}`).then(({data}) => {
            vm.student = data;
            vm.$apply();
            console.log('triggering event');
            vm.that.$broadcast('memento:init', {student: vm.student.id, group: vm.student.class_id});
        }).catch(err => {
            throw err;
        });
    },
    async saveComment(comment: string): Promise<void> {
        mementoService.saveComment(vm.studentId, comment)
            .then((response: AxiosResponse) => {
                if (response.status !== 200 && response.status !== 201) {
                    toasts.warning('viescolaire.memento.student.comment.error');
                }
            })
            .catch((_: AxiosError) => {
                toasts.warning('viescolaire.memento.student.comment.error');
            });
    },

    formatBirthDate(date: string): string {
        return DateUtils.getDisplayDate(date);
    },

    async updateRelativePriorities(): Promise<void> {
        let primaryRelatives: Array<string> = [];
        if (vm.student.relatives) {
            vm.student.relatives.forEach((relative: MementoRelative) => {
                if (relative.primary === true) {
                    primaryRelatives.push(relative.id);
                }
            });
        }

        mementoService.updateRelativePriorities(vm.studentId, primaryRelatives)
            .then((response: AxiosResponse) => {
                if (response.status !== 200 && response.status !== 201) {
                    toasts.warning('viescolaire.memento.primary.contact.error');
                }
            })
            .catch((_: AxiosError) => {
                toasts.warning('viescolaire.memento.primary.contact.error');
            });
    },

    closeMemento(): void {
        document.getElementsByTagName('html')[0].style['overflowY'] = 'inherit';
        vm.that.$broadcast('memento:close');
        vm.show = false;
        vm.$apply();
    },
    async init(): Promise<void> {
        await vm.loadConfig();
        ['viescolaire', ...Object.keys(vm.config)].forEach(moduleName => {
            if (vm.config[moduleName]) {
                Behaviours.load(moduleName);
                idiom.addBundle(`/${moduleName}/i18n`);
            }
        });
    },
    setApplier(obj: any): void {
        vm.$apply = obj.$apply;
        vm.$eval = obj.$eval;
        vm.that = obj;
    },
    config: {
        competences: false,
        presences: false
    },
    async loadConfig(): Promise<void> {
        try {
            const {data}: AxiosResponse = await http.get('/viescolaire/config');
            vm.config = data;
        } catch (err) {
            toasts.warning('viescolaire.memento.config.err');
            throw err;
        }
    },
    async searchForStudent(): Promise<void> {
        try {
            const {data}: AxiosResponse = await http.get(`/viescolaire/user/search?q=${vm.search.value}&field=lastName&field=firstName&field=displayName&profile=Student&structureId=${window.structure.id}`);
            const toString = function () {
                return `${this.lastName} ${this.firstName}`;
            };
            data.forEach((student) => student.toString = toString);
            vm.search.list = data;
            vm.$apply();
        } catch (err) {
            throw err;
        }
    },
    selectStudent(model: string, option: IStudentSearchObject): void {
        vm.search = {
            list: null,
            value: null
        };
        vm.loadMemento(option.id);
    },
    scrollToSection(target: string): void {
        vm.focusedSection = target;
        vm.$apply();
        let element = document.getElementById(target);
        element.scrollIntoView({behavior: 'smooth', block: 'start', inline: 'nearest'});
    }
};

const MementoViewModel = vm;

export default MementoViewModel;