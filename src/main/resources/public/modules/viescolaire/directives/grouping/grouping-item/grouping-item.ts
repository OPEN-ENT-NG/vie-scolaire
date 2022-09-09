import {idiom, ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/Grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services";
import {Classe} from "../../../models/personnel/Classe";
import {StudentDivision} from "../../../models/common/StudentDivision";


interface IViewModel {

    updateGroupingItem(grouping: Grouping, name: string): void;

    deleteGroupingItem(grouping: Grouping): void;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): Promise<void>;

    deleteGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    getAllClass(): Classe[];

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe): void;

    resetEdit(): void;

    groupingItem: Grouping;

    audienceList: Array<Classe>

    groupingClassItem: GroupingClass;

    groupingClass: GroupingClass[];

    lang: typeof idiom;

}

class Controller implements ng.IController, IViewModel {
    groupingItem: Grouping;
    audienceList: Array<Classe>;
    private onDeleteGroupingItem: () => (scope: IScope, grouping: Grouping) => void;
    private onUpdateGroupingItem: () => (scope: IScope, grouping: Grouping, name: string) => Promise<void>;
    private onAddGroupingAudienceItem: () => (scope: IScope, grouping: Grouping, studentDivision: StudentDivision) => Promise<void>;
    private onDeleteGroupingAudienceItem: () => (scope: IScope, grouping: Grouping, studentDivision: StudentDivision) => void;
    groupingClassItem: GroupingClass;
    groupingClass: GroupingClass[];
    lang: typeof idiom;
    displayRenameGrouping: boolean = false;
    groupingTitle: string = "";

    constructor(private $scope: IScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService) {
        this.$scope['vm'] = this;
        this.lang = idiom;
    }

    $onInit = async (): Promise<void> => {
        this.groupingClassItem = this.groupingClass.find((groupingClass: GroupingClass) => groupingClass.grouping.id == this.groupingItem.id);
        this.groupingClassItem.classes = this.audienceList.filter((classe: Classe) => !!this.groupingItem.student_divisions.find((classeItem: StudentDivision) => classeItem.id === classe.id));
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

    addGroupingAudience = async (grouping: Grouping, classOrGroup: Classe): Promise<void> => {
        let studentDivision: StudentDivision = new StudentDivision(classOrGroup.id, classOrGroup.name);
        await this.onAddGroupingAudienceItem()(this.$scope, grouping, studentDivision);
    }

    deleteGroupingAudience = (grouping: Grouping, studentDivision: StudentDivision): void => {
        this.onDeleteGroupingAudienceItem()(this.$scope, grouping, studentDivision);
    }

    getAllClass = (): Classe[] => {
        return this.audienceList ? this.audienceList : [];
    }

    classeSelect(groupingClass: GroupingClass, grouping: Grouping): void {
        groupingClass.classes.forEach((classe: Classe) => {
            if (grouping.student_divisions.length == 0) {
                this.addGroupingAudience(grouping, classe);
            } else {
                let classeForAudience: StudentDivision = grouping.student_divisions.find((studentSelect: StudentDivision) => studentSelect.id == classe.id);
                if (!classeForAudience) {
                    this.addGroupingAudience(grouping, classe);
                }
            }
        })
    }

    classeUnselect(groupingClass: GroupingClass, grouping: Grouping): void {
        grouping.student_divisions.forEach((studentDivision: StudentDivision) => {
            let classeTab: Classe = groupingClass.classes.find((classeFind: Classe) => classeFind.id == studentDivision.id);
            if (!classeTab) {
                this.deleteGroupingAudience(grouping, studentDivision);
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
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/grouping/grouping-item/grouping-item.html`,
        controllerAs: 'vm',
        bindToController: {
            groupingItem: '=',
            groupingClass: '=',
            audienceList: '=',
            onUpdateGroupingItem: '&',
            onDeleteGroupingItem: '&',
            onAddGroupingAudienceItem: '&',
            onDeleteGroupingAudienceItem: '&',
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

