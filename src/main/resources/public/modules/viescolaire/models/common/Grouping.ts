import {Classe} from "../personnel/Classe";
import {IGroup} from "./Group";
import {DefaultClasse} from "./DefaultClasse";
import {TimeSlot} from "./TimeSlots";

//will potentially be deleted
export interface Grouping {
    id: string,
    name: string,
    structureId: string,
    class: Classe[],
    group: IGroup[],

}


//will potentially be deleted
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

    constructor(name: string, structure: string) {
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

    //setters will be delete later for the back
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