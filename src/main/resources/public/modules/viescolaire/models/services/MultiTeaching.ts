import http from 'axios';
import {Teacher} from "../common/Teacher";
import {ServiceClasse} from "./ServiceClasse";
import {Subject} from "../common/Subject";
import {moment, toasts} from "entcore";

export class MultiTeaching {

    idStructure: string;
    isCoteaching: boolean;
    subject: Subject;
    mainTeacher: Teacher;
    coTeachers: any[];
    newCoTeachers: Teacher[];
    classesCoteaching: ServiceClasse[];
    start_date: Date;
    end_date: Date;
    entered_end_date: Date;

    constructor (idStructure, teacher, subject, isCoteaching, coTeachers?, groups?) {
        this.idStructure = idStructure;
        this.subject = new Subject(subject) ;
        this.mainTeacher = new Teacher(teacher);
        this.coTeachers = [];
        this.newCoTeachers = [];
        this.classesCoteaching = [];
        this.isCoteaching = isCoteaching;
        if(isCoteaching !== undefined){
            this.isCoteaching = isCoteaching;
            if(!isCoteaching){
                this.start_date = new Date();
                this.end_date = moment(new Date()).add(1,'day');
                this.entered_end_date = this.end_date ;
            }
        }
        if(coTeachers){
            this.coTeachers = coTeachers;
            if(coTeachers.length > 0){
                if(!this.isCoteaching){
                    this.start_date = new Date(_.first(coTeachers).start_date);
                    this.end_date = new Date(_.first(coTeachers).end_date);
                    this.entered_end_date = new Date(_.first(coTeachers).entered_end_date);
                }
            }
        }
    }


    toJson(){
        if(this.isCoteaching){
            return{
                main_teacher_id: this.mainTeacher.id,
                second_teacher_ids: _.map(this.newCoTeachers, (teacher) => {return teacher.id;}),
                subject_id: this.subject.id,
                class_or_group_ids: _.map(this.classesCoteaching, (classe) => {return classe.id;}),
                structure_id: this.idStructure,
                co_teaching: this.isCoteaching
            }
        }else{
            return{
                main_teacher_id: this.mainTeacher.id,
                second_teacher_ids: [_.first(this.newCoTeachers).id],
                subject_id: this.subject.id,
                class_or_group_ids: _.map(this.classesCoteaching, (classe) => {return classe.id;}),
                start_date: moment(this.start_date).format("YYYY-MM-DD"),
                end_date: moment(this.end_date).format("YYYY-MM-DD"),
                entered_end_date: moment(this.entered_end_date).format("YYYY-MM-DD"),
                structure_id: this.idStructure,
                co_teaching: this.isCoteaching
            }
        }
    }

    async addCoTeaching(){
        try{

            let {status,data}= await http.post('viescolaire/multiteaching/create', this.toJson());

            if( status === 200){
                toasts.confirm('evaluation.coteaching.create');
            }else{
                toasts.warning('evaluation.coteaching.create.error');
            }

        } catch ( e ) {
            toasts.warning('evaluation.coteaching.create.error');
            console.log(e);
        }

    }

    async deleteCoTeaching(multiTeachingIds){
        try{

            return http.put('viescolaire/multiteaching/delete', {ids : multiTeachingIds });


        }catch (e) {
            toasts.warning('evaluation.coteaching.delete.error');
            console.log(e);
        }
    }
}