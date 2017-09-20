/**
 * Created by ledunoiss on 21/09/2016.
 */
import { ng } from 'entcore/entcore';

export let $ = require('jquery');

export let navigatable = ng.directive('cNavigaTable', function(){
    return {
        restrict : 'A',
        link: function(scope, element, attrs) {

            /**
             * Détermine si l'élément peut être sélectionner
             * @param moveToRow
             * @param pos
             * @param up
             * @returns {any}
             */
            scope.canMove = function (moveToRow, pos, up) {

                while (moveToRow !== undefined && moveToRow.length){
                    let moveTo = $(moveToRow[0].cells[pos]);
                    let inputs =  moveTo.find('input,textarea');
                    if (inputs.length > 0) {
                        let i = 0 ;
                        while (i < inputs.length){
                            if(!inputs[i].disabled) {
                                //hasFundInputDisabled = true;
                                return moveTo;
                            }
                            i++;
                        }
                    }
                    if (up) {
                        moveToRow = moveToRow.prev('tr');
                    }else{
                        moveToRow = moveToRow.next('tr');
                    }
                }
            }

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
                            moveTo = td.prevAll('td.nav-input')[0]
                        }
                        break;
                    }
                    case keys.arrow.right:{
                        if (input.selectionEnd == input.value.length) {
                            moveTo = td.nextAll('td.nav-input')[0];
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
                            moveTo = scope.canMove(moveToRow , pos , key == keys.arrow.up);
                        }
                        break;
                    }
                }
                if (moveTo) {
                    if(moveTo.length) {
                        event.preventDefault();
                        moveTo.find('input,textarea').each(function (i, input) {
                            input.focus();
                            input.select();
                        });
                    } else {
                        var input = moveTo.getElementsByClassName("input-note")[0];
                        if(input) {
                            input.focus();
                            input.select()
                        }

                    }


                }
            });
        }
    };
});