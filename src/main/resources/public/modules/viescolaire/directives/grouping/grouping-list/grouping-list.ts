import {ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services";
import {Classe} from "../../../models/personnel/Classe";
import {StudentDivision} from "../../../models/common/StudentDivision";

interface IViewModel {
    updateGroupingList(grouping: Grouping, name: string): void;

    deleteGroupingList(grouping: Grouping): void;

    addGroupingAudienceList(grouping: Grouping, classOrGroup: Classe): void;

    deleteGroupingAudienceList(grouping: Grouping, classOrGroup: Classe): void;
}

interface IGroupingListProps {
    groupings: Array<Grouping>,
    groupingClassList: GroupingClass[],
    audienceList: Array<Classe>,
    onUpdateGrouping: () => (grouping: Grouping, name: string) => Promise<void>,
    onDeleteGrouping: () => (grouping: Grouping) => void,
    onAddGroupingAudience: () => (grouping: Grouping, studentDivision: StudentDivision) => Promise<void>,
    onDeleteGroupingAudience: () => (grouping: Grouping, studentDivision: StudentDivision) => void,
}

interface IGroupingItemScope extends IScope {
    vm: IGroupingListProps;
}

class Controller implements ng.IController {

    constructor(private $scope: IGroupingItemScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService) {
    }
}

function directive() {
    return {
        restrict: 'E',
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/grouping-list/grouping-list.html`,
        scope: {
            groupings: '=',
            groupingClassList: '=',
            audienceList: '=',
            onUpdateGrouping: '&',
            onDeleteGrouping: '&',
            onAddGroupingAudience: '&',
            onDeleteGroupingAudience: '&',
        },
        controllerAs: 'vm',
        bindToController: true,
        controller: ['$scope', '$location', '$window', Controller],
        link: function (scope: IGroupingItemScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: IViewModel) {

            vm.updateGroupingList = (grouping: Grouping, name: string): void => {
                scope.vm.onUpdateGrouping()(grouping, name);
            }

            vm.deleteGroupingList = (grouping: Grouping): void => {
                scope.vm.onDeleteGrouping()(grouping);
            }

            vm.addGroupingAudienceList = (grouping: Grouping, classOrGroup: Classe): void => {
                scope.vm.onAddGroupingAudience()(grouping, classOrGroup);
            }

            vm.deleteGroupingAudienceList = (grouping: Grouping, classOrGroup: Classe): void => {
                scope.vm.onDeleteGroupingAudience()(grouping, classOrGroup);
            }
        }
    }
}

export const groupingList = ng.directive('groupingList', directive);