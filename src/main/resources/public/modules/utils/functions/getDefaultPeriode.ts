export function getDefaultPeriode (periodes: any) {
    let PeriodeParD = new Date().toISOString();

    for (let i = 0; i < periodes.length; i++) {
        if (PeriodeParD >= periodes[i].timestamp_dt && PeriodeParD <= periodes[i].timestamp_fn) {
            return periodes[i];
        }
    }
    return '';
}