/**
 * Created by agnes.lapeyronnie on 15/09/2017.
 */
import{ng} from "entcore/entcore";
import { LSU, Classe, Responsable } from '../models/eval_teacher_mdl';

export let exportControleur = ng.controller('ExportController',['$scope',
    function($scope) {

      $scope.lsu = new LSU($scope.structure.id);

        // Créer une fonction dans le $scope qui lance la récupération des résponsables
        $scope.getResponsables = function () {
            $scope.structure.responsables.sync().then(() => {
                // On a fini la synchronisation
               $scope.lsu.responsable = $scope.structure.responsables.all[0].displayName
            });
        };

        $scope.getResponsables();
        $scope.criteriaMatch = () => {
            return function(classe) {
                return classe.type_groupe === 0;
            };
        };
        $scope.addClasse = () => {
            $scope.lsu.classes.push($scope.lsu.classe);
        };

        $scope.addResponsable = () => {
            $scope.lsu.responsables.push($scope.lsu.responsable);
        };
        $scope.deselectClasse = (classe) => {
            $scope.lsu.classes = _.without($scope.lsu.classes, classe);
        };

        $scope.deselectResponsable = (responsable) => {
            $scope.lsu.responsables = _.without($scope.lsu.responsables, responsable);
        };
        /**
         * Controle la validité des selections avant l'exportLSU
         */
        $scope.controleExportLSU = function(){
            return !(
                $scope.lsu.responsables.length > 0
                && $scope.lsu.classes.length > 0
            );
        };
        $scope.exportLSU = ()=> {
          $scope.lsu.export()
        };

    }
]);




// Si 1 structure =>  Initialiser lsu.structureId à l'id de la structure
// if($scope.evaluations.structures.all.length == 1){
//$scope.lsu.idStructure = $scope.structure.id;
// }