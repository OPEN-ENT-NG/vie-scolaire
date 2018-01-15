/**
 * Retourne la liste des cours rapprochés
 * @param startMomentPeriod : début de la recherche des cours
 * @param endMomentPeriod : fin de la recherche des cours
 * @param structure : établissement en cours
 * @param evenements : événements sélectionnés
 * @param coursPostgres : cours SQL sélectionnés
 * @param coursMongo
 * @returns {any[]}
 */
export function checkRapprochementCoursCommon (startMomentPeriod, endMomentPeriod, structure, evenements, coursPostgres, coursMongo) {
    let iterationsCoursMongo = [];
    coursMongo.forEach(coursMongo => {
        coursMongo.startMoment = moment(coursMongo.startDate);
        coursMongo.endMoment = moment(coursMongo.endDate);

        let debutMomentIteration = moment(coursMongo.startMoment);
        let endMomentIteration = moment(coursMongo.startMoment.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));

        if (coursMongo.dayOfWeek !== undefined) {
            if (debutMomentIteration.isoWeekday() <= coursMongo.dayOfWeek) {
                debutMomentIteration = debutMomentIteration.isoWeekday(coursMongo.dayOfWeek);
                endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));
            } else {
                debutMomentIteration = debutMomentIteration.add(1, 'weeks').isoWeekday(coursMongo.dayOfWeek);
                endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));
            }
        } else {
            // Si dayOfWeek n'existe pas, alors on se base sur la startDate
            // et on considère que le cours est ponctuel, donc sa date de fin est la même que la startDate
            // en gardant sa date de fin
            endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + endMomentIteration.format('HH:mm'));
            coursMongo.endMoment = endMomentIteration.clone();
        }

        if (debutMomentIteration < endMomentIteration) {
            while (endMomentIteration <= coursMongo.endMoment) {
                // Si l'iteration est en dans la période on l'ajoute.
                if ((startMomentPeriod < debutMomentIteration && endMomentIteration < endMomentPeriod) || // Est à l'intérieur de la période
                    (debutMomentIteration < startMomentPeriod && endMomentPeriod < endMomentIteration) || // Englobe la période
                    !(endMomentPeriod < debutMomentIteration || startMomentPeriod > endMomentIteration)) { // Touche la période

                    iterationsCoursMongo.push({
                        idMongo: coursMongo._id,
                        startMoment: debutMomentIteration.clone(),
                        endMoment: endMomentIteration.clone(),
                        teacherIds: coursMongo.teacherIds,
                        roomLabels: coursMongo.roomLabels,
                        classes: coursMongo.classes,
                        subjectId: coursMongo.subjectId,
                        isFromMongo: true,
                    });
                }
                debutMomentIteration.add(7, 'days');
                endMomentIteration.add(7, 'days');
            }
        }
    });

    coursPostgres.forEach(coursPostgresTemp => {
        coursPostgresTemp.startMoment =  moment(coursPostgresTemp.timestamp_dt);
        coursPostgresTemp.endMoment =  moment(coursPostgresTemp.timestamp_fn);
        coursPostgresTemp.isFromMongo = false;
        coursPostgresTemp.subjectId = coursPostgres.id_matiere;
        coursPostgresTemp.evenements = evenements.filter(ev => ev.id_cours === coursPostgresTemp.id && ev.id_type !== 1);
        coursPostgresTemp.classeNames = structure.classes.all.filter(classe => coursPostgresTemp.classeIds.includes(classe.id)).map(a => a.name);
    });

    let arrayCours = [];
    iterationsCoursMongo.forEach(iteration => {
        let coursPostgresFound = false;
        for (let i = 0; i < coursPostgres.length; i++) {
            let coursPostgresTemp = coursPostgres[i];
            if (iterationAndCoursIsSame(iteration, coursPostgresTemp)) {
                coursPostgresFound = true;
                coursPostgresTemp.isAlreadyFound = true;
                arrayCours.push(coursPostgresTemp);
                break;
            }
        }
        if (!coursPostgresFound) {
            arrayCours.push(iteration);
        }
    });

    coursPostgres.forEach(coursPostgresTemp => {
        if (!coursPostgresTemp.isAlreadyFound) {
            arrayCours.push(coursPostgresTemp);
        }
    });

    return arrayCours;
}

/**
 * Détermine si des cours Mongo et SQL sont égaux
 * @param iterationMongo
 * @param coursPostgres
 * @returns {boolean}
 */
function iterationAndCoursIsSame(iterationMongo, coursPostgres) {
    // Le cours est liée à l'iteration si sa date est égale à la date de l'itation (+ ou - la tolerance (15min))
    let toleranceMinute = 15;
    if (!(coursPostgres.startMoment.isBetween(
            iterationMongo.startMoment.clone().subtract(toleranceMinute, 'minute'),
            iterationMongo.startMoment.clone().add(toleranceMinute, 'minute'))
            &&
            coursPostgres.endMoment.isBetween(
                iterationMongo.endMoment.clone().subtract(toleranceMinute, 'minute'),
                iterationMongo.endMoment.clone().add(toleranceMinute, 'minute')))) {
        return false;
    }

    // Le prof correspond
    if (!_.isEqual(iterationMongo.teacherIds.sort(), coursPostgres.teacherIds.sort())) {
        return false;
    }

    // La classe correspond
    if (!_.isEqual(iterationMongo.classes.sort(), coursPostgres.classeNames.sort())) {
        return false;
    }

    // Check si c'est la meme matiere
    if (coursPostgres.id_matiere !== iterationMongo.subjectId) {
        return false;
    }

    return true;
}