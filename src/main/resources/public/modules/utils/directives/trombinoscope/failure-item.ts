import {ng, Directive, toasts} from 'entcore';
import {StudentsSearch} from '../../../viescolaire/utils/autocomplete';
import {ISearchService, trombinoscopeService} from '../../../viescolaire/services';
import {IFailure} from '../../../viescolaire/models/trombinoscope';
import {safeApply} from '../../functions/safeApply';
import {IUser} from '../../../viescolaire/models/common/User';
import {FAILURE_EVENTER} from '../../../viescolaire/core/enum/failure-eventer';


interface IViewModel {

    failure: IFailure;

    studentsSearch: StudentsSearch;

    searchStudent(student: string): Promise<void>;

    selectStudent(valueInput: string, studentItem: IUser): void;

    getImgQuery(): string;
}

export const failureItem: Directive = ng.directive('failureItem', ['SearchService', (searchService: ISearchService) => {

        return {
            restrict: 'E',
            transclude: true,
            scope: {
                failure: '='
            },
            template: `
            <div class="failure">
                <!-- Picture -->
                <img class="error-photo"
                     draggable="false"
                     src="[[vm.getImgQuery()]]">

                <!-- Details -->
                <div class="error-info">
                    <span class="error-info-name"><i18n>viescolaire.trombinoscope.param.photo.failure.name</i18n></span>
                    <span class="error-info-path">[[vm.failure.path]]</span>

                    <!-- Search bar -->
                    <div class="cell twelve error-info-search">
                        <async-autocomplete
                                data-ng-disabled="false"
                                data-ng-model="vm.studentsSearch.student"
                                data-ng-change="vm.selectStudent"
                                data-on-search="vm.searchStudent"
                                data-options="vm.studentsSearch.students"
                                data-placeholder="viescolaire.trombinoscope.param.search.student"
                                data-search="vm.studentsSearch.student">
                        </async-autocomplete>
                    </div>
                </div>
            </div>
            `,
            controllerAs: 'vm',
            bindToController: true,
            replace: true,

            controller: async ($scope) => {
                const vm: IViewModel = <IViewModel> this;
            },
            link: ($scope, $element) => {
                const vm: IViewModel = $scope.vm;

                vm.studentsSearch = new StudentsSearch($scope.vm.failure.structureId, searchService);

                vm.getImgQuery = (): string => {
                    return `/viescolaire/structures/${vm.failure.structureId}/trombinoscope/failures/${vm.failure.id}`;
                };

                vm.searchStudent = async (student: string): Promise<void> => {
                    await vm.studentsSearch.searchStudents(student);
                    await safeApply($scope);
                };

                vm.selectStudent = (valueInput: string, studentItem: IUser): void => {
                    trombinoscopeService.linkTrombinoscope(vm.failure.structureId, studentItem.id, vm.failure.pictureId)
                        .then(res => {
                            if (res.status === 200) {
                                toasts.confirm('viescolaire.trombinoscope.student.picture.link.confirm');
                                $scope.$emit(FAILURE_EVENTER.DELETE);
                            } else {
                                toasts.warning('viescolaire.trombinoscope.student.picture.link.error');
                            }
                        });
                };
            }
        };
    }]);