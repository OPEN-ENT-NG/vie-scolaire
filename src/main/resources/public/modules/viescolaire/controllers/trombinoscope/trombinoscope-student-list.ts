import {ng, template} from 'entcore';
import {StudentsSearch} from '../../utils/autocomplete';
import {ISearchService, UserService} from '../../services';
import {safeApply} from '../../../utils/functions/safeApply';
import {GroupsSearch} from '../../utils/autocomplete';
import {IUser} from '../../models/common/User';
import {IGroup} from '../../models/common/Group';
import {INFINITE_SCROLL_EVENTER} from '../../core/enum/infinite-scroll-eventer';

interface IFilter {
    students: Array<string>;
    groups: Array<string>;
    page: number;
}

interface IViewModel {

    structureName: string;
    studentsSearch: StudentsSearch;
    groupsSearch: GroupsSearch;
    studentList: IUser[];
    filter: IFilter;

    getStudents(): Promise<void>;

    updateFilter(): Promise<void>;

    getStudentsFromGroup(groupName: string): IUser[];

    getGroupNames(): string[];

    searchStudent(student: string): Promise<void>;

    selectStudent(valueInput, studentItem): void;

    removeSelectedStudents(studentItem): void;

    searchGroup(group: string): Promise<void>;

    selectGroup(valueInput, groupItem): void;

    removeSelectedGroups(groupItem): void;

    onScroll(): Promise<void>;

    goToImport(): void;
}

export const trombinoscopeStudentListController = ng.controller('TrombinoscopeStudentListController',
    ['$scope', '$route', '$location', 'SearchService', 'UserService',
        async function ($scope, $route, $location, searchService: ISearchService, userService: UserService) {

            const vm: IViewModel = this;
            vm.studentsSearch = undefined;
            vm.groupsSearch = undefined;
            vm.studentList = [];
            vm.filter = {
              students: [],
              groups: [],
              page: 0
            };

            const initData = async (): Promise<void> => {

                vm.structureName = $scope.structure.name;

                await vm.getStudents();

                vm.studentsSearch = new StudentsSearch($scope.structure.id, searchService);

                vm.groupsSearch = new GroupsSearch($scope.structure.id, searchService);

                safeApply($scope);
            };

            vm.getStudents = async (): Promise<void> => {
                vm.filter.page = 0;
                vm.studentList = await userService.getStudents($scope.structure.id, vm.filter.page, vm.filter.students, vm.filter.groups, true);
                safeApply($scope);
            };

            vm.updateFilter = async (): Promise<void> => {
                /* Retrieving our search bar info */
                vm.filter.students = vm.studentsSearch.getSelectedStudents().map(student => student['id']);
                vm.filter.groups = vm.groupsSearch.getSelectedGroups().map(group => group['name']);

                await vm.getStudents();
                $scope.$broadcast(INFINITE_SCROLL_EVENTER.UPDATE);
            };

            vm.getStudentsFromGroup = (groupName: string): IUser[] => {
                return vm.studentList.filter(
                    (student: IUser) =>
                        student.audienceName === groupName);
            };

            vm.getGroupNames = (): string[] => {
                return _.union(vm.studentList.map((student: IUser) => student.audienceName));
            };

            /**
             * ⚠ Autocomplete classes/methods for students and groups
             */
            vm.searchStudent = async (student: string): Promise<void> => {
                await vm.studentsSearch.searchStudents(student);
                safeApply($scope);
            };

            vm.selectStudent = (valueInput: string, studentItem: IUser): void => {
                vm.studentsSearch.selectStudents(valueInput, studentItem);
                vm.filter.students = vm.studentsSearch.getSelectedStudents().map(student => student['id']);
                vm.studentsSearch.student = '';
                vm.updateFilter();
            };

            vm.removeSelectedStudents = (studentItem: IUser): void => {
                vm.studentsSearch.removeSelectedStudents(studentItem);
                vm.filter.students = vm.studentsSearch.getSelectedStudents().map(student => student['id']);
                vm.updateFilter();
            };

            vm.searchGroup = async (group: string): Promise<void> => {
                await vm.groupsSearch.searchGroups(group);
                safeApply($scope);
            };

            vm.selectGroup = (valueInput: string, groupItem: IGroup): void => {
                vm.groupsSearch.selectGroups(valueInput, groupItem);
                vm.filter.groups = vm.groupsSearch.getSelectedGroups().map(group => group['id']);
                vm.groupsSearch.group = '';
                vm.updateFilter();
            };

            vm.removeSelectedGroups = (groupItem: IGroup): void => {
                vm.groupsSearch.removeSelectedGroups(groupItem);
                vm.filter.groups = vm.groupsSearch.getSelectedGroups().map(group => group['id']);
                vm.updateFilter();
            };

            vm.onScroll = async (): Promise<void> => {
                vm.filter.page++;

                let students: Array<IUser> = await userService.getStudents($scope.structure.id, vm.filter.page, vm.filter.students, vm.filter.groups, true);
                if (students.length !== 0) {
                    vm.studentList = vm.studentList.concat(students);
                    $scope.$broadcast(INFINITE_SCROLL_EVENTER.UPDATE);
                }
                safeApply($scope);
            };

            vm.goToImport = (): void => {
                template.open('trombinoscope', '../templates/viescolaire/param_etab_items/trombinoscope/param_trombinoscope_import');
            };

            $scope.$watch(() => $scope.structure, async () => {
                await initData();
            });
        }]);
