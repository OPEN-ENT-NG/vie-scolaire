import {Behaviours, http} from 'entcore';

Behaviours.register('viescolaire', {
    rights: {
        workflow: {
            adminChefEtab: 'fr.openent.DisplayController|view',
            importRetardAndAbsences: 'fr.openent.viescolaire.controller.ImportCsvController|importRetadsAndAbsences'
        },
        resource: {}
    }
});