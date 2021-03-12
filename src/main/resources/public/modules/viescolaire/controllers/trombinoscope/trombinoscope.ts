import {ng, template} from 'entcore';
import {safeApply} from '../../../utils/functions/safeApply';

declare let window: any;

interface IViewModel {
    showImportPage: boolean;
}

export const trombinoscopeController = ng.controller('TrombinoscopeController',
    ['$scope', '$route', '$location', function ($scope, $route, $location) {

            const vm: IViewModel = this;
            vm.showImportPage = true;

            const initData = async (): Promise<void> => {
                template.open('trombinoscope', '../templates/viescolaire/param_etab_items/trombinoscope/param_trombinoscope_import');
                safeApply($scope);
            };

            $scope.$watch(() => $scope.structure, async () => {
                await initData();
            });
        }]);
