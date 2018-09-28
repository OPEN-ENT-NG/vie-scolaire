import {Behaviours, http} from 'entcore';

Behaviours.register('viescolaire', {
    rights: {
        workflow: {
            adminChefEtab: 'fr.openent.viescolaire.controller.DisplayController|view',
            importRetardAndAbsences: 'fr.openent.viescolaire.controller.ImportCsvController|importRetadsAndAbsences'
        },
        resource: {}
    }
});
