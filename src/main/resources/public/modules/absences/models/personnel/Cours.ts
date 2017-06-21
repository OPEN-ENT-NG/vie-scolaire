import {DefaultCours} from "../common/DefaultCours";
import {Model} from "../../../entcore/modelDefinitions";

/**
 * Created by rahnir on 09/06/2017.
 */


export class Cours extends DefaultCours implements Model{
    etatcours: string;
    id_appel: number;
}