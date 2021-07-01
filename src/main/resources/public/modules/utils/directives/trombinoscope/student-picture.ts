import {Directive, moment, ng} from 'entcore';
import {trombinoscopeService} from '../../../viescolaire/services';
import {IUser} from '../../../viescolaire/models/common/User';
import {AxiosError, AxiosResponse} from "axios";
import {safeApply} from "../../functions/safeApply";

interface IViewModel {

    student: IUser;

    structureId: string;

    student_pic: FileList;

    getStudentPictureQuery(): string;

    updateInputDOM(): void;

    uploadStudentPicture(file: FileList): void;

    toggleRemovePictureLightbox(): void;

    changeFile(): void;
}

export const studentPicture: Directive = ng.directive('studentPicture', () => {

    return {
        restrict: 'E',
        transclude: true,
        scope: {
            onClick: '&'
        },
        template: `
            <div class="student-picture">
            
                 <!-- Student picture content -->
                 <div class="student-picture-content">
                 
                     <!-- background color to fill its icon -->
                     <div class="student-picture-content-background"></div>
                     <!-- remove current picture -->
                     <i class="mdi-delete-forever student-picture-content-remove" ng-click="vm.toggleRemovePictureLightbox()"></i>

                     
                     <!-- Picture picture-->
                     <img id="img-[[vm.student.id]]"
                         ng-if="vm.getStudentPictureQuery()"
                         class="student-picture-content-img"
                         src="[[vm.getStudentPictureQuery()]]"
                         onerror="this.src='/assets/themes/cg77/skins/default/../..//img/illustrations/no-avatar.svg';">
                       
                     <!-- Student name --> 
                     <button class="student-picture-content-button" data-ng-click="vm.changeFile()">
                        <i18n>viescolaire.trombinoscope.param.student.list.edit</i18n>
                     </button>
                 </div>
                 
                 <!-- Student name -->
                 <div class="student-picture-name ellipsis-multiline-two">
                    [[vm.student.displayName]]
                 </div>
                              
                 <input id="fileInput-[[vm.student.id]]"
                        data-ng-show="false"
                        type="file"
                        ng-model="vm.student_pic"
                        accept="image/*"
                        ng-click="vm.updateInputDOM()"
                        files-input-change="vm.uploadStudentPicture(vm.student_pic)" />
            </div>
            `,
        controllerAs: 'vm',
        bindToController: {
            student: '=',
            structureId: '=',
        },
        replace: true,

        controller: async ($scope) => {
            const vm: IViewModel = <IViewModel> this;
            vm.student_pic = null;
        },
        link: ($scope, $element) => {
            const vm: IViewModel = $scope.vm;

            vm.getStudentPictureQuery = (): string => {
                return `/viescolaire/structures/${vm.structureId}/students/${vm.student.id}/picture`;
            };

            vm.changeFile = (): void => {
                document.getElementById('fileInput-' + vm.student.id).click();
            };

            vm.updateInputDOM = (): void => {
                safeApply($scope);
            };

            vm.uploadStudentPicture = async (file: FileList): Promise<void> => {
                const studentPicture: File = file[0];
                trombinoscopeService.updateTrombinoscope(vm.structureId, vm.student.id, studentPicture).then((res: AxiosResponse) => {
                    if (res.status === 200 || res.status === 201) {
                        updatePictureDOM();
                        safeApply($scope);
                    }
                }).catch((_: AxiosError) => {
                    updatePictureDOM();
                    safeApply($scope);
                });
            };

            vm.toggleRemovePictureLightbox = (): void => $scope.$parent.$eval($scope.onClick);

            const updatePictureDOM = (): void => {
                const timestamp: string = `?timestamp=${moment().unix()}`; // weird tricks to refresh the ng-src img manually front
                (<HTMLImageElement>document.getElementById('img-' + vm.student.id)).src = vm.getStudentPictureQuery() + timestamp;
            };
        }
    };
});