import {model, idiom as lang, _, Behaviours} from 'entcore';

export class Utils {
    static canAccessCompetences () {

        /*console.log("Behaviours.applicationsBehaviours.competences !== undefined = " + (Behaviours.applicationsBehaviours.competences !== undefined));
        console.log("Behaviours.applicationsBehaviours.competences.rights !== undefined = " + (Behaviours.applicationsBehaviours.competences !== undefined && Behaviours.applicationsBehaviours.competences.rights !== undefined));
        console.log("Behaviours.applicationsBehaviours.competences.rights.workflow !== undefined = " + (Behaviours.applicationsBehaviours.competences !== undefined && Behaviours.applicationsBehaviours.competences.rights !== undefined && Behaviours.applicationsBehaviours.competences.rights.workflow !== undefined));
        */

        return Behaviours.applicationsBehaviours.competences !== undefined && Behaviours.applicationsBehaviours.competences.rights !== undefined
            && Behaviours.applicationsBehaviours.competences.rights.workflow !== undefined && model.me.hasWorkflow(Behaviours.applicationsBehaviours.competences.rights.workflow.access);
    }

    static canAccessPresences () {

        /*console.log("Behaviours.applicationsBehaviours.presences !== undefined = " + (Behaviours.applicationsBehaviours.presences !== undefined));
        console.log("Behaviours.applicationsBehaviours.presences.rights !== undefined = " + (Behaviours.applicationsBehaviours.presences !== undefined && Behaviours.applicationsBehaviours.presences.rights !== undefined));
        console.log("Behaviours.applicationsBehaviours.presences.rights.workflow !== undefined = " + (Behaviours.applicationsBehaviours.presences !== undefined && Behaviours.applicationsBehaviours.presences.rights !== undefined && Behaviours.applicationsBehaviours.presences.rights.workflow !== undefined));
        */
        return Behaviours.applicationsBehaviours.presences !== undefined && Behaviours.applicationsBehaviours.presences.rights !== undefined
            && Behaviours.applicationsBehaviours.presences.rights.workflow !== undefined && model.me.hasWorkflow(Behaviours.applicationsBehaviours.presences.rights.workflow.access);
    }


}