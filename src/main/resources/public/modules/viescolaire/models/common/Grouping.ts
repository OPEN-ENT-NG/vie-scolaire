import {Classe} from "../personnel/Classe";
import {IGroup} from "./Group";
import {DefaultClasse} from "./DefaultClasse";
import {TimeSlot} from "./TimeSlots";
import {StudentDivision} from "./StudentDivision";

//will potentially be deleted
export interface IGrouping extends Grouping{
    id: string,
    name: string,
    student_division: StudentDivision[],
    structure_id: string;
}

export interface IGroupingResponse {
    all: Array<IGrouping>;
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
    student_divisions: StudentDivision[];
    structure_id: string;

    constructor(id: string, name: string, structure: string, division: StudentDivision[]) {
        this.id = id;
        this.name = name;
        this.structure_id = structure;
        this.student_divisions = division;
    }

    toJson() {
        return {
            id: this.id,
            name: this.name,
            structureId: this.structure_id,
            division: this.student_divisions
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
        this.structure_id = structureId;
    }

    setDivision(division: StudentDivision[]): void {
        this.student_divisions = division;
    }

}

export class Groupings {
    all: Grouping[]
    groupingResponse: IGroupingResponse;

    constructor(groupingTab?: Grouping[]) {
        this.all = groupingTab;
    }

    async buildTest(data: Grouping[]): Promise<void> {
        this.all = [];
        data.forEach((grouping: Grouping) => {
            this.all.push(grouping);
        })
    }
}