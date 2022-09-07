import {Classe} from "../personnel/Classe";
import {IGroup} from "./Group";
import {DefaultClasse} from "./DefaultClasse";
import {TimeSlot} from "./TimeSlots";
import {IStudentDivisionResponse, StudentDivision} from "./StudentDivision";

//will potentially be deleted
export interface IGroupingItemResponse {
    id: string,
    name: string,
    student_divisions: IStudentDivisionResponse[],
    structure_id: string;
}

export interface IGroupingResponse {
    all: Array<IGroupingItemResponse>;
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

    construct(id: string, name: string, structure: string, division: StudentDivision[]): Grouping {
        this.id = id;
        this.name = name;
        this.structure_id = structure;
        this.student_divisions = division;
        return this;
    }

    build(data: IGroupingItemResponse): Grouping {
        this.id = data.id;
        this.name = data.name;
        this.structure_id = data.structure_id;
        this.student_divisions = data.student_divisions.map((studentDivision: StudentDivision) => new StudentDivision(studentDivision));
        return this;
    }
}