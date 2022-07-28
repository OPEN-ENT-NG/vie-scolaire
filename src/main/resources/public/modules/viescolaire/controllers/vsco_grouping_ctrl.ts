import {ng, notify, idiom as lang} from "entcore";
import {GroupingService} from "../services/GroupingService";
import {Grouping, GroupingClass, Groupings} from "../models/common/Grouping";
import {Classe} from "../models/personnel/Classe";
import {Structure} from "../models/personnel/Structure";
import * as utils from "../../utils/functions/safeApply";
import {ILocationService, IScope, IWindowService} from "angular";

interface IViewModel {

    createGrouping(name: String): Promise<void>;

    updateGrouping(grouping: Grouping, name: string): Promise<void>;

    deleteGrouping(grouping: Grouping): Promise<void>;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): Promise<void>;

    deleteGroupingAudience(grouping: Grouping, classOrGroupId: Classe): Promise<void>;

    getAllGrouping(): Promise<void>;

    getAllClass(): Classe[];

    getGrouping(): Grouping[];

    groupings: Groupings;

    structure: Structure;

    groupingClass: GroupingClass[];

}


class Controller implements ng.IController, IViewModel {
    groupings: Groupings;
    structure: Structure;
    groupingClass: GroupingClass[];

    constructor(private $scope: IScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService) {
        this.$scope['vm'] = this;
    }

    $onInit = (): void => {
        this.groupingClass = [];
        this.getAllGrouping();
    }

    //test for front, it will be delete later
    getGrouping = (): Grouping[] => {
        let grouping: Grouping = new Grouping("test", "");
        grouping.setId("1");
        let classe: Classe = new Classe();
        classe.name = "31";
        classe.id = "3bb8e550-feb3-4b14-94b8-73289db9cc7d";
        classe.type_groupe = 0;
        let tabClasse = [classe];
        let grouping2: Grouping = new Grouping("test2", "");
        grouping2.setId("2");
        let group: Grouping[] = [grouping, grouping2];


        grouping.setClass(tabClasse);
        group.forEach((grouping: Grouping) => {
            this.groupingClass.push({grouping: grouping, classes: [], errorClasses: [], savedClasses: []});
        });
        return group;
    }

    createGrouping = async (name: string): Promise<void> => {
        let structureId: string = model.me.structures[0];
        try {
            await this.groupingService.createGrouping(structureId, name);
            let grouping: Grouping = new Grouping(name, "");//test for front, it will be delete later
            this.groupings.all.push(grouping);
            notify.success(lang.translate('viescolaire.create.done'));
            utils.safeApply(this.$scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.create.fail'));
            console.error(e);
        }
    }

    updateGrouping = async (grouping: Grouping, name: string): Promise<void> => {
        try {
            await this.groupingService.updateGrouping(grouping.id, name);
            grouping.setName(name);
            notify.success(lang.translate('viescolaire.update.done'));
            utils.safeApply(this.$scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.update.fail'));
            console.error(e);
        }
    }

    deleteGrouping = async (grouping: Grouping): Promise<void> => {
        try {
            await this.groupingService.deleteGrouping(grouping.id);
            this.groupings.all = this.groupings.all.filter(groupingFilter => groupingFilter.name != grouping.name);//test for front will be delete later
            notify.success(lang.translate('viescolaire.delete.done'));
            utils.safeApply(this.$scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.delete.fail'));
            console.error(e);
        }
    }

    addGroupingAudience = async (grouping: Grouping, classOrGroup: Classe): Promise<void> => {
        try {
            await this.groupingService.addGroupingAudience(grouping.id, classOrGroup.id);
            grouping.class.push(classOrGroup);//test for front
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.add.audience.fail'));
            console.error(e);
        }
    }

    deleteGroupingAudience = async (grouping: Grouping, classOrGroup: Classe): Promise<void> => {
        try {
            await this.groupingService.deleteGroupingAudience(grouping.id, classOrGroup.id);
            grouping.class = grouping.class.filter((classe: Classe) => classe != classOrGroup);//test for front, it will be delete later
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.delete.audience.fail'));
            console.error(e);
        }
    }

    getAllGrouping = async (): Promise<void> => {
        try {
            let grouping: Grouping[] = this.getGrouping();//test for front will be change later
            this.groupings = new Groupings(grouping);
        } catch (e) {
            notify.error(lang.translate('viescolaire.delete.fail'));
            console.error(e);
        }

    }

    getAllClass = (): Classe[] => {
        return this.structure && this.structure.classes && this.structure.classes.all ? this.structure.classes.all : [];
    }

}

export const groupingController = ng.controller('groupingController',
    ['$scope', 'route', '$window', 'GroupingService', Controller]);
