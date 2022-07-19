import {ng} from "entcore";
import {Grouping} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services/GroupingService";


interface IViewModel {
    $onInit(): any;

    $onDestroy(): any;

    createGrouping(nom): void;

    onCreateGrouping(): void;
}


class Controller implements ng.IController, IViewModel {
    $parent: any;
    viewModel: IViewModel;
    groupingItem: Grouping;
    onCreateGrouping: any;


    constructor(private $scope: IScope, private $location: ILocationService, private $window: IWindowService, private groupingService: GroupingService) {
    }

    $onInit = async () => {

    };

    $onDestroy = async () => {

    };

    createGrouping = (name: string): void => {
        this.onCreateGrouping()(name);
    }

}

function directive() {
    return {
        restrict: 'E',
        scope: {
            onCreateGrouping: `&`,
        },
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/groupingCreate/grouping-create.html`,
        controllerAs: 'vm',
        bindToController: true,
        replace: false,
        controller: ['$scope', '$location', '$window', Controller],
        link: function (scope: ng.IScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: ng.IController) {
            console.log("link data: ", vm);
            console.log("link scope: ", scope);
        }
    }
}

export const groupingCreate = ng.directive('groupingCreate', directive);
