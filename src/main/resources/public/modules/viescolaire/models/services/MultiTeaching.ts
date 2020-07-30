import http from 'axios';
import {_, moment, toasts} from "entcore";

export class MultiTeaching {

    structure_id: string;
    main_teacher_id: string;
    second_teacher_id: string;
    subject_id: string;
    idsAndIdsGroups: any[];
    displayName: string;
    start_date: Date;
    end_date: Date;
    entered_end_date: Date;
    isCoteaching: boolean;
    is_visible: boolean;

   constructor(o){
       this.structure_id = o.structure_id;
       this.main_teacher_id = o.main_teacher_id;
       this.subject_id = o.subject_id;
       this.isCoteaching = o.isCoteaching;
       this.is_visible = o.is_visible;
       if(o.idsAndIdsGroups &&  o.second_teacher_id && o.displayName) {
           this.idsAndIdsGroups = o.idsAndIdsGroups;
           this.second_teacher_id = o.second_teacher_id;
           this.displayName = o.displayName;
       }
       if(!o.is_coteaching){
           if(o.start_date !=null){
               this.start_date = new Date (o.start_date);
               this.end_date = new Date (o.end_date);
               this.entered_end_date = new Date(o.entered_end_date);
           }else{
               this.start_date = new Date();
               this.end_date = moment(new Date()).add(1,'day');
               this.entered_end_date = this.end_date ;
           }
       }
   }

    toJson(newCoTeachers, idsClassesCoteaching, idsAndIdsGroupsOldTeacher?){
        if(this.isCoteaching){
            return{
                structure_id: this.structure_id,
                main_teacher_id: this.main_teacher_id,
                second_teacher_ids: _.pluck(newCoTeachers, "id"),
                subject_id: this.subject_id,
                class_or_group_ids: idsClassesCoteaching,
                co_teaching: this.isCoteaching,
                is_visible: this.is_visible
            }
        } else {
            let substituteTeacher = {
                structure_id: this.structure_id,
                main_teacher_id: this.main_teacher_id,
                second_teacher_ids: [_.first(newCoTeachers).id],
                subject_id: this.subject_id,
                class_or_group_ids: idsClassesCoteaching,
                start_date: moment(this.start_date).format("YYYY-MM-DD"),
                end_date: moment(this.end_date).format("YYYY-MM-DD"),
                entered_end_date: moment(this.entered_end_date).format("YYYY-MM-DD"),
                co_teaching: this.isCoteaching,
                is_visible: this.is_visible
            };
            if(idsAndIdsGroupsOldTeacher){
                _.extend(substituteTeacher,{"ids_multiTeachingToUpdate": _.pluck(_.filter(idsAndIdsGroupsOldTeacher, (object)=> {
                    return _.contains(_.intersection(_.pluck(idsAndIdsGroupsOldTeacher,"idGroup"),idsClassesCoteaching), object.idGroup)
                }), "id") });
                _.extend(substituteTeacher,{"ids_multiTeachingToDelete": _.pluck(_.filter(idsAndIdsGroupsOldTeacher, (object) => {
                        return _.contains( _.difference(_.pluck(idsAndIdsGroupsOldTeacher,"idGroup"),idsClassesCoteaching),  object.idGroup)
                    }),"id")});

            }
            return substituteTeacher;
        }
    }

    async addCoTeaching(newCoTeachers, classesCoteaching){
        try{
            let idsClassesCoteaching =  _.pluck(classesCoteaching, "id");
            let {status}= await http.post('viescolaire/multiteaching/create',
                this.toJson(newCoTeachers, idsClassesCoteaching));

            if(status === 200){
                toasts.confirm('evaluation.coteaching.create');
            } else {
                toasts.warning('evaluation.coteaching.create.error');
            }

        } catch (e) {
            toasts.warning('evaluation.coteaching.create.error');
            console.log(e);
        }
    }

    async updateSubstituteTeacher(newCoTeachers, classesCoteaching, oldSubstituteTeacher){
       try{
           let idsClassesCoteaching = _.pluck(classesCoteaching, "id");
           let idsAndIdsGroupsOldTeacher = oldSubstituteTeacher.idsAndIdsGroups;
           let {status} = await http.put('viescolaire/multiteaching/update',
               this.toJson(newCoTeachers, idsClassesCoteaching, idsAndIdsGroupsOldTeacher));
           if(status != 200){
               toasts.warning('evaluation.coteaching.create.error');
           }
       } catch (e) {
           console.log(e);
           toasts.warning('evaluation.coteaching.create.error');
       }
    }

   async deleteCoTeaching(){
        try{
            let multiTeachingIds = _.pluck(this.idsAndIdsGroups, "id");
            return  http.put('viescolaire/multiteaching/delete', {ids : multiTeachingIds });

        }catch (e) {
            toasts.warning('evaluation.coteaching.delete.error');
            console.log(e);
        }
    }
}