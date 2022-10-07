import {idiom, ng} from "entcore";
import {Grouping, GroupingClass} from "../../../models/common/grouping";
import {ILocationService, IScope, IWindowService} from "angular";
import {GroupingService} from "../../../services";
import {Classe} from "../../../models/personnel/Classe";
import {Student_division} from "../../../models/common/student_division";


interface IViewModel extends ng.IController {

    updateGroupingItem(grouping: Grouping, name: string): void;

    deleteGroupingItem(grouping: Grouping): void;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    deleteGroupingAudience(grouping: Grouping, classOrGroup: Classe): void;

    getAllClass(): Classe[];

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe): void;

    openEdit(): void;

    resetEdit(): void;

    groupingItem: Grouping;

    audienceList: Array<Classe>

    groupingClassItem: GroupingClass;

    groupingClass: GroupingClass[];

    lang: typeof idiom;
}

interface IGroupingItemProps {
    groupingItem: Grouping,
    audienceList: Array<Classe>,
    groupingClassItem: GroupingClass,

    onDeleteGroupingItem: () => (grouping: Grouping) => void;
    onUpdateGroupingItem: () => (grouping: Grouping, name: string) => void;
    onAddGroupingAudienceItem: () => (grouping: Grouping, studentDivision: Student_division) => void;
    onDeleteGroupingAudienceItem: () => (grouping: Grouping, studentDivision: Student_division) => void;
}

interface IGroupingItemScope extends IScope {
    vm: IGroupingItemProps;
}

class Controller implements IViewModel {
    groupingItem: Grouping;
    audienceList: Array<Classe>;
    groupingClassItem: GroupingClass;
    groupingClass: GroupingClass[];
    lang: typeof idiom;
    displayRenameGrouping: boolean = false;
    groupingTitle: string = "";

    constructor(private $scope: IGroupingItemScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService) {
        this.lang = idiom;
    }

    $onInit = async (): Promise<void> => {
        this.groupingClassItem = this.groupingClass.find((groupingClass: GroupingClass) => groupingClass.grouping.id == this.groupingItem.id);
        this.groupingClassItem.classes = this.audienceList.filter((classe: Classe) => !!this.groupingItem.student_divisions.find((classeItem: Student_division) => classeItem.id === classe.id));
    };

    updateGroupingItem = (grouping: Grouping, name: string): void => {
        this.$scope.vm.onUpdateGroupingItem()(grouping, name);
        this.resetEdit()
    }

    openEdit(): void {
        this.displayRenameGrouping = true;
        this.groupingTitle = this.groupingItem.name;
    }

    resetEdit = (): void => {
        this.displayRenameGrouping = false;
        this.groupingTitle = "";
    }

    deleteGroupingItem = (grouping: Grouping): void => {
        this.$scope.vm.onDeleteGroupingItem()(grouping);
    }

    addGroupingAudience = (grouping: Grouping, classOrGroup: Classe): void => {
        let studentDivision: Student_division = new Student_division({id: classOrGroup.id, name: classOrGroup.name});
        this.$scope.vm.onAddGroupingAudienceItem()(grouping, studentDivision);
    }

    deleteGroupingAudience = (grouping: Grouping, studentDivision: Student_division): void => {
        this.$scope.vm.onDeleteGroupingAudienceItem()(grouping, studentDivision);
    }

    getAllClass = (): Classe[] => {
        return this.audienceList ? this.audienceList : [];
    }

    classSelect(groupingClass: GroupingClass, grouping: Grouping): void {
        groupingClass.classes.forEach((classe: Classe) => {
            if (grouping.student_divisions.length == 0) {
                this.addGroupingAudience(grouping, classe);
            } else {
                let classeForAudience: Student_division = grouping.student_divisions.find((studentSelect: Student_division) => studentSelect.id == classe.id);
                if (!classeForAudience) {
                    this.addGroupingAudience(grouping, classe);
                }
            }
        })
    }

    classUnselect(groupingClass: GroupingClass, grouping: Grouping): void {
        grouping.student_divisions.forEach((studentDivision: Student_division) => {
            let classeTab: Classe = groupingClass.classes.find((classeFind: Classe) => classeFind.id == studentDivision.id);
            if (!classeTab) {
                this.deleteGroupingAudience(grouping, studentDivision);
            }
        })
    }

    deleteClassItem(groupingClass: GroupingClass, grouping: Grouping, classeSelect: Classe): void {
        groupingClass.classes = groupingClass.classes.filter((classe: Classe) => classe.id != classeSelect.id);
        this.classUnselect(groupingClass, grouping);
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
                        vm: IViewModel) {
        }
    }
}

export const groupingItem = ng.directive('groupingItem', directive);