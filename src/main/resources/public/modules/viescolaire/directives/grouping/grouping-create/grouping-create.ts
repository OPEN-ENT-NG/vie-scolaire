import {ng} from "entcore";
import {Grouping} from "../../../models/common/grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services";


interface IViewModel {

    createGrouping(name: string): void;
}

class Controller implements ng.IController, IViewModel {
    viewModel: IViewModel;
    groupingItem: Grouping;
    private onCreateGrouping: () => (name: string) => void;
    groupingName: string = "";


    constructor(private $scope: IScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService) {
        this.$scope['vm'] = this;
    }

    createGrouping = (name: string): void => {
        this.onCreateGrouping()(name);
        this.groupingName = "";
    }
}

function directive() {
    return {
        restrict: 'E',
        scope: {
            onCreateGrouping: `&`,
        },
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/grouping-create/grouping-create.html`,
        controllerAs: 'vm',
        bindToController: true,
        replace: false,
        controller: ['$scope', '$location', '$window', Controller],
        link: function (scope: ng.IScope,
                        element: ng.IAugmentedJQuery,
                        attrs: ng.IAttributes,
                        vm: ng.IController) {
        }
    }
}

export const groupingCreate = ng.directive('groupingCreate', directive);
