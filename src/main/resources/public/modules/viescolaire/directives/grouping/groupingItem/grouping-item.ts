import {idiom, ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services/GroupingService";
import {Structure} from "../../../models/personnel/Structure";
import {Classe} from "../../../models/personnel/Classe";


interface IViewModel {
    $onInit(): any;

    $onDestroy(): any;

    updateGroupingItem(grouping: Grouping, name: string): void;

    deleteGroupingItem(grouping: Grouping): void;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    deleteGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    getAllClass(): Classe[];

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classet: Classe);

    groupingItem: Grouping;

    structure: Structure;

    groupingClassItem: GroupingClass;

    groupingClass: GroupingClass[];

    lang: typeof idiom;

}

class Controller implements ng.IController, IViewModel {
    $parent: any;
    viewModel: IViewModel;
    groupingItem: Grouping;
    structure: Structure;
    onDeleteGroupingItem: any;
    onUpdateGroupingItem: any;
    onAddGroupingAudienceItem: any;
    onDeleteGroupingAudienceItem: any;
    groupingClassItem: GroupingClass;
    groupingClass: GroupingClass[];
    classe: Classe;
    lang: typeof idiom;

    constructor(private $scope: IScope, private $location: ILocationService, private $window: IWindowService, private groupingService: GroupingService) {
        this.$scope['vm'] = this;
        this.lang = idiom;
    }

    $onInit = async () => {
        this.groupingClass.forEach(grouping => {
            this.groupingClassItem = this.groupingClass.find(groupingClass => groupingClass.grouping.id == this.groupingItem.id);
        })
        this.groupingItem.class.forEach(classeItem => {
            this.classe = this.structure.classes.find(classe => classe.id == classeItem.id);
            this.groupingClassItem.classes.push(this.classe);
        })
    };

    $onDestroy = async () => {

    };

    updateGroupingItem = (grouping: Grouping, name: string): void => {
        this.onUpdateGroupingItem()(this.$scope, grouping, name);
    }

    deleteGroupingItem = (grouping: Grouping): void => {
        this.onDeleteGroupingItem()(this.$scope, grouping);
    }

    addGroupingAudience = (grouping: Grouping, classOrGroup: Classe): void => {
        this.onAddGroupingAudienceItem()(this.$scope, grouping, classOrGroup)
    }

    deleteGroupingAudience = (grouping: Grouping, classOrGroup: Classe): void => {
        this.onDeleteGroupingAudienceItem()(this.$scope, grouping, classOrGroup)

    }

    getAllClass = (): Classe[] => {
        return this.structure.classes.all;
    }

    classeSelect(groupingClass: GroupingClass, grouping: Grouping): void {
        groupingClass.classes.forEach(classe => {
            if (grouping.class.length == 0) {
                this.addGroupingAudience(grouping, classe);

            } else {
                let classeForAudience = grouping.class.find(classeSelect => classeSelect == classe);
                if (!classeForAudience) {
                    this.addGroupingAudience(grouping, classe);
                }
            }
        })
    }

    classeUnselect(groupingClass: GroupingClass, grouping: Grouping): void {
        grouping.class.forEach(classe => {
            let classeTab = groupingClass.classes.find(classet => classet == classe);
            if (!classeTab) {
                this.deleteGroupingAudience(grouping, classe);
            }
        })
    }

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe) {
        groupingClass.classes = groupingClass.classes.filter(classe => classe.id != classeSelect.id);
        this.classeUnselect(groupingClass, grouping);
    }

}


function directive() {
    return {
        restrict: 'E',
        scope: {},
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/groupingItem/grouping-item.html`,
        controllerAs: 'vm',
        bindToController: {
            groupingItem: '=',
            groupingClass: '=',
            onUpdateGroupingItem: '&',
            onDeleteGroupingItem: '&',
            onAddGroupingAudienceItem: '&',
            onDeleteGroupingAudienceItem: '&',
            structure: '=',
        },
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

export const groupingItem = ng.directive('groupingItem', directive);

