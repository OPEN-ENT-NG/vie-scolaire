import { DefaultEnseignant } from '../common/DefaultEnseignant';

export class Enseignant extends DefaultEnseignant {
    firstName: string;
    lastName: string;

    toString = () : string => {
        return this.lastName + " " + this.firstName;
    }
}