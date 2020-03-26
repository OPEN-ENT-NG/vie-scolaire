/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, CGI, 2016.
 *     This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation (version 3 of the License).
 *   For the sake of explanation, any module that communicate over native
 *   Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 *   license and could be license under its own terms. This is merely considered
 *   normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import {Model, Collection, http, _} from 'entcore';
import {Mix} from "entcore-toolkit";
declare let bundle:any;

export class ServiceClasse {
    id : string;
    name : string;
    externalId : string;
    type_groupe : number;
    type_groupe_libelle : string;
    selected : boolean;

    toString() {
        return this.name;
    }

    public static  libelle = {
        CLASSE:'Classe',
        GROUPE: "Groupe d'enseignement",
        GROUPE_MANUEL: "Groupe manuel"
    };

    public static type = {
        CLASSE: 0,
        GROUPE: 1,
        GROUPE_MANUEL: 2
    };

    constructor(o:any){
        this.id = o.id;
        this.name = o.name;
        this.externalId = o.externalId;
        this.type_groupe = o.type_groupe;
        this.type_groupe_libelle = o.type_groupe_libelle;
    }

    public static get_type_groupe_libelle = (classe) => {
        let libelleClasse;

        if ( classe.type_groupe === ServiceClasse.type.CLASSE) {
            libelleClasse = ServiceClasse.libelle.CLASSE;
        } else if ( classe.type_groupe === ServiceClasse.type.GROUPE) {
            libelleClasse = ServiceClasse.libelle.GROUPE;
        }else if ( classe.type_groupe === ServiceClasse.type.GROUPE_MANUEL) {
            libelleClasse = ServiceClasse.libelle.GROUPE_MANUEL;
        }
        return libelleClasse;

    };


}