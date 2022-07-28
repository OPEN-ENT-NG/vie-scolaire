import {ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services/GroupingService";
import {Structure} from "../../../models/personnel/Structure";
import {Classe} from "../../../models/personnel/Classe";


interface IViewModel {

    updateGroupingList(scope: IScope, grouping: Grouping, name: string): void;

    deleteGroupingList(scope: IScope, grouping: Grouping): void;

    addGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void;

    deleteGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void;

    groupings: Grouping[];

    groupingInfo: Grouping;

    structureList: Structure;

    groupingClassList: GroupingClass[];
}

class Controller implements ng.IController, IViewModel {
    groupings: Grouping[];
    groupingInfo: Grouping;
    structureList: Structure;
    groupingClassList: GroupingClass[];
    onUpdateGrouping: () => (grouping: Grouping, name: string) => void;
    onDeleteGrouping: () => (grouping: Grouping) => void;
    onAddGroupingAudience: () => (grouping: Grouping, classOrGroup: Classe) => void;
    onDeleteGroupingAudience: () => (grouping: Grouping, classOrGroup: Classe) => void;

    constructor(private $scope: IScope, private $location: ILocationService, private $window: IWindowService, private groupingService: GroupingService) {
        this.$scope['vm'] = this;
    }

    updateGroupingList(scope: IScope, grouping: Grouping, name: string): void {
        scope.$parent.$eval(scope.$parent["vm"].onUpdateGrouping()(grouping, name));
    }

    deleteGroupingList(scope: IScope, grouping: Grouping): void {
        scope.$parent.$eval(scope.$parent["vm"].onDeleteGrouping()(grouping));
    }

    addGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void {
        scope.$parent.$eval(scope.$parent["vm"].onAddGroupingAudience()(grouping, classOrGroup));
    }

    deleteGroupingAudienceList(scope: IScope, grouping: Grouping, classOrGroup: Classe): void {
        scope.$parent.$eval(scope.$parent["vm"].onDeleteGroupingAudience()(grouping, classOrGroup));
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