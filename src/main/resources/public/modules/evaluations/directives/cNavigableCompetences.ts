/**
 * Created by ledunoiss on 26/10/2016.
 */

/**
 * Created by ledunoiss on 21/09/2016.
 */
import { ng } from 'entcore/entcore';

export let $ = require('jquery');

export let navigableCompetences = ng.directive('cNavigableCompetences', function(){
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
                return $(el).find('.'+cls);
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

            scope.isCompetenceCell = function (cell) {
                return scope.hasClass(cell, 'competences-cell');
            };

            scope.isCompetence = function (input) {
                return scope.hasClass(input, 'competence-eval');
            };

            scope.findNavigableRow = function(row){
                return (scope.findChildren(row, 'navigable-inputs-row'))[0];
            };

            scope.findCompetence = function (cell) {
                return $(cell).find('.competence-eval')[0];
            };

            element.bind('keyup', function(event){
                var keys = {
                    enter : 13,
                    arrow : {left: 37, up: 38, right: 39, down: 40},
                    numbers : {zero : 96, one : 97, two : 98, three : 99, four : 100},
                    shiftNumbers : {zero : 48, one : 49, two : 50, three : 51, four : 52}
                };
                var key = event.which | event.keyCode;

                if ($.inArray(key, [keys.arrow.left, keys.arrow.up, keys.arrow.right, keys.arrow.down, keys.enter,
                        keys.numbers.zero, keys.numbers.one, keys.numbers.two, keys.numbers.three, keys.numbers.four,
                        keys.shiftNumbers.zero, keys.shiftNumbers.one, keys.shiftNumbers.two, keys.shiftNumbers.three, keys.shiftNumbers.four]) < 0) { return; }
                var input = event.target;
                var td = scope.findAncestor(event.target, 'navigable-cell');
                var row = scope.findAncestor(td, 'navigable-inputs-row');
                var children = scope.findChildren(row, 'navigable-cell');
                var index = scope.findIndex(td, children);
                var moveTo = null;
                switch(key){
                    case keys.arrow.left:{
                        if (!scope.isCompetence(input)) {
                            if (input.selectionStart === 0) {
                                if (index > 0) {
                                    moveTo = children[index - 1];
                                }
                            }
                        } else {
                            if (index > 0) {
                                moveTo = children[index - 1];
                            }
                        }
                    }
                        break;
                    case keys.numbers.zero:
                    case keys.numbers.one:
                    case keys.numbers.two:
                    case keys.numbers.three:
                    case keys.numbers.four:
                    case keys.shiftNumbers.zero:
                    case keys.shiftNumbers.one:
                    case keys.shiftNumbers.two:
                    case keys.shiftNumbers.three:
                    case keys.shiftNumbers.four: {
                        if (scope.isCompetence(input)) {
                               if(index < children.length){
                               moveTo = children[index+1];
                            }
                        }
                    }
                        break;
                    case keys.arrow.right:{
                        if((index +1) < children.length){
                            moveTo = children[index+1];
                        } else {
                            var tr = scope.findAncestor(td, 'navigable-row');
                            var pos = scope.findIndex(td, children);
                            var expandChildren = scope.findChildren(scope.findAncestor(tr, 'expandable-liste'), 'navigable-row');
                            var trIndex = scope.findIndex(tr, expandChildren);
                            var moveToRow = expandChildren[trIndex+1];
                            if (moveToRow !== null) {
                                var navigableCells = scope.findChildren(scope.findNavigableRow(moveToRow), 'navigable-cell');
                                moveTo = navigableCells[0];
                            }
                        }
                    }
                        break;
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
                    }
                        break;
                }
                if (moveTo) {
                    event.preventDefault();
                    if (scope.isCompetenceCell(moveTo)) {
                        input = scope.findCompetence(moveTo);
                    } else {
                        input = scope.findInput(moveTo);
                    }
                    input.focus();
                    if (!scope.isCompetenceCell(moveTo)) {
                        input.select();
                    }
                }

            });
        }
    };
});