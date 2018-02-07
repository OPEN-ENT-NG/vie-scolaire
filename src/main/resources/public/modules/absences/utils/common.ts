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
import {Cours} from '../models/shared/Cours';
import {moment} from 'entcore';

export function checkRapprochementCoursCommon (startMomentPeriod, endMomentPeriod, structure, evenements,
                                               arrayCoursPostgresRaw, arrayCoursMongoRaw) {

    let arrayIterationMongo: Cours[] = [];
    let arrayCoursPostgres: Cours[] = [];

    arrayCoursMongoRaw.forEach(coursMongo => {
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
                    !(endMomentIteration < startMomentPeriod || endMomentPeriod < debutMomentIteration)) { // Touche la période

                    // let iterationMongo = new Cours(undefined, debutMomentIteration.clone(), endMomentIteration.clone());
                    let iterationMongo = new Cours();
                    iterationMongo.startMoment = debutMomentIteration.clone();
                    iterationMongo.endMoment = endMomentIteration.clone();
                    iterationMongo._id = coursMongo._id;
                    iterationMongo.roomLabels = coursMongo.roomLabels;
                    iterationMongo.subjectId = coursMongo.subjectId;
                    iterationMongo.teacherIds = coursMongo.teacherIds;
                    iterationMongo.classeNames = coursMongo.classes;
                    iterationMongo.classeIds = structure.classes.all.filter(classe => _.contains(iterationMongo.classeNames, classe.name)).map(a => a.id);
                    iterationMongo.isFromMongo = true;
                    iterationMongo.dayOfWeek = coursMongo.dayOfWeek;
                    arrayIterationMongo.push(iterationMongo);
                }
                debutMomentIteration.add(7, 'days');
                endMomentIteration.add(7, 'days');
            }
        }
    });

    // On met en forme les objets bruts en objet de type Cours.
    arrayCoursPostgresRaw.forEach(coursPostgresRaw => {
        let coursPostgres = new Cours();
        coursPostgres.startMoment = moment(coursPostgresRaw.timestamp_dt);
        coursPostgres.endMoment = moment(coursPostgresRaw.timestamp_fn);
        coursPostgres.id = coursPostgresRaw.id;
        coursPostgres.id_appel = coursPostgresRaw.id_appel;
        coursPostgres.roomLabels = coursPostgresRaw.roomLabels;
        coursPostgres.subjectId = coursPostgresRaw.id_matiere;
        coursPostgres.teacherIds = coursPostgresRaw.teacherIds;
        coursPostgres.classeIds = coursPostgresRaw.classeIds;
        coursPostgres.timestamp_dt = coursPostgresRaw.timestamp_dt;
        coursPostgres.timestamp_fn = coursPostgresRaw.timestamp_fn;
        if (evenements !== undefined) {
            coursPostgres.evenements = evenements.filter(ev => ev.id_cours === coursPostgres.id && ev.id_type !== 1);
        }
        coursPostgres.classeNames = structure.classes.all.filter(classe => coursPostgresRaw.classeIds.includes(classe.id)).map(a => a.name);
        coursPostgres.isFromMongo = false;
        arrayCoursPostgres.push(coursPostgres);
    });


    let arrayCours: Cours[] = [];
    arrayIterationMongo.forEach(iteration => {
        let coursPostgresFound = false;
        for (let i = 0; i < arrayCoursPostgres.length; i++) {
            let coursPostgresTemp = arrayCoursPostgres[i];
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

    arrayCoursPostgres.forEach(coursPostgresTemp => {
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
    if (!_.isEqual(iterationMongo.classeNames.sort(), coursPostgres.classeNames.sort())) {
        return false;
    }

    // Check si c'est la meme matiere
    if (coursPostgres.subjectId !== iterationMongo.subjectId) {
        return false;
    }

    return true;
}