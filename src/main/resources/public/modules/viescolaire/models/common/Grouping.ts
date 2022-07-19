import {Classe} from "../personnel/Classe";
import {IGroup} from "./Group";
import {DefaultClasse} from "./DefaultClasse";
import {TimeSlot} from "./TimeSlots";

export interface Grouping {
    id: string,
    name: string,
    structureId: string,
    class: Classe[],
    group: IGroup[],

}

export interface GroupingClass {
    grouping: Grouping;
    classes: Classe[];
    savedClasses: Classe[];
    errorClasses: Classe[];

}

export class Grouping {
    id: string;
    name: string;
    structureId: string;
    class: Classe[];
    group: IGroup[];

    constructor(name, structure) {
        this.name = name;
        this.structureId = structure;
        this.class = [];
        this.group = [];

    }

    toJson() {
        return {
            id: this.id,
            name: this.name,
            structureId: this.structureId,
            class: this.class,
            group: this.group
        }
    }

    setId(id: string): void {
        this.id = id;
    }

    setName(name: string): void {
        this.name = name;
    }

    setStructureId(structureId: string): void {
        this.structureId = structureId;
    }

    setClass(classTab: Classe[]): void {
        this.class = classTab;
    }

    setGroup(groupTab: IGroup[]) {
        this.group = groupTab
    }
}

export class Groupings {
    all: Grouping[]

    constructor(groupingTab: Grouping[]) {
        this.all = groupingTab;
    }
}