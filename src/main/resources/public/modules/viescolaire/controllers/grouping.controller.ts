import {idiom as lang, ng, notify, template} from "entcore";
import {GroupingService, GroupService} from "../services";
import {Grouping, GroupingClass} from "../models/common/Grouping";
import {Classe} from "../models/personnel/Classe";
import * as utils from "../../utils/functions/safeApply";
import {safeApply} from "../../utils/functions/safeApply";
import {ILocationService, IScope, IWindowService} from "angular";
import {vieScolaire} from "../models/vsco_personnel_mdl";
import {StudentDivision} from "../models/common/StudentDivision";

interface IViewModel {

    createGrouping(name: string): Promise<void>;

    updateGrouping(grouping: Grouping, name: string): Promise<void>;

    deleteGrouping(grouping: Grouping): Promise<void>;

    addGroupingAudience(grouping: Grouping, studentDivision: Classe): Promise<void>;

    deleteGroupingAudience(grouping: Grouping, studentDivision: Classe): Promise<void>;

    setAllGrouping(): Promise<void>;

    getAllClass(): Classe[];

    groupings: Array<Grouping>;

    groupingClass: GroupingClass[];

    audienceList: Classe[];

}


class Controller implements ng.IController, IViewModel {
    groupings: Array<Grouping>;
    groupingClass: GroupingClass[];
    audienceList: Classe[];

    constructor(private $scope: IScope,
                private $location: ILocationService,
                private $window: IWindowService,
                private groupingService: GroupingService,
                private groupService: GroupService) {
        this.$scope['vm'] = this;
    }

    $onInit = (): void => {
        this.groupings = [];
        this.groupingClass = [];
        this.audienceList = [];
        this.setAllGrouping();
        console.log(this.groupings);
        this.$scope.$watch(() => this.$scope['structure'], async () => {
            if (this.$scope['structure'] && this.$scope['structure'].id) {
                this.audienceList = (await this.groupService.getClasses(this.$scope['structure'].id)).data;
                this.audienceList.forEach((classe: Classe) => classe.toString = () => classe.name)
            }
            await this.initData();
        });

    }

    initData = async (): Promise<void> => {
        template.open('grouping', '../templates/viescolaire/param_etab_items/param-grouping.html');
        this.setAllGrouping();
        safeApply(this.$scope);
    }

    createGrouping = async (name: string): Promise<void> => {
        await this.groupingService.createGrouping(vieScolaire.structure.id, name)
            .then(res => {
                let grouping: Grouping = new Grouping(res.data.id, name, vieScolaire.structure.id, []);
                this.groupings.push(grouping);
                this.groupingClass.push({grouping: grouping, classes: [], errorClasses: [], savedClasses: []});
                notify.success(lang.translate('viescolaire.create.done'));
                utils.safeApply(this.$scope);
            }).catch(e => {
                notify.error(lang.translate('viescolaire.create.fail'));
                console.error(e);
            });
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
            this.groupings = this.groupings.filter((groupingFilter: Grouping) => groupingFilter.id != grouping.id);
            notify.success(lang.translate('viescolaire.delete.done'));
            utils.safeApply(this.$scope);
        } catch (e) {
            notify.error(lang.translate('viescolaire.delete.fail'));
            console.error(e);
        }
    }

    addGroupingAudience = async (grouping: Grouping, studentDivision: Classe): Promise<void> => {
        try {
            await this.groupingService.addGroupingAudience(grouping.id, studentDivision.id);
            grouping.student_divisions.push(studentDivision);
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.add.audience.fail'));
            console.error(e);
        }
    }

    deleteGroupingAudience = async (grouping: Grouping, studentDivision: Classe): Promise<void> => {
        try {
            await this.groupingService.deleteGroupingAudience(grouping.id, studentDivision.id);
            grouping.student_divisions = grouping.student_divisions.filter((studentDivisionFilter: StudentDivision) => studentDivisionFilter.id != studentDivision.id);
        } catch (e) {
            notify.error(lang.translate('viescolaire.grouping.delete.audience.fail'));
            console.error(e);
        }
    }

    setAllGrouping = async (): Promise<void> => {
        this.groupingService.getGroupingList(vieScolaire.structure.id)
            .then(listGroupings => {
                this.groupings = listGroupings;
                this.groupings.forEach((grouping: Grouping) => {
                    this.groupingClass.push({grouping: grouping, classes: [], errorClasses: [], savedClasses: []});
                });
                utils.safeApply(this.$scope);
            })
            .catch(e => {
                notify.error(lang.translate('viescolaire.get.fail'));
                console.error(e);
            })
    }

    getAllClass = (): Classe[] => {
        return this.audienceList ? this.audienceList : [];
    }

}

export const groupingController = ng.controller('groupingController',
    ['$scope', 'route', '$window', 'GroupingService', 'GroupService', Controller]);
