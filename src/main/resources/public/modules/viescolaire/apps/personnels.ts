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

import { routes, ng } from 'entcore';

import { sticky } from '../../utils/directives/sticky';
import { cSkillsBubble } from '../../utils/directives/cSkillsBubble';
import {viescolaireController} from '../controllers/vsco_personnel_ctrl';
import {adminVieScolaireController} from '../controllers/vsco_acu_personnel_ctrl';
import {periodeSearch} from '../filtres/periodeSearch';

ng.controllers.push(viescolaireController);
ng.controllers.push(adminVieScolaireController);

ng.filters.push(periodeSearch);

ng.directives.push(sticky);
ng.directives.push(cSkillsBubble);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/viescolaire/accueil', {
            action: 'accueil'
        }).otherwise({
        redirectTo : '/viescolaire/accueil'
    });
});



