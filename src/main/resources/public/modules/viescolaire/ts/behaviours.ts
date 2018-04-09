import {Behaviours, http} from 'entcore';

Behaviours.register('viescolaire', {
    rights: {
        workflow: {
            exportLSU: 'fr.openent.DisplayController|view',
        },
        resource: {}
    }
});
