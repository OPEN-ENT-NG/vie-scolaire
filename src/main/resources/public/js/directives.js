/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

/**
 * Created by ledunoiss on 08/08/2016.
 */
var angularDirectives = {
    addDirectives: function(){
        module.directive('cNavigable', function(){
            return {
                restrict : 'A',
                link: function(scope, element, attrs) {

                    scope.getNext = function(obj){
                        var row = obj.closest('.navigable-inputs-row');

                    };
                    scope.findAncestor = function(el, cls) {
                        while ((el = el.parentElement) && !el.classList.contains(cls));
                        return el;
                    };

                    scope.findChildren = function(el, cls){
                        var children = [];
                        for(var i = 0 ; i < el.children.length; i++){
                            if(scope.hasClass(el.children[i], cls)){
                                children.push(el.children[i]);
                            }
                        }
                        return children;
                    };

                    scope.hasClass = function(el, cls){
                        return el.className.indexOf(cls) > -1;
                    };

                    scope.findIndex = function(el, row){
                        var i;
                        for(i = 0; i < row.length; i++){
                            if(row[i] === el){
                                return i;
                            }
                        }
                        return -1;
                    };

                    scope.findInput = function(row){
                        for(var i = 0; i < row.children.length; i++){
                            if(row.children[i].tagName === 'INPUT'){
                                return row.children[i];
                            }
                        }
                        return null;
                    };

                    scope.findInputs = function(row){
                        var inputs = [];
                        for(var i = 0 ; i < row.children.length; i++){
                            if(row.children[i].tagName === 'INPUT'){
                                inputs.push(row.children[i]);
                            }
                        }
                        return inputs;
                    };

                    scope.findNavigableRow = function(row){
                        return (scope.findChildren(row, 'navigable-inputs-row'))[0];
                    };

                    element.bind('keydown', function(event){
                        var keys = {
                            enter : 13,
                            arrow : {left: 37, up: 38, right: 39, down: 40}
                        };
                        var key = event.which;

                        if ($.inArray(key, [keys.arrow.left, keys.arrow.up, keys.arrow.right, keys.arrow.down, keys.enter]) < 0) { return; }
                        var input = event.target;
                        var td = scope.findAncestor(event.target, 'navigable-cell');
                        var row = scope.findAncestor(td, 'navigable-inputs-row');
                        var children = scope.findChildren(row, 'navigable-cell');
                        var index = scope.findIndex(td, children);
                        var moveTo = null;
                        switch(key){
                            case keys.arrow.left:{
                                if (input.selectionStart === 0) {
                                    if(index > 0){
                                        moveTo = children[index-1];
                                    }
                                }
                                break;
                            }
                            case keys.arrow.right:{
                                if (input.selectionEnd == input.value.length) {
                                    if(index < row.children.length){
                                        moveTo = children[index+1];
                                    }
                                }
                                break;
                            }
                            case keys.arrow.up:
                            case keys.enter:
                            case keys.arrow.down:{
                                var tr = scope.findAncestor(td, 'navigable-row');
                                var pos = scope.findIndex(td, children);
                                var expandChildren = scope.findChildren(scope.findAncestor(tr, 'expandable-liste'),'navigable-row');
                                var trIndex = scope.findIndex(tr, expandChildren);
                                var moveToRow = null;
                                if (key == keys.arrow.down || key == keys.enter) {
                                    if(trIndex < expandChildren.length -1){
                                        moveToRow = expandChildren[trIndex+1];
                                    }
                                }
                                else if (key == keys.arrow.up) {
                                    if(trIndex > 0){
                                        moveToRow = expandChildren[trIndex-1];
                                    }
                                }
                                if(moveToRow !== null){
                                    if (moveToRow.children.length) {
                                        var targets = [];
                                        var navigableCells = scope.findChildren(scope.findNavigableRow(moveToRow), 'navigable-cell');
                                        for(var i = 0 ; i < navigableCells.length; i++){
                                            targets.push(scope.findInput(navigableCells[i]));
                                        }
                                        moveTo = navigableCells[pos];
                                    }
                                }
                                break;
                            }
                            /*case keys.enter:{
                             var focusedElement = $(input);
                             var nextElement = focusedElement.parent().next();
                             if (nextElement.find('input').length>0){
                             nextElement.find('input').focus();
                             }else{
                             nextElement = nextElement.parent().next().find('input').first();
                             nextElement.focus();
                             }
                             break;
                             }*/
                        }
                        if (moveTo) {
                            event.preventDefault();
                            input = scope.findInput(moveTo);
                            input.focus();
                            input.select();
                        }

                    });
                }
            };
        });
        module.directive('cNavigaTable', function(){
            return {
                restrict : 'A',
                link: function(scope, element, attrs) {
                    element.bind('keydown', function(event){
                        var keys = {
                            enter : 13,
                            arrow : {left: 37, up: 38, right: 39, down: 40}
                        };
                        var key = event.which;

                        if ($.inArray(key, [keys.arrow.left, keys.arrow.up, keys.arrow.right, keys.arrow.down, keys.enter]) < 0) { return; }
                        var input = event.target;
                        var td = $(event.target).closest('td');
                        var moveTo = null;
                        switch(key){
                            case keys.arrow.left:{
                                if (input.selectionStart === 0) {
                                    moveTo = td.prev('td:has(input,textarea)');
                                }
                                break;
                            }
                            case keys.arrow.right:{
                                if (input.selectionEnd == input.value.length) {
                                    moveTo = td.next('td:has(input,textarea)');
                                }
                                break;
                            }
                            case keys.arrow.up:
                            case keys.enter:
                            case keys.arrow.down:{
                                var tr = td.closest('tr');
                                var pos = td[0].cellIndex;
                                var moveToRow = null;
                                if (key == keys.arrow.down || key == keys.enter) {
                                    moveToRow = tr.next('tr');
                                }
                                else if (key == keys.arrow.up) {
                                    moveToRow = tr.prev('tr');
                                }
                                if (moveToRow.length) {
                                    moveTo = $(moveToRow[0].cells[pos]);
                                }
                                break;
                            }
                            // case keys.enter:{
                            //   var focusedElement = $(input);
                            //   var nextElement = focusedElement.parent().next();
                            //   if (nextElement.find('input').length>0){
                            //     nextElement.find('input').focus();
                            //   }else{
                            //     nextElement = nextElement.parent().next().find('input').first();
                            //     nextElement.focus();
                            //   }
                            //   break;
                            // }
                        }
                        if (moveTo && moveTo.length) {
                            event.preventDefault();
                            moveTo.find('input,textarea').each(function (i, input) {
                                input.focus();
                                input.select();
                            });
                        }
                    });
                }
            };
        });
        module.directive("cFilAriane", ["$location", "route", "$rootScope", "$route", function($location, routes, $rootScope, $route){
            return {
                templateUrl: "/"+appPrefix+"/public/components/ariane.html",
                restrict : "E",
                link : function($scope, element, attrs){
                    $scope.ariane = [];
                    $scope.ariane.push({stateName : appPrefix+".title", url : $route.routes.null.redirectTo });
                    $rootScope.$on("$routeChangeSuccess", function($currentRoute, $previousRoute, $location){
                        if($route.current.originalPath === $route.routes.null.redirectTo || $route.current.action === undefined){
                            $scope.ariane.splice(1, $scope.ariane.length-1);
                        }else{
                            var o = _.findWhere($scope.ariane, {url : $route.current.originalPath});
                            if(o!== undefined){
                                var i = $scope.ariane.indexOf(o);
                            }else{
                                i=-1;
                            }
                            if(i !== -1){
                                $scope.ariane.splice(i+1, $scope.ariane.length-1);
                            }else{
                                var state = {
                                    stateName : "ariane."+appPrefix+"."+$route.current.action,
                                };
                                if(Object.size($route.current.params) > 0){
                                    state.url = $route.current.originalPath.replace($route.current.regexp.exec($route.current.originalPath)[1], $route.current.params[$route.current.regexp.exec($route.current.originalPath)[1].substring(1)]);
                                }else{
                                    state.url = $route.current.originalPath;
                                }
                                $scope.ariane.push(state);
                            }
                        }
                    });

                    $scope.isLast = function(state){
                        return $scope.ariane.indexOf(state)+1 === $scope.ariane.length;
                    };

                    $scope.getI18nValue = function(i18nKey){
                        return lang.translate(i18nKey);
                    };

                    Object.size = function(obj) {
                        var size = 0, key;
                        for (key in obj) {
                            if (obj.hasOwnProperty(key)) size++;
                        }
                        return size;
                    };
                }

            };
        }]);

        module.directive('tabs', function() {
            return {
                restrict: 'E',
                transclude: true,
                scope: {},
                controller: [ "$scope", function($scope) {
                    var panes = $scope.panes = [];

                    $scope.select = function(pane) {
                        angular.forEach(panes, function(pane) {
                            pane.selected = false;
                        });
                        pane.selected = true;
                    };

                    this.addPane = function(pane) {
                        if (panes.length === 0) $scope.select(pane);
                        panes.push(pane);
                    };
                }],
                template:
                '<div class="tabbable">' +
                '<ul class="nav nav-tabs">' +
                '<li ng-repeat="pane in panes" ng-click="select(pane)" ng-class="{active:pane.selected}" class="six">'+
                '<a href=""">{{pane.title}}</a>' +
                '</li>' +
                '</ul>' +
                '<div class="tab-content" ng-transclude></div>' +
                '</div>',
                replace: true
            };
        }).
        directive('pane', function() {
            return {
                require: '^tabs',
                restrict: 'E',
                transclude: true,
                scope: { title: '@' },
                link: function(scope, element, attrs, tabsCtrl) {
                    tabsCtrl.addPane(scope);
                },
                template:
                '<div class="tab-pane" ng-class="{active: selected}" ng-transclude>' +
                '</div>',
                replace: true
            };
        });
// <span class="rounded-color-vignette rounded" ng-class="competence.color"></span>
        module.directive("cSkillsList", function(){
            return {
                restrict : 'E',
                scope : {
                    data : '=',
                    devoir : '='
                },
                templateUrl : "/"+appPrefix+"/public/components/cSkillsList.html",
                controller : ['$scope', function($scope){

                    $scope.initCheckBox = function(item, parentItem){

                        var bLastCompetence = (_.findWhere($scope.devoir.competencesLastDevoirList, {idcompetence : item.id}) !== undefined);

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

                    $scope.toggleCheckbox = function(item, parentItem){
                        if(item.competences !== undefined && item.competences.all.length > 0){
                            $scope.$emit('checkConnaissances', item);
                        }else{
                            $scope.$emit('checkParent', parentItem);
                        }
                    };

                    $scope.$on('checkConnaissances', function(event, parentItem){
                        return (parentItem.competences.each(function(e){e.selected = parentItem.selected;}));
                    });

                    $scope.$on('checkParent', function(event, parentItem){
                        return (parentItem.selected = parentItem.competences.every(function(e){ return e.selected === true; }));
                    });

                }]
            };
        })

        //.directive("cSkillsColor", function(){
        //  return {
        //    restrict : 'E',
        //    scope : {
        //      competence : '=',
        //      currentDevoir: '=',
        //      competenceIndex : '=',
        //      competencesLength : '=',
        //      student : '='
        //    },
        //    templateUrl: "/"+appPrefix+"/public/template/component/cSkillsColor.html",
        //    controller : ['$scope', function ($scope){
        //      $scope.openSelector = function(){
        //        $scope.competence.opened = !$scope.competence.opened;
        //      };
        //
        //
        //      $scope.initCompetence = function(){
        //
        //        setTimeout(function() {
        //
        //          // recherche d'un parent parmis la liste des competences du devoir
        //          var parent = _.findWhere($scope.currentDevoir.competences.all, {idcompetence: $scope.competence.idparent});
        //
        //          var competenceId = "#competence-"+$scope.competence.idcompetence+"-student-"+$scope.student.id;
        //          var competenceClass = ".competence-parent-"+$scope.competence.idcompetence+"-student-"+$scope.student.id;
        //          var children = $(competenceClass);
        //          if(children.length > 0){
        //            $scope.competence.isParent = true;
        //          }
        //
        //          $scope.competence.parent = parent;
        //          // si la competence a un parent par defaut on la cache
        //          if(parent !== undefined) {
        //            jQuery(competenceId).hide();
        //            //$scope.competence.show = false;
        //          }
        //
        //
        //          // indentation CSS
        //          jQuery(competenceId).css( "margin-left", function() {
        //            if(parent === undefined) {
        //              return 0;
        //            } else {
        //              // TODO calculer indentation en fonction de la profondeur de la competence dans l'arbre des competences
        //              // du devoir
        //              return "30px";
        //            }
        //          });
        //
        //        }, 1);
        //
        //
        //
        //        if($scope.competenceIndex === $scope.competencesLength-1){
        //          $scope.$emit('checkHeaderColor', $scope.competence);
        //        }
        //        if($scope.competence.evaluation > -1) {
        //          return $scope.competence.evaluation;
        //        } else {
        //          $scope.competence.evaluation = -1;
        //          return $scope.competence.evaluation;
        //        }
        //      };
        //      $scope.getLibelleCompetence = function (idCompetence) {
        //        if(idCompetence !== undefined) {
        //          return _.findWhere($scope.currentDevoir.competences.all, {idcompetence: idCompetence}).nom;
        //        }
        //      };
        //      $scope.selectColor = function(evaluation){
        //        $scope.competence.evaluation = evaluation;
        //        $scope.competence.opened = !$scope.competence.opened;
        //        var competenceToSave = new CompetenceNote($scope.competence);
        //        competenceToSave.save(function(id){
        //          $scope.competence.id = id;
        //        });
        //        $scope.$emit('checkHeaderColor', $scope.competence);
        //      };
        //      $scope.selectCompetencesFilles = function(idCompetence) {
        //        $scope.competence.showChildren = !$scope.competence.showChildren;
        //        var competenceClass = ".competence-parent-"+idCompetence+"-student-"+$scope.student.id;
        //        if($scope.competence.showChildren) {
        //          jQuery(competenceClass).show();
        //        } else {
        //          jQuery(competenceClass).hide();
        //        }
        //      };
        //      $scope.$on('selectColor', function($event, evaluation){
        //        $scope.competence.evaluation = evaluation;
        //        var competenceToSave = new CompetenceNote($scope.competence);
        //        competenceToSave.save(function(id){
        //          $scope.competence.id = id;
        //        });
        //      });
        //    }]
        //  };
        //})

        //.directive('cColorSkillsList', function(){
        //  return {
        //    restrict : 'E',
        //    scope : {
        //      competences : '=',
        //      currentDevoir : '=',
        //      student       : '='
        //    },
        //    templateUrl : "/"+appPrefix+"/public/template/component/cColorSkillsList.html",
        //    controller : ['$scope', function($scope){
        //      $scope.children = [];
        //      $scope.initCompetences = function(index){
        //        if(index === ($scope.competences.length-1)){
        //          $scope.$broadcast('initHeaderCompetence');
        //        }
        //      };
        //      $scope.$on('checkHeaderColor', function($event, competenceParam){
        //          $scope.$broadcast('changeHeaderColor', competenceParam);
        //      });
        //    }]
        //  };
        //})

        //.directive('cHeaderSkillsColor', function(){
        //  return {
        //    restrict : 'E',
        //    templateUrl : "/"+appPrefix+"/public/template/component/cHeaderSkillsColor.html",
        //    controller : ['$scope', function($scope){
        //      $scope.skills = {
        //        opened : false
        //      };
        //      $scope.openSelector = function(){
        //        $scope.skills.opened = !$scope.skills.opened;
        //      };
        //
        //      $scope.majHeaderColor = function() {
        //        if($scope.competences !== undefined && $scope.competences.length > 0) {
        //          // si toutes les competences ont la même note on colore evaluation de la même couleur
        //          if (_.every($scope.competences, function (competence) {
        //                return (competence.evaluation === $scope.competences[0].evaluation);
        //              })) {
        //            $scope.skills.evaluation = $scope.competences[0].evaluation;
        //          } else {
        //            // sinon on colore en gris
        //            $scope.skills.evaluation = -1;
        //          }
        //        } else {
        //          $scope.skills.evaluation = -1;
        //        }
        //      };
        //
        //      $scope.$on('initHeaderCompetence', function(){
        //        $scope.majHeaderColor();
        //      });
        //
        //      /*$scope.initCompetence = function(){
        //        $scope.majHeaderColor();
        //      };*/
        //      $scope.selectColor = function(evaluation){
        //        $scope.skills.evaluation = evaluation;
        //        if($scope.skills.opened === true){
        //          $scope.skills.opened = !$scope.skills.opened;
        //        }
        //        //$scope.$emit('headerSelected', evaluation);
        //
        //        // on colore toutes les competences de la même couleur que
        //        // l'evaluation globale
        //        _.each($scope.competences, function(competence){
        //          competence.evaluation = evaluation;
        //          var competenceToSave = new CompetenceNote(competence);
        //          competenceToSave.save(function(id){
        //            competence.id = id;
        //          });
        //        });
        //
        //      };
        //      $scope.$on('changeHeaderColor', function($event, competenceParam){
        //        $scope.majHeaderColor();
        //      });
        //
        //      //$scope.$on('allCompetences', function($event, evaluation){
        //      //  $scope.selectColor(evaluation);
        //      //});
        //    }]
        //  };
        //})
            .directive("cSkillsColorPage", function(){
                return {
                    restrict : 'E',
                    scope : {
                        devoir : '='
                    },
                    templateUrl: "/"+appPrefix+"/public/components/cSkillsColorPage.html",
                    controller : ['$scope', function($scope){
                        $scope.selectColor = function(evaluation){
                            var text = "Cette action va initialiser l'ensemble des compétences à la valeur sélectionnée.\n\n Souhaitez vous continuer ?\n";
                            if(confirm(text) === true){
                                var _datas = [];
                                for (var i = 0; i < $scope.devoir.eleves.all.length; i++) {
                                    var eval = $scope.devoir.eleves.all[i].evaluation;
                                    for (var j = 0; j < eval.competenceNotes.all.length; j++) {
                                        eval.competenceNotes.all[j].evaluation = evaluation;
                                        _datas.push(eval.competenceNotes.all[j]);
                                    }
                                }
                                $scope.devoir.saveCompetencesNotes(_datas);
                                for (var g = 0; g < $scope.devoir.competences.all.length; g++) {
                                    $scope.devoir.competences.all[g].evaluation = evaluation;
                                }
                            }
                        };
                    }]
                };
            })
            .directive("cSkillsColorColumn", function(){
                return {
                    restrict : 'E',
                    scope : {
                        devoir : '='
                    },
                    templateUrl: "/"+appPrefix+"/public/components/cSkillsColorColumn.html",
                    controller : ['$scope', function($scope){
                        $scope.compteur = 0;

                        $scope.selectCompetences = function(competenceHeader){
                            _.each($scope.eleves, function (eleve) {
                                var competencesNotesEleve = eleve.competencesNotesEleve;
                                var competenceEleve = _.findWhere(competencesNotesEleve, {idcompetence: competenceHeader.idcompetence});
                                competenceEleve.evaluation = competenceHeader.evaluation;
                            });
                            $scope.safeApply();
                        };

                        $scope.safeApply = function (fn) {
                            var phase = this.$root.$$phase;
                            if(phase === '$apply' || phase === '$digest') {
                                if(fn && (typeof(fn) === 'function')) fn();
                            } else this.$apply(fn);
                        };

                        $scope.saveCompetences = function(competenceHeader){
                            if(competenceHeader.modified) {
                                var _data = [];
                                for (var i = 0; i < $scope.devoir.eleves.all.length; i++) {
                                    var competence = $scope.devoir.eleves.all[i].evaluation.competenceNotes.findWhere({idcompetence: competenceHeader.idcompetence});
                                    if (competence !== undefined) {
                                        competence.evaluation = competenceHeader.evaluation;
                                        _data.push(competence);
                                    }
                                }
                                $scope.devoir.saveCompetencesNotes(_data);
                            }
                        };

                        $scope.init = function(competenceHeader){
                            $scope.$on('initHeaderColumn', function () {
                                competenceHeader.evaluation = -1;
                                competenceHeader.modified = false;
                                $scope.majHeaderColor(competenceHeader);
                            })
                        };

                        $scope.switchColor = function(competenceHeader){
                            if(competenceHeader.evaluation === -1){
                                competenceHeader.evaluation = 3;
                            }else{
                                competenceHeader.evaluation = competenceHeader.evaluation -1;
                            }
                            competenceHeader.modified = true;
                            $scope.selectCompetences(competenceHeader);
                        };

                        $scope.$on('changeHeaderColumn', function(event, competence){
                            var competenceHeader = $scope.devoir.competences.findWhere({idcompetence : competence.idcompetence});
                            $scope.majHeaderColor(competenceHeader);
                        });

                        $scope.majHeaderColor = function(competenceHeader) {
                            // recuperation de la competence pour chaque eleve
                            var allCompetencesElevesColumn = [];
                            _.each($scope.devoir.eleves.all, function (eleve) {
                                if (eleve.evaluation.competenceNotes !== undefined && eleve.evaluation.competenceNotes.all.length > 0) {
                                    var competenceEleve = eleve.evaluation.competenceNotes.findWhere({idcompetence: competenceHeader.idcompetence});
                                    allCompetencesElevesColumn.push(competenceEleve);
                                }
                            });


                            if(allCompetencesElevesColumn !== undefined && allCompetencesElevesColumn.length > 0) {
                                // si toutes les competences ont la même note on colore evaluation de la même couleur
                                if (_.every(allCompetencesElevesColumn, function (competence) {
                                        return (competence.evaluation === allCompetencesElevesColumn[0].evaluation);
                                    })) {
                                    competenceHeader.evaluation = allCompetencesElevesColumn[0].evaluation;
                                } else {
                                    competenceHeader.evaluation = -1;
                                }
                            }
                            $scope.safeApply();
                        };
                    }]
                };
            })
            .directive('cSkillNoteDevoir', function($compile){
                return {
                    restrict : 'E',
                    scope : {
                        competence : '=',
                        nbEleve : '=',
                        nbCompetencesDevoir : '=',
                        currentDevoir   : '='
                    },
                    template : '<span ng-click="switchColor()" ng-mouseover="detailCompetence(competence.nom)"  ng-mouseleave="saveCompetence()" ng-init="init()"  class="rounded" ng-class="{grey : competence.evaluation == -1, red : competence.evaluation == 0, orange : competence.evaluation == 1, yellow : competence.evaluation == 2, green : competence.evaluation == 3}"></span>',
                    controller : ['$scope', function($scope){
                        $scope.color = -1;
                        $scope.modified = false;
                        $scope.compteur = 0;
                        $scope.switchColor = function(){
                            if($scope.competence.evaluation === -1){
                                $scope.competence.evaluation = 3;
                            }else{
                                $scope.competence.evaluation = $scope.competence.evaluation -1;
                            }
                            $scope.$emit('majHeaderColumn', $scope.competence);
                            $scope.modified = true;
                        };

                        $scope.detailCompetence = function(competenceNom) {
                            var e = jQuery("#competence-detail");
                            e.html('<a class="resume-competence" tooltip="'+competenceNom +'">'+ competenceNom +'</a>');
                            $compile(e.contents())($scope);
                        };

                        $scope.saveCompetence = function(){
                            if($scope.modified === true){
                                // var competenceToSave = new CompetenceNote($scope.competence);
                                $scope.competence.save(function(id){
                                    $scope.competence.id = id;
                                });
                                $scope.modified = false;
                            }
                        };
                    }]
                }
            }).directive('sticky', ['$window', '$timeout', function ($window, $timeout) {
                return {
                    restrict: 'A', // this directive can only be used as an attribute.
                    // scope: {
                    //     disabled: '=disabledSticky',
                    //     devoirs : '=devoirsFiltres',
                    //     listeDevoir : '=',
                    //     currentDevoir : '='
                    // },
                    scope : true,
                    link: function linkFn($scope, $elem, $attrs) {
                        // Setting scope
                        var scrollableNodeTagName = "sticky-scroll"; // convention used in the markup for annotating scrollable container of these stickies
                        var stickyLine;
                        var stickyBottomLine = 0;
                        var placeholder;
                        var isSticking = false;
                        var originalOffset;

                        // Optional Classes
                        var stickyClass = $attrs.stickyClass || '';
                        var unstickyClass = $attrs.unstickyClass || '';
                        var bodyClass = $attrs.bodyClass || '';
                        var bottomClass = $attrs.bottomClass || '';

                        // Find scrollbar
                        var scrollbar = deriveScrollingViewport ($elem);

                        // Define elements
                        var windowElement = angular.element($window);
                        var scrollbarElement = angular.element(scrollbar);
                        var $body = angular.element(document.body);

                        // Define options
                        var usePlaceholder = ($attrs.usePlaceholder !== 'false');
                        var anchor = $attrs.anchor === 'bottom' ? 'bottom' : 'top';
                        var confine = ($attrs.confine === 'true');
                        // flag: can react to recalculating the initial CSS dimensions later as link executes prematurely. defaults to immediate checking
                        var isStickyLayoutDeferred = $attrs.isStickyLayoutDeferred !== undefined ? ($attrs.isStickyLayoutDeferred === 'true') : false;

                        // flag: is sticky content constantly observed for changes. Should be true if content uses ngBind to show text that may vary in size over time
                        var isStickyLayoutWatched = $attrs.isStickyLayoutWatched !== undefined ? ($attrs.isStickyLayoutWatched === 'true') : true;
                        var initialPosition = $elem.css('position'); // preserve this original state

                        var offset = $attrs.offset ? parseInt ($attrs.offset.replace(/px;?/, '')) : 0;
                        var onStickyContentLayoutHeightWatchUnbind;

                        // initial style
                        var initialStyle = $elem.attr('style') || '';
                        var initialCSS;
                        var originalInitialCSS;

                        /**
                         * Initialize Sticky
                         */
                        var initSticky = function() {
                            // Listeners
                            scrollbarElement.on('scroll', checkIfShouldStick);
                            windowElement.on('resize', $scope.$apply.bind($scope, onResize));

                            memorizeDimensions (); // remember sticky's layout dimensions

                            // Setup watcher on digest and change
                            $scope.$watch(onDigest, onChange);

                            // Clean up
                            $scope.$on('$destroy', onDestroy);
                        };

                        // $scope.getInfoCompetencesDevoir = function(){
                        //     $scope.$emit('getInfoCompetencesDevoir');
                        // };

                        /**
                         * need to recall sticky's DOM attributes ( make sure layout has occured)
                         */
                        function memorizeDimensions() {
                            // immediate assignment, but there is the potential for wrong values if content not ready
                            initialCSS = $scope.calculateStickyContentInitialDimensions ();

                            // option to calculate the dimensions when layout is "ready"
                            if (isStickyLayoutDeferred) {

                                // logic: when this directive link() runs before the content has had a chance to layout on browser, height could be 0
                                if (!$elem[0].getBoundingClientRect().height) {

                                    onStickyContentLayoutHeightWatchUnbind = $scope.$watch(
                                        function() {
                                            return $elem.height();
                                        },

                                        // state change: sticky content's height set
                                        function onStickyContentLayoutInitialHeightSet(newValue, oldValue) {
                                            if (newValue > 0) {
                                                // now can memorize
                                                initialCSS = $scope.calculateStickyContentInitialDimensions ();

                                                if (!isStickyLayoutWatched) {
                                                    // preference was to do just a one-time async watch on the sticky's content; now stop watching
                                                    onStickyContentLayoutHeightWatchUnbind ();
                                                }
                                            }
                                        }
                                    );
                                }

                                // any processing for when sticky layout is immediate
                            }
                        }

                        /**
                         * Determine if the element should be sticking or not.
                         */
                        var checkIfShouldStick = function() {
                            // Check media query and disabled attribute
                            if ($scope.disabled === true || mediaQueryMatches ()) {
                                if(isSticking) unStickElement ();
                                return false;
                            }

                            // What's the document client top for?
                            var scrollbarPosition = scrollbarYPos();
                            var shouldStick;

                            if (anchor === 'top') {
                                if (confine === true) {
                                    shouldStick = scrollbarPosition > stickyLine && scrollbarPosition <= stickyBottomLine;
                                } else {
                                    shouldStick = scrollbarPosition > stickyLine;
                                }
                            } else {
                                shouldStick = scrollbarPosition <= stickyLine;
                            }

                            // Switch the sticky mode if the element crosses the sticky line
                            // $attrs.stickLimit - when it's equal to true it enables the user
                            // to turn off the sticky function when the elem height is
                            // bigger then the viewport
                            var closestLine = getClosest (scrollbarPosition, stickyLine, stickyBottomLine);



                            if (shouldStick && !shouldStickWithLimit ($attrs.stickLimit) && !isSticking) {
                                stickElement (closestLine);
                            } else if (!shouldStick && isSticking) {
                                unStickElement(closestLine, scrollbarPosition);
                            } else if (confine && !shouldStick) {
                                // If we are confined to the parent, refresh, and past the stickyBottomLine
                                // We should "remember" the original offset and unstick the element which places it at the stickyBottomLine
                                originalOffset = elementsOffsetFromTop ($elem[0]);

                                unStickElement (closestLine, scrollbarPosition);
                            }
                        };

                        /**
                         * determine the respective node that handles scrolling, defaulting to browser window
                         */
                        function deriveScrollingViewport(stickyNode) {
                            // derive relevant scrolling by ascending the DOM tree
                            var match =findAncestorTag (scrollableNodeTagName, stickyNode);
                            return (match.length === 1) ? match[0] : $window;
                        }

                        /**
                         * since jqLite lacks closest(), this is a pseudo emulator ( by tag name )
                         */
                        function findAncestorTag(tag, context) {
                            var m = [], // nodelist container
                                n = context.parent(), // starting point
                                p;

                            do {
                                var node = n[0]; // break out of jqLite
                                // limit DOM territory
                                if (node.nodeType !== 1) {
                                    break;
                                }

                                // success
                                if (node.tagName.toUpperCase() === tag.toUpperCase()) {
                                    return n;
                                }

                                p = n.parent();
                                n = p; // set to parent
                            } while (p.length !== 0);

                            return m; // empty set
                        }

                        /**
                         * Seems to be undocumented functionality
                         */
                        function shouldStickWithLimit(shouldApplyWithLimit) {
                            if (shouldApplyWithLimit === 'true') {
                                return ($window.innerHeight - ($elem[0].offsetHeight + parseInt (offset)) < 0);
                            } else {
                                return false;
                            }
                        }

                        /**
                         * Finds the closest value from a set of numbers in an array.
                         */
                        function getClosest(scrollTop, stickyLine, stickyBottomLine) {
                            var closest = 'top';
                            var topDistance = Math.abs(scrollTop - stickyLine);
                            var bottomDistance = Math.abs(scrollTop - stickyBottomLine);

                            if (topDistance > bottomDistance) {
                                closest = 'bottom';
                            }

                            return closest;
                        }

                        /**
                         * Unsticks the element
                         */
                        function unStickElement(fromDirection) {
                            $elem.attr('style', initialStyle);
                            isSticking = false;

                            $body.removeClass(bodyClass);
                            $elem.removeClass(stickyClass);
                            $elem.addClass(unstickyClass);

                            if (fromDirection === 'top') {
                                $elem.removeClass(bottomClass);

                                $elem
                                    .css('z-index', 10)
                                    .css('width', $elem[0].offsetWidth)
                                    .css('top', initialCSS.top)
                                    .css('position', initialCSS.position)
                                    //.css('left', initialCSS.cssLeft)
                                    .css('margin-top', initialCSS.marginTop)
                                    .css('height', initialCSS.height);
                            } else if (fromDirection === 'bottom' && confine === true) {
                                $elem.addClass(bottomClass);

                                // It's possible to page down page and skip the "stickElement".
                                // In that case we should create a placeholder so the offsets don't get off.
                                createPlaceholder();

                                $elem
                                    .css('z-index', 10)
                                    .css('width', $elem[0].offsetWidth)
                                    .css('top', '')
                                    .css('bottom', 0)
                                    .css('position', 'absolute')
                                    //.css('left', initialCSS.cssLeft)
                                    .css('margin-top', initialCSS.marginTop)
                                    .css('margin-bottom', initialCSS.marginBottom)
                                    .css('height', initialCSS.height);
                            }

                            if (placeholder && fromDirection === anchor) {
                                placeholder.remove();
                            }
                        }

                        /**
                         * Sticks the element
                         */
                        function stickElement(closestLine) {
                            // Set sticky state
                            isSticking = true;
                            $timeout( function() {
                                initialCSS.offsetWidth = $elem[0].offsetWidth;
                            }, 0);
                            $body.addClass(bodyClass);
                            $elem.removeClass(unstickyClass);
                            $elem.removeClass(bottomClass);
                            $elem.addClass(stickyClass);

                            createPlaceholder();

                            $elem
                                .css('z-index', '10')
                                .css('width', $elem[0].offsetWidth + 'px')
                                .css('position', 'fixed')
                                .css('left', $elem.css('left').replace('px', '') + 'px')
                                .css(anchor, (offset + elementsOffsetFromTop (scrollbar)) + 'px')
                                .css('margin-top', 0);

                            if (anchor === 'bottom') {
                                $elem.css('margin-bottom', 0);
                            }
                        }

                        /**
                         * Clean up directive
                         */
                        var onDestroy = function() {
                            scrollbarElement.off('scroll', checkIfShouldStick);
                            windowElement.off('resize', onResize);


                            $body.removeClass(bodyClass);

                            if (placeholder) {
                                placeholder.remove();
                            }
                        };

                        /**
                         * Updates on resize.
                         */
                        var onResize = function() {
                            unStickElement (anchor);
                            checkIfShouldStick ();
                        };

                        /**
                         * Triggered on load / digest cycle
                         */
                        var onDigest = function() {
                            if ($scope.disabled === true) {
                                return unStickElement ();
                            }

                            if (anchor === 'top') {
                                return (originalOffset || elementsOffsetFromTop ($elem[0])) - elementsOffsetFromTop (scrollbar) + scrollbarYPos ();
                            } else {
                                return elementsOffsetFromTop ($elem[0]) - scrollbarHeight () + $elem[0].offsetHeight + scrollbarYPos ();
                            }
                        };

                        /**
                         * Triggered on change
                         */
                        var onChange = function (newVal, oldVal) {
                            if (( newVal !== oldVal || typeof stickyLine === 'undefined' ) &&
                                (!isSticking && !isBottomedOut()) && newVal !== 0) {
                                stickyLine = newVal - offset;

                                // IF the sticky is confined, we want to make sure the parent is relatively positioned,
                                // otherwise it won't bottom out properly
                                if (confine) {
                                    $elem.parent().css({
                                        'position': 'relative'
                                    });
                                }

                                // Get Parent height, so we know when to bottom out for confined stickies
                                var parent = $elem.parent()[0];
                                // Offset parent height by the elements height, if we're not using a placeholder
                                var parentHeight = parseInt (parent.offsetHeight) - (usePlaceholder ? 0 : $elem[0].offsetHeight);

                                // and now lets ensure we adhere to the bottom margins
                                // TODO: make this an attribute? Maybe like ignore-margin?
                                var marginBottom = parseInt ($elem.css('margin-bottom').replace(/px;?/, '')) || 0;

                                // specify the bottom out line for the sticky to unstick
                                var elementsDistanceFromTop = elementsOffsetFromTop ($elem[0]);
                                var parentsDistanceFromTop = elementsOffsetFromTop (parent)
                                var scrollbarDistanceFromTop = elementsOffsetFromTop (scrollbar);

                                var elementsDistanceFromScrollbarStart = elementsDistanceFromTop - scrollbarDistanceFromTop;
                                var elementsDistanceFromBottom = parentsDistanceFromTop + parentHeight - elementsDistanceFromTop;

                                stickyBottomLine = elementsDistanceFromScrollbarStart + elementsDistanceFromBottom - $elem[0].offsetHeight - marginBottom - offset + +scrollbarYPos ();

                                checkIfShouldStick ();
                            }
                        };

                        /**
                         * Helper Functions
                         */

                        /**
                         * Create a placeholder
                         */
                        function createPlaceholder() {
                            if (usePlaceholder) {
                                // Remove the previous placeholder
                                if (placeholder) {
                                    placeholder.remove();
                                }

                                placeholder = angular.element('<div>');
                                placeholder.css('height', $elem[0].offsetHeight + 'px');

                                $elem.after(placeholder);
                            }
                        }

                        /**
                         * Are we bottomed out of the parent element?
                         */
                        function isBottomedOut() {
                            if (confine && scrollbarYPos() > stickyBottomLine) {
                                return true;
                            }

                            return false;
                        }

                        /**
                         * Fetch top offset of element
                         */
                        function elementsOffsetFromTop(element) {
                            var offset = 0;

                            if (element.getBoundingClientRect) {
                                offset = element.getBoundingClientRect().top;
                            }

                            return offset;
                        }

                        /**
                         * Retrieves top scroll distance
                         */
                        function scrollbarYPos() {
                            var position;

                            if (typeof scrollbar.scrollTop !== 'undefined') {
                                position = scrollbar.scrollTop;
                            } else if (typeof scrollbar.pageYOffset !== 'undefined') {
                                position = scrollbar.pageYOffset;
                            } else {
                                position = document.documentElement.scrollTop;
                            }

                            return position;
                        }

                        /**
                         * Determine scrollbar's height
                         */
                        function scrollbarHeight() {
                            var height;

                            if (scrollbarElement[0] instanceof HTMLElement) {
                                // isn't bounding client rect cleaner than insane regex mess?
                                height = $window.getComputedStyle(scrollbarElement[0], null)
                                    .getPropertyValue('height')
                                    .replace(/px;?/, '');
                            } else {
                                height = $window.innerHeight;
                            }

                            return parseInt (height) || 0;
                        }

                        /**
                         * Checks if the media matches
                         */
                        function mediaQueryMatches() {
                            var mediaQuery = $attrs.mediaQuery || false;
                            var matchMedia = $window.matchMedia;

                            return mediaQuery && !(matchMedia ('(' + mediaQuery + ')').matches || matchMedia (mediaQuery).matches);
                        }

                        // public accessors for the controller to hitch into. Helps with external API access
                        $scope.selectedDevoirs = [];
                        $scope.getElement = function() { return $elem; };
                        $scope.getScrollbar = function() { return scrollbar; };
                        $scope.getInitialCSS = function() { return initialCSS; };
                        $scope.getAnchor = function() { return anchor; };
                        $scope.isSticking = function() { return isSticking; };
                        $scope.getOriginalInitialCSS = function() { return originalInitialCSS; };
                        // pass through aliases
                        $scope.processUnStickElement = function(anchor){ unStickElement(anchor)};
                        $scope.processCheckIfShouldStick =function() { checkIfShouldStick(); };
                        $scope.selectAllDevoir = function(){
                            $scope.$emit('getSelectedAllDevoir');
                        };
                        $scope.getDevoirInfo = function(id){
                            $scope.$emit('getDevoirInfo', id);
                        };
                        $scope.goTo = function(path){
                            $scope.$emit('getGotTo', path);
                        };
                        /**
                         * set the dimensions for the defaults of the content block occupied by the sticky element
                         */
                        $scope.calculateStickyContentInitialDimensions = function() {
                            return {
                                zIndex: $elem.css('z-index'),
                                top: $elem.css('top'),
                                position: initialPosition, // revert to true initial state
                                marginTop: $elem.css('margin-top'),
                                marginBottom: $elem.css('margin-bottom'),
                                //cssLeft: $elem.css('left'),
                                height: $elem.css('height')
                            };
                        };

                        /**
                         * only change content box dimensions
                         */
                        $scope.updateStickyContentUpdateDimensions = function(width, height) {
                            if (width && height) {
                                initialCSS.width = width + "px";
                                initialCSS.height = height + "px";
                                // if a dimensionless pair of arguments was supplied.
                            }
                        };

                        // ----------- configuration -----------

                        $timeout( function() {
                            originalInitialCSS = $scope.calculateStickyContentInitialDimensions(); // preserve a copy
                            // Init the directive
                            initSticky();
                        },0);
                    },

                    /**
                     * +++++++++ public APIs+++++++++++++
                     */
                    controller: ['$scope', '$window', function($scope, $window) {

                        /**
                         * integration method allows for an outside client to reset the pinned state back to unpinned.
                         * Useful for when refreshing the scrollable DIV content completely
                         * if newWidth and newHeight integer values are not supplied then function will make a best guess
                         */
                        this.resetLayout = function(newWidth, newHeight) {

                            var scrollbar = $scope.getScrollbar(),
                                initialCSS = $scope.getInitialCSS(),
                                anchor = $scope.getAnchor();

                            function _resetScrollPosition() {

                                // reset means content is scrolled to anchor position
                                if (anchor === "top") {
                                    // window based scroller
                                    if (scrollbar === $window) {
                                        $window.scrollTo(0, 0);
                                        // DIV based sticky scroller
                                    } else {
                                        if (scrollbar.scrollTop > 0) {
                                            scrollbar.scrollTop = 0;
                                        }
                                    }
                                }
                                // todo: need bottom use case
                            }

                            // only if pinned, force unpinning, otherwise height is inadvertently reset to 0
                            if ($scope.isSticking()) {
                                $scope.processUnStickElement (anchor);
                                $scope.processCheckIfShouldStick ();
                            }
                            // remove layout-affecting attribures that were modified by this sticky
                            $scope.getElement().css({"width": "", "height": "", "position": "", "top": "", zIndex: ""});
                            // model resets
                            initialCSS.position = $scope.getOriginalInitialCSS().position; // revert to original state
                            delete initialCSS.offsetWidth; // stickElement affected

                            // use this directive element's as default, if no measurements passed in
                            if (newWidth === undefined && newHeight === undefined) {
                                var e_bcr = $scope.getElement()[0].getBoundingClientRect();
                                newWidth = e_bcr.width;
                                newHeight = e_bcr.height;
                            }

                            // update model with new dimensions ( if supplied from client's own measurement )
                            $scope.updateStickyContentUpdateDimensions(newWidth, newHeight); // update layout dimensions only

                            _resetScrollPosition ();
                        };

                        /**
                         * return a reference to the scrolling element ( window or DIV with overflow )
                         */
                        this.getScrollbar = function() {
                            return $scope.getScrollbar();
                        };
                    }]
                };
            }]
        )
    }};