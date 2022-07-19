import {ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services/GroupingService";
import {Structure} from "../../../models/personnel/Structure";
import {Classe} from "../../../models/personnel/Classe";


interface IViewModel {
    $onInit(): any;

    $onDestroy(): any;

    getGroupings(name): void;

    onGetGroupings(): void;

    updateGroupingList(scope: IScope, grouping: Grouping, name: string): void;

    onUpdateGrouping(): void;

    deleteGroupingList(scope: IScope, grouping: Grouping): void;

    onDeleteGrouping(): void;

    addGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void;

    onAddGroupingAudience(): void;

    deleteGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void;

    onDeleteGroupingAudience(): void;

    groupings: Grouping[];

    groupingInfo: Grouping;

    structureList: Structure;

    groupingClassList: GroupingClass[];
}

class Controller implements ng.IController, IViewModel {
    $parent: any;
    viewModel: IViewModel;
    groupings: Grouping[];
    groupingInfo: Grouping;
    structureList: Structure;
    groupingClassList: GroupingClass[];

    constructor(private $scope: IScope, private $location: ILocationService, private $window: IWindowService, private groupingService: GroupingService) {
        this.$scope['vm'] = this;
    }

    $onInit(): any {

    }

    $onDestroy(): any {

    }

    getGroupings(name): void {

    }

    onGetGroupings(): void {

    }

    updateGroupingList(scope: IScope, grouping: Grouping, name: string): void {
        scope.$parent.$eval((<any>scope.$parent).vm.onUpdateGrouping()(grouping, name));
    }

    onUpdateGrouping(): void {

    }

    deleteGroupingList(scope: IScope, grouping: Grouping): void {
        scope.$parent.$eval((<any>scope.$parent).vm.onDeleteGrouping()(grouping));
    }

    onDeleteGrouping(): void {

    }

    addGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void {
        scope.$parent.$eval((<any>scope.$parent).vm.onAddGroupingAudience()(grouping, classOrGroup));
    }

    onAddGroupingAudience(): void {

    }

    deleteGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void {
        scope.$parent.$eval((<any>scope.$parent).vm.onDeleteGroupingAudience()(grouping, classOrGroup));
    }

    onDeleteGroupingAudience(): void {

    }

}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/groupingList/grouping-list.html`,
        scope: {
            groupings: '=',
            groupingClassList: '=',
            onUpdateGrouping: '&',
            onDeleteGrouping: '&',
            onAddGroupingAudience: '&',
            onDeleteGroupingAudience: '&',
            structureList: '=',
        },
        controllerAs: 'vm',
        bindToController: true,
        controller: ['$scope', '$location', '$window', Controller],
        link: function (scope: ng.IScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: ng.IController) {
        }
    }
}

export const groupingList = ng.directive('groupingList', directive);