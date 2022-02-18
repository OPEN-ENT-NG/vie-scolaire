/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import {routes, ng} from 'entcore';

/* Directives */
import {sticky} from '../../utils/directives/sticky';
import {cSkillsBubble} from '../../utils/directives/cSkillsBubble';
import {asyncAutocomplete} from '../../utils/directives/async-autocomplete';
import {infiniteScroll} from '../../utils/directives/infinite-scroll';
import {Toasts, Toast} from '../../utils/directives/toasts';
import {failureItem} from '../../utils/directives/trombinoscope/failure-item';
import {studentPicture} from '../../utils/directives/trombinoscope/student-picture';
import {timeslotAudience} from '../directives/timeslotAudience/timeslot-audience';

/* Controllers */
import {viescolaireController} from '../controllers/vsco_personnel_ctrl';
import {adminVieScolaireController} from '../controllers/vsco_acu_personnel_ctrl';
import {periodeAnneeController} from '../controllers/vsco_periodeAnnee_ctrl';
import {timeSlotsController} from '../controllers/vsco_time_slots_ctrl';
import {evalAcuTeacherController} from '../controllers/vsco_service_ctrl';
import {trombinoscopeController} from '../controllers/trombinoscope/trombinoscope';
import {trombinoscopeImportController} from '../controllers/trombinoscope/trombinoscope-import';
import {trombinoscopeStudentListController} from '../controllers/trombinoscope/trombinoscope-student-list';

/* Filters */

import {periodeSearch} from '../filtres/periodeSearch';
import * as services from '../services';

ng.controllers.push(viescolaireController);
ng.controllers.push(adminVieScolaireController);
ng.controllers.push(periodeAnneeController);
ng.controllers.push(timeSlotsController);
ng.controllers.push(trombinoscopeController);
ng.controllers.push(trombinoscopeImportController);
ng.controllers.push(trombinoscopeStudentListController);
ng.controllers.push(evalAcuTeacherController);

ng.filters.push(periodeSearch);

ng.directives.push(sticky);
ng.directives.push(cSkillsBubble);
ng.directives.push(asyncAutocomplete);
ng.directives.push(infiniteScroll);
ng.directives.push(Toasts);
ng.directives.push(Toast);
ng.directives.push(failureItem);
ng.directives.push(studentPicture);
ng.directives.push(timeslotAudience);

for (let service in services) {
    ng.services.push(services[service]);
}


routes.define(function($routeProvider) {
    $routeProvider
        .when('/viescolaire/accueil', {
            action: 'accueil'
        }).otherwise({
        redirectTo : '/viescolaire/accueil'
    });
});



