import {ng, notify, idiom as lang} from "entcore";
import {groupingService} from "../services/GroupingService";
import {Grouping, GroupingClass, Groupings} from "../models/common/Grouping";
import {Classe} from "../models/personnel/Classe";
import {Structure} from "../models/personnel/Structure";
import * as utils from "../../utils/functions/safeApply";

interface ViewModel {
    $onInit(): any;

    $onDestroy(): any;

    createGrouping(name: String): Promise<void>;

    updateGrouping(grouping: Grouping, name: string): Promise<void>;

    deleteGrouping(grouping: Grouping): Promise<void>;

    addGroupingAudience(grouping: Grouping, classOrGroup: Classe): Promise<void>;

    deleteGroupingAudience(grouping: Grouping, classOrGroupId: Classe): Promise<void>;

    getAllGrouping(): Promise<void>;

    toto(): void;

    getAllClass(): Classe[];

    getGrouping(): any;

    groupings: Groupings;

    structure: Structure;

    groupingClass: GroupingClass[];

}

export const groupingController = ng.controller('groupingController', ['$scope', 'route', 'model', function ($scope, route) {
    const vm: ViewModel = this;
    vm.groupingClass = [];

    vm.$onInit = () => {
        vm.getAllGrouping();
    }

    //test for front, it will be delete later
    vm.getGrouping = (): any => {
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
        group.forEach(grouping => {
            vm.groupingClass.push({grouping: grouping, classes: [], errorClasses: [], savedClasses: []});
        });
        return group;
    }

    vm.createGrouping = async (name: string): Promise<void> => {
        let structureId: string = model.me.structures[0];
        try {
            await groupingService.createGrouping(structureId, name);
            let grouping: Grouping = new Grouping(name, "");//test for front, it will be delete later
            vm.groupings.all.push(grouping);
            notify.success(lang.translate('viescolaire.create.done'));
            utils.safeApply($scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.create.fail'));
        }
    }

    vm.updateGrouping = async (grouping: Grouping, name: string): Promise<void> => {
        try {
            await groupingService.updateGrouping(grouping.id, name);
            grouping.setName(name);
            notify.success(lang.translate('viescolaire.update.done'));
            utils.safeApply($scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.update.fail'));
        }
    }

    vm.deleteGrouping = async (grouping: Grouping): Promise<void> => {
        try {
            await groupingService.deleteGrouping(grouping.id);
            vm.groupings.all = vm.groupings.all.filter(groupingFilter => groupingFilter.name != grouping.name);//test for front will be delete later
            notify.success(lang.translate('viescolaire.delete.done'));
            utils.safeApply($scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.delete.fail'));
        }
    }

    vm.addGroupingAudience = async (grouping: Grouping, classOrGroup: Classe): Promise<void> => {
        try {
            await groupingService.addGroupingAudience(grouping.id, classOrGroup.id);
            grouping.class.push(classOrGroup);//test for front
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.add.audience.fail'));
        }
    }

    vm.deleteGroupingAudience = async (grouping: Grouping, classOrGroup: Classe): Promise<void> => {
        try {
            await groupingService.deleteGroupingAudience(grouping.id, classOrGroup.id);
            grouping.class = grouping.class.filter(classe => classe != classOrGroup);//test for front, it will be delete later
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.delete.audience.fail'));
        }
    }

    vm.getAllGrouping = async (): Promise<void> => {
        try {
            let grouping: Grouping[] = vm.getGrouping();//test for front will be change later
            vm.groupings = new Groupings(grouping);
        } catch (e) {
            notify.error(lang.translate('viescolaire.delete.fail'));
        }

    }

    vm.getAllClass = (): Classe[] => {
        return vm.structure.classes.all;
    }
}]);