export class Teacher {
    id:string;
    lastName: string;
    firstName: string;
    displayName: string;
    functions: Array<string>;
    classes: Array<string>;
    subjectTaught: Array<string>;
    groups: Array<string>;
    selected: boolean;

    constructor (teacher?){
        if(teacher) _.extend(this, teacher)
    }
}