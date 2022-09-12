export interface IStudentDivisionResponse {
    id: string;
    name: string;
}

export class Student_division {
    id: string;
    name: string;

    constructor(data: IStudentDivisionResponse) {
        this.id = data.id;
        this.name = data.name;
    }
}