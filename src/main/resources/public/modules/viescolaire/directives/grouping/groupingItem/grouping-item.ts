import {idiom, ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services/GroupingService";
import {Structure} from "../../../models/personnel/Structure";
import {Classe} from "../../../models/personnel/Classe";


interface IViewModel {

    updateGroupingItem(grouping: Grouping, name: string): void;

    deleteGroupingItem(grouping: Grouping): void;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    deleteGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    getAllClass(): Classe[];

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe): void;

    resetEdit(): void;

    groupingItem: Grouping;

    structure: Structure;

    groupingClassItem: GroupingClass;

    groupingClass: GroupingClass[];

    lang: typeof idiom;

}

class Controller implements ng.IController, IViewModel {
    groupingItem: Grouping;
    structure: Structure;
    onDeleteGroupingItem: () => (scope: IScope, grouping: Grouping) => void;
    onUpdateGroupingItem: () => (scope: IScope, grouping: Grouping, name: string) => void;
    onAddGroupingAudienceItem: () => (scope: IScope, grouping: Grouping, classOrGroup: Classe) => void;
    onDeleteGroupingAudienceItem: () => (scope: IScope, grouping: Grouping, classOrGroup: Classe) => void;
    groupingClassItem: GroupingClass;
    groupingClass: GroupingClass[];
    lang: typeof idiom;
    displayRenameGrouping: boolean = false;
    groupingTitle: string = "";

    constructor(private $scope: IScope, private $location: ILocationService, private $window: IWindowService, private groupingService: GroupingService) {
        this.$scope['vm'] = this;
        this.lang = idiom;
    }

    $onInit = async (): Promise<void> => {
        this.groupingClassItem = this.groupingClass.find(groupingClass => groupingClass.grouping.id == this.groupingItem.id);
        this.groupingClassItem.classes = this.structure.classes.filter((classe: Classe) => !!this.groupingItem.class.find(classeItem => classeItem.id === classe.id));
    };

    updateGroupingItem = (grouping: Grouping, name: string): void => {
        this.onUpdateGroupingItem()(this.$scope, grouping, name);
        this.resetEdit()
    }

    resetEdit = (): void => {
        this.displayRenameGrouping = false;
        this.groupingTitle = "";
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
        return this.structure && this.structure.classes && this.structure.classes.all ? this.structure.classes.all : [];
    }

    classeSelect(groupingClass: GroupingClass, grouping: Grouping): void {
        groupingClass.classes.forEach((classe: Classe) => {
            if (grouping.class.length == 0) {
                this.addGroupingAudience(grouping, classe);

            } else {
                let classeForAudience = grouping.class.find((classeSelect: Classe) => classeSelect == classe);
                if (!classeForAudience) {
                    this.addGroupingAudience(grouping, classe);
                }
            }
        })
    }

    classeUnselect(groupingClass: GroupingClass, grouping: Grouping): void {
        grouping.class.forEach((classe: Classe) => {
            let classeTab = groupingClass.classes.find((classeFind: Classe) => classeFind == classe);
            if (!classeTab) {
                this.deleteGroupingAudience(grouping, classe);
            }
        })
    }

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe) {
        groupingClass.classes = groupingClass.classes.filter((classe: Classe) => classe.id != classeSelect.id);
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
        }
    }
}

export const groupingItem = ng.directive('groupingItem', directive);

