export class Subject {
    id: string;
    idEtablissement: string;
    externalId: string;
    source: string;
    name: string;
    external_id_subject: string;
    sous_matieres: Array<Object>;

    constructor (subject?){
        if(subject) _.extend(this,subject);
    }
}
