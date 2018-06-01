import {model, idiom as lang, _, Behaviours} from 'entcore';

export class Utils {
    static canAccessCompetences () {
        return Behaviours.applicationsBehaviours.competences !== undefined && Behaviours.applicationsBehaviours.competences.rights !== undefined
            && Behaviours.applicationsBehaviours.competences.rights.workflow !== undefined && model.me.hasWorkflow(Behaviours.applicationsBehaviours.competences.rights.workflow.access);
    }

    static canAccessPresences () {
        return Behaviours.applicationsBehaviours.presences !== undefined && Behaviours.applicationsBehaviours.presences.rights !== undefined
            && Behaviours.applicationsBehaviours.presences.rights.workflow !== undefined && model.me.hasWorkflow(Behaviours.applicationsBehaviours.presences.rights.workflow.access);
    }


}