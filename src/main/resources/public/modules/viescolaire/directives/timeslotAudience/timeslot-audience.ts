import {TimeSlot, TimeslotClass, TimeSlots} from "../../models/common/TimeSlots";
import {idiom, ng, toasts} from "entcore";
import {ILocationService, IScope, IWindowService} from "angular";
import * as utils from "../../../utils/functions/safeApply";
import {Classe} from "../../models/personnel/Classe";
import {Structure} from "../../models/personnel/Structure";
import {timeslotClasseService} from "../../services/TimeslotClasseService";

interface IViewModel {
    selectTimeslot(): void;

    getAllClasse(): Classe[];

    classeSelect(timeslotClass: TimeslotClass): void;

    classeUnselect(timeslotClass: TimeslotClass): void;

    deleteAllAudienceFromTimeslot(timeslotClass: TimeslotClass): void;

    deleteClassTimeslot(classe: Classe, timeslotClasse: TimeslotClass): void;

    ifClasseHasAlreadyTimeslot(classe: Classe): boolean;

    isClasseIsOnError(classe: Classe, timeslotClasse: TimeslotClass): boolean;

    lang: typeof idiom;
    addTimeSlotInProgress: boolean;
    timeSlot: TimeSlot;
    selectableTimeSlot: TimeSlot[];
    listTimeSlotsClasse: TimeslotClass[];
    structure: Structure;
    structureTimeslot: TimeSlots;
}

class Controller implements ng.IController, IViewModel {
    lang: typeof idiom;
    addTimeSlotInProgress: boolean;
    timeSlot: TimeSlot;
    selectableTimeSlot: TimeSlot[];
    listTimeSlotsClasse: TimeslotClass[];
    structure: Structure;
    structureTimeslot: TimeSlots;

    constructor(private $scope: IScope,
                private $location: ILocationService,
                private $window: IWindowService) {
        this.$scope['vm'] = this;
        this.lang = idiom;
    }

    $onInit(): void {
        this.selectableTimeSlot = [];
        this.listTimeSlotsClasse = [];
        this.addTimeSlotInProgress = false;
        this.$scope.$watchCollection(() => this.structureTimeslot.all, async () => {
            this.selectableTimeSlot = [];
            this.listTimeSlotsClasse = [];
            this.addTimeSlotInProgress = false;
            this.syncData();
        });
    }

    private async syncData(): Promise<void> {
        this.structureTimeslot.all.forEach((timeslot: TimeSlot) => {
            this.selectableTimeSlot = [...this.structureTimeslot.all];
            //Load all association for each timeslot
            timeslot.syncClasseAssociation().then(() => {
                this.loadTimeslotData(timeslot);
                utils.safeApply(this.$scope);
            });
        })
    }

    private loadTimeslotData(timeslot: TimeSlot): void {
        if (timeslot.classes.length > 0) {
            const classeObject: Classe[] = this.structure.classes.all.filter((classe: Classe) => !!timeslot.classes.find((el: string) => el == classe.id))
            this.listTimeSlotsClasse.push({
                timeSlot: timeslot,
                classes: [...classeObject].sort((el1: Classe, el2: Classe) => el1.name.localeCompare(el2.name)),
                errorClasses: [],
                savedClasses: [...classeObject]
            });
            if (this.structure.classes.all.length > 0) {
                this.selectableTimeSlot = this.selectableTimeSlot.filter((el: TimeSlot) => el != timeslot);
            }
        }
    }

    selectTimeslot(): void {
        this.addTimeSlotInProgress = false;
        this.listTimeSlotsClasse.push({timeSlot: this.timeSlot, classes: [], errorClasses: [], savedClasses: []});
        this.selectableTimeSlot = this.selectableTimeSlot.filter((timeSlot: TimeSlot) => timeSlot != this.timeSlot);
        this.timeSlot = undefined
        utils.safeApply(this.$scope);
    }

    getAllClasse(): Classe[] {
        return this.structure.classes.all;
    }

    classeSelect(timeslotClass: TimeslotClass): void {
        //Filter to get only new selected classe
        timeslotClass.classes.filter((classe: Classe) => !timeslotClass.savedClasses.find((el: Classe) => el == classe)
            && !timeslotClass.errorClasses.find((el: Classe) => el == classe))
            .forEach(classe => {
                if (this.ifClasseHasAlreadyTimeslot(classe)) {
                    timeslotClass.errorClasses.push(classe);
                } else {
                    timeslotClasseService.createOrUpdateClassTimeslot(timeslotClass.timeSlot._id, classe.id).then(() => {
                        timeslotClass.savedClasses.push(classe);
                        utils.safeApply(this.$scope);
                    }).catch(() => {
                        toasts.warning(`viescolaire.association.error`);
                        timeslotClass.errorClasses.push(classe);
                    })
                }
            });
    }

    classeUnselect(timeslotClasse: TimeslotClass): void {
        //Filter to get only deselected classe
        const classeMustBeDelete: Classe[] = timeslotClasse.savedClasses
            .concat(timeslotClasse.errorClasses)
            .filter((el: Classe) => !timeslotClasse.classes.find((classe: Classe) => el == classe))
        classeMustBeDelete.forEach((classe: Classe) => this.deleteClassTimeslot(classe, timeslotClasse))
    }

    deleteAllAudienceFromTimeslot(timeslotClass: TimeslotClass): void {
        timeslotClasseService.deleteAllAudienceFromTimeslot(timeslotClass.timeSlot._id).then(() => {
            this.listTimeSlotsClasse = this.listTimeSlotsClasse.filter((el: TimeslotClass) => el != timeslotClass)
            this.selectableTimeSlot.push(timeslotClass.timeSlot);
            this.selectableTimeSlot.sort((el1: TimeSlot, el2: TimeSlot) => el1.name.localeCompare(el2.name));
            utils.safeApply(this.$scope);
        }).catch(() => toasts.warning(`viescolaire.association.error`));
    }

    deleteClassTimeslot(classe: Classe, timeslotClasse: TimeslotClass): void {
        timeslotClasse.classes = timeslotClasse.classes.filter(el => el != classe);
        if (!!timeslotClasse.savedClasses.find((el: Classe) => classe == el)) {
            timeslotClasseService.deleteClassTimeslot(classe.id).then(() => {
                timeslotClasse.savedClasses = timeslotClasse.savedClasses.filter((el: Classe) => el != classe);
                utils.safeApply(this.$scope);
            }).catch(() => toasts.warning(`viescolaire.association.error`));
        } else {
            timeslotClasse.errorClasses = timeslotClasse.errorClasses.filter((el: Classe) => el != classe);
        }
    }

    ifClasseHasAlreadyTimeslot(classe: Classe): boolean {
        return !!this.listTimeSlotsClasse.find((el: TimeslotClass) => !!el.savedClasses.find((elClasse: Classe) => elClasse == classe))
    }

    isClasseIsOnError(classe: Classe, timeslotClasse: TimeslotClass): boolean {
        return !!timeslotClasse.errorClasses.find((elClasse: Classe) => elClasse == classe);
    }
}

function directive() {
    return {
        restrict: 'E',
        scope: {
            structure: '=',
            structureTimeslot: '=',
        },
        templateUrl: `/viescolaire/public/modules/viescolaire/directives/timeslotAudience/timeslot-audience.html`,
        controllerAs: 'vm',
        bindToController: true,
        controller: ['$scope', '$location', '$window', Controller]
    }
}

export const timeslotAudience = ng.directive('timeslotAudience', directive);