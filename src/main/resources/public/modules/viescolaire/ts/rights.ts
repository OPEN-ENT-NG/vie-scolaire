const rights = {
    workflow: {
        adminChefEtab: 'fr.openent.DisplayController|view',
        importRetardAndAbsences: 'fr.openent.viescolaire.controller.ImportCsvController|importRetadsAndAbsences',
        periodYearManage: 'fr.openent.viescolaire.controller.PeriodeAnneeController|PeriodYearManage',
        timeSlotsManage: 'fr.openent.viescolaire.controller.TimeSlotController|getSlotProfilesByStructure',
        paramServices: 'fr.openent.viescolaire.controller.ServicesController|createService',
        paramTrombinoscope: 'fr.openent.viescolaire.controller.TrombinoscopeController|manageTrombinoscope',
        viescolaire1d: 'fr.openent.viescolaire.controller.FakeRight|viescolaire1d'
    }
}

export default rights;
