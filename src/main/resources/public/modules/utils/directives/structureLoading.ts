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

import { ng, idiom as lang } from 'entcore';

export let structureLoader = ng.directive('structureLoader', function() {
    return {
        restrict: 'E',
        transclude: true,
        scope: {
            structureName: '=',
            display: '='
        },
        template: '<div id="structure-loader" class="overlay" ng-show="display">' +
        '<div>' +
        '<h4 class="content-loader"><i18n>viescolaire.structure.load</i18n> ' +
        '[[structureName]] ' +
        '<i18n>viescolaire.structure.load.end</i18n></h4>' +
        '</div>' +
        '</div>',
        replace: true
    };
});