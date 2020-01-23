import http from 'axios';
import {Behaviours, idiom, moment, toasts} from 'entcore';

declare let window: any;

interface IStudentSearchObject {
    id: string,
    displayName: string,
    lastName: string,
    firstName: string,
    idClasse: Array<string>
}

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
        relatives: Array<String>,
        class_id: string
    }
    config: {
        competences: boolean
        presences: boolean
    };
    that: any,
    $apply: any;
    $eval: any;
    search: {
        value: string
        list: Array<IStudentSearchObject>
    }

    setApplier(obj: any): void;

    saveComment(comment: string): Promise<void>;

    formatBirthDate(date: string): string;

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
        // vm.$apply();
        http.get(`/viescolaire/memento/students/${studentId}`).then(({data}) => {
            vm.student = data;
            vm.$apply();
            console.log('triggering event');
            vm.that.$broadcast('memento:init', {student: vm.student.id, group: vm.student.class_id});
        }).catch(err => {
            throw err;
        })
    },
    async saveComment(comment: string): Promise<void> {
        try {
            await http.post(`/viescolaire/memento/students/${vm.studentId}/comments`, {comment});
        } catch (err) {
            toasts.warning('viescolaire.memento.student.comment.error');
            throw err;
        }
    },
    formatBirthDate(date: string): string {
        return moment(date).format('DD/MM/YYYY');
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
            const {data} = await http.get('/viescolaire/config');
            vm.config = data;
        } catch (err) {
            toasts.warning('viescolaire.memento.config.err');
            throw err;
        }
    },
    async searchForStudent(): Promise<void> {
        try {
            const {data} = await http.get(`/viescolaire/user/search?q=${vm.search.value}&field=lastName&field=firstName&field=displayName&profile=Student&structureId=${window.structure.id}`);
            const toString = function () {
                return `${this.lastName} ${this.firstName}`;
            };
            data.forEach(student => student.toString = toString);
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
        element.scrollIntoView({behavior: "smooth", block: "start", inline: "nearest"});
    }
};

const MementoViewModel = vm;

export default MementoViewModel;