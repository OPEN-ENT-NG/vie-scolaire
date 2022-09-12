import {Classe} from "../personnel/Classe";
import {IGroup} from "./Group";
import {DefaultClasse} from "./DefaultClasse";
import {TimeSlot} from "./TimeSlots";
import {IStudentDivisionResponse, Student_division} from "./student_division";

export interface IGroupingItemResponse {
    id: string,
    name: string,
    student_divisions: IStudentDivisionResponse[],
    structure_id: string;
}

export interface IGroupingResponse {
    all: Array<IGroupingItemResponse>;
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
    student_divisions: Student_division[];
    structure_id: string;

    construct(id: string, name: string, structure: string, division: Student_division[]): Grouping {
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
        this.student_divisions = data.student_divisions.map((studentDivision: Student_division) => new Student_division(studentDivision));
        return this;
    }
}