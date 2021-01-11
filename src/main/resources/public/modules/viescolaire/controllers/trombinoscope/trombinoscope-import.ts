import {ng, idiom as lang, template, toasts, moment} from 'entcore';
import {IReport} from '../../models/trombinoscope';
import {IFailure} from '../../models/trombinoscope';
import {safeApply} from '../../../utils/functions/safeApply';
import {ITrombinoscopeService} from '../../services';
import {AxiosError, AxiosResponse} from 'axios';
import {FAILURE_EVENTER} from '../../core/enum/failure-eventer';
import {DateUtils} from '../../utils/dateUtils';

declare let window: any;

interface IViewModel {
    activeENTSetting: boolean;
    reportsList: IReport[];
    failureList: IFailure[];
    isReportDisplayed: boolean;
    displayAllReports: boolean;
    displayedReportText: string;

    syncSettings(): Promise<void>;

    setSetting(value: boolean): Promise<void>;

    getReportList(): Promise<void>;

    getFailureList(): Promise<void>;

    getReportTitle(report: IReport): string;

    displayReport(content: string): void;

    uploadFile(file: FileList): Promise<void>;

    deleteFailuresHistory(): Promise<void>;

    goToStudents(): void;

}

export const trombinoscopeImportController = ng.controller('TrombinoscopeImportController',
    ['$scope', '$route', '$location', 'TrombinoscopeService',
        async function ($scope, $route, $location, trombinoscopeService: ITrombinoscopeService) {

            const vm: IViewModel = this;

            vm.isReportDisplayed = false;
            vm.activeENTSetting = true;
            vm.displayAllReports = false;
            vm.reportsList = [];
            vm.failureList = [];

            const initData = async (): Promise<void> => {

                await Promise.all([vm.syncSettings(), vm.getReportList(), vm.getFailureList()]);
                safeApply($scope);
            };


            vm.syncSettings = async (): Promise<void> => {
               vm.activeENTSetting = !(await trombinoscopeService.getStructureSettings($scope.structure.id));
            };

            vm.setSetting = async (value: boolean): Promise<void> => {
                await trombinoscopeService.setStructureSettings($scope.structure.id, value);
                await vm.syncSettings();
            };

            vm.getReportList = async (): Promise<void> => {
                vm.reportsList = await trombinoscopeService.getReports($scope.structure.id,  -1, 0);
                safeApply($scope);
            };


            vm.getReportTitle = (report: IReport): string => {
                return lang.translate('viescolaire.trombinoscope.param.report.import.from') +
                    DateUtils.getDisplayDate(report.createdAt) +
                     lang.translate('viescolaire.trombinoscope.param.report.import.to') +
                    DateUtils.getDisplayTime(report.createdAt);
            };


            vm.getFailureList = async (): Promise<void> => {
                vm.failureList = await trombinoscopeService.getFailures($scope.structure.id);
                safeApply($scope);
            };

            vm.displayReport = (content: string): void => {
                vm.isReportDisplayed = true;
                vm.displayedReportText = content;
            };

            vm.uploadFile = async (file: FileList): Promise<void> => {
                const zipFile: File = file[0];

                trombinoscopeService.importTrombinoscope($scope.structure.id, zipFile)
                    .then(async (res: AxiosResponse) => {
                            if (res.status === 200) {
                                toasts.confirm('viescolaire.trombinoscope.import.zip.confirm');
                                await Promise.all([ vm.getFailureList(), vm.getReportList()]);
                            } else {
                                toasts.warning('viescolaire.trombinoscope.import.zip.error');
                            }
                    })
                    .catch(async (err: AxiosError) => {
                        switch (err.response.status) {
                            case 401:
                                toasts.warning('evaluation.error.unautorize');
                                break;
                            case 500:
                                toasts.warning('viescolaire.trombinoscope.import.zip.error');
                                await vm.getReportList();
                                break;
                            case 413:
                                toasts.warning('viescolaire.trombinoscope.import.zip.memory.issue');
                                break;
                        }
                    });
            };

            vm.deleteFailuresHistory = async (): Promise<void> => {
                trombinoscopeService.deleteFailuresHistory($scope.structure.id)
                    .then((res: AxiosResponse) => {
                        if (res.status === 200 || res.status === 201) {
                            vm.failureList = [];
                            toasts.confirm('viescolaire.trombinoscope.param.photo.failure.delete.success');
                        }
                        safeApply($scope);
                    })
                    .catch((_: AxiosError) => toasts.warning('viescolaire.trombinoscope.param.photo.failure.delete.error'));
            };

            vm.goToStudents = (): void => {
                template.open('trombinoscope', '../templates/viescolaire/param_etab_items/trombinoscope/param_trombinoscope_student_list');
            };


            $scope.$watch(() => $scope.structure, async () => {
                await initData();

            });

            $scope.$on(FAILURE_EVENTER.DELETE, async () => {
                await vm.getFailureList();
                safeApply($scope);
            });
        }]);
