/**
 * Created by agnes.lapeyronnie on 15/09/2017.
 */
import{ng,model} from "entcore/entcore";
import { LSU, Classe, Responsable } from '../models/eval_teacher_mdl';

export let exportControleur = ng.controller('ExportController',['$scope',
    function($scope) {

      $scope.lsu = new LSU($scope.structure.id, $scope.evaluations.classes, $scope.structure.responsables);

      //Vue export du fichier xml

      //initialisation
      $scope.bSelectAllClasses = false;
      $scope.bSelectAllResponsables = false;

      $scope.pOFilterCtrl={
          bOFClasse : true,
          bOFResponsable : true
      };

      //function toutes les classes selectionnées ont leur attribut selected mis à la valeur  bSelectAllClasses
      $scope.switchAllClasses = function(oListe) {
            oListe.each(function(o) {
                o.selected = $scope.bSelectAllClasses;
            });
      };

        //selectionner seulement les classes sans les groupes
        $scope.criteriaMatch = () => {
            return function(classe) {
                return classe.type_groupe === 0;
            };
        };

        // Créer une fonction dans le $scope qui lance la récupération des responsables
        $scope.getResponsables = function () {
            $scope.structure.responsables.sync().then(() => {
                // On a fini la synchronisation
               $scope.lsu.responsable = $scope.structure.responsables.all[0].displayName
            });
        };

        $scope.getResponsables();

        $scope.switchAllResponsables = function(oListe) {
            oListe.each(function(o) {
                o.selected = $scope.bSelectAllResponsables;
            });
        };

        /**
         * Controle la validité des selections avant l'exportLSU
         */
        $scope.controleExportLSU = function(){
            return !(
                _.where($scope.lsu.classes.all, {selected: true}).length > 0 &&
                _.where($scope.lsu.responsables.all, {selected: true}).length > 0
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