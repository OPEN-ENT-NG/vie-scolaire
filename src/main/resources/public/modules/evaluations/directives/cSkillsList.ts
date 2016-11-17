/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng, appPrefix} from 'entcore/entcore';

export let cSkillsList = ng.directive("cSkillsList", function(){
    return {
        restrict : 'E',
        scope : {
            data : '=',
            devoir : '=',
            functionFilter : '=',
            functionSearch : '=',
            enseignementsFilter :'=',
            competencesFilter: '=',
            search: '='
        },
        templateUrl : "/"+appPrefix+"/public/components/cSkillsList.html",
        controller : ['$scope', '$sce', function($scope, $sce){

            $scope.initCheckBox = function(item, parentItem){

                //item.nomHtml = item.nom;

                // on regarde sur l'objet competencesLastDevoirList pour detecter quand il est charge
                // et pouvoir deplier l'arbre des competences selectionnees lors du dernier devoir
                $scope.$watch('devoir.competencesLastDevoirList', function (newValue, oldValue) {
                    if (newValue !== oldValue) {
                        $scope.initCheckBox(item, parentItem);
                    }
                }, true);
                var bLastCompetence = (_.findWhere($scope.devoir.competencesLastDevoirList, {id_competence : item.id}) !== undefined);

                if(bLastCompetence) {
                    item.open = true;

                    var parent = item.composer;
                    while(parent !== undefined) {
                        parent.open = true;
                        parent = parent.composer;
                    }
                    $scope.safeApply();
                }
                return (item.selected = parentItem.enseignement && parentItem.enseignement.selected || item.selected || false);
            };

            $scope.initHeader = function(item){
                return (item.open = false);
            };

            $scope.safeApply = function(fn) {
                var phase = this.$root.$$phase;
                if(phase == '$apply' || phase == '$digest') {
                    if(fn && (typeof(fn) === 'function')) {
                        fn();
                    }
                } else {
                    this.$apply(fn);
                }
            };

            $scope.doNotApplySearchFilter = function(){
                $scope.search.haschange=false;
            };

            $scope.toggleCheckbox = function(item, parentItem){
                if(item.competences !== undefined && item.competences.all.length > 0){
                    $scope.$emit('checkConnaissances', item);
                }else{
                    $scope.$emit('checkParent', parentItem, item);
                }
            };

            $scope.$on('checkConnaissances', function(event, parentItem){
                return (parentItem.competences.each(function(e){e.selected = parentItem.selected;}));
            });

            // item pas utilise ici mais utilise dans la creation d'un devoir
            $scope.$on('checkParent', function(event, parentItem, item){
                return (parentItem.selected = parentItem.competences.every(function(e){ return e.selected === true; }));
            });

        }]
    };
});