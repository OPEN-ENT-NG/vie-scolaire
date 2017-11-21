import { ng , appPrefix, template } from 'entcore/entcore';

export let inputTextList = ng.directive('inputTextList', function() {
    return {
        restrict : 'E',
        scope : {
            items : '=',                        // Liste de choix
            model : '=',                        // Valeur sélectionnée ou saisie dans l'input : model[modelAttribute]
            modelAttribute : '@',               // Champ sauvegardée dans le model : model[modelAttribute]
            displayAttributeList : '@',         // Champs utilisé pour le libellé de la liste : item[displayAttributeList]
            displayAttributeInput : '@',        // Champs utilisé pour la valeur à positionner dans l'input : item[$scope.displayAttributeInput]
            disabledInput : '=',                // Désactive/Active le champs texte
            validationItemFunction : '&',       // Valide et Sauvegarde l'item
            autofocus : '=',                    // Autofocus de l'input
            error : '=',                        // Erreur remontée lors de la validation : validationItemFunction
            template : '@?',                    // Template utilisé lors du focus de l'input : template.open($scope.template, $scope.templatePath)
            templatePath : '@?',                // Template utilisé lors du focus de l'input : template.open($scope.template, $scope.templatePath)
            focusItems : '=?',                  // Eléments utilisés lors du focus de l'input
            focusItemsAttribute : '@?',         // Attribut d'élément utilisé lors du focus de l'input : $scope.focusItems[$scope.focusItemsAttribute]
            focusItem : '=?',                   // Elément utilisé lors du focus de l'input
            getEleveInfo: '='                   // Remplissage de la fiche élève
        },
        templateUrl: "/" + appPrefix + "/public/components/inputTextList.html",
        link :  ($scope, element) => {
            $scope.openedList = false;
            $scope.itemsToDisplay = $scope.items;
            /**
             * Ouvre ou ferme la liste de choix
             * @param item
             */
            $scope.changeOpenedList = function(item) {
                $scope.getEleveInfo($scope.focusItem);
                $scope.openedList = !$scope.openedList;
                if ($scope.openedList) {
                    $scope.itemsToDisplay = $scope.items;
                }

                let inputElement = element.find('input[type=text]');
                if (inputElement !== undefined && inputElement.length > 0) {
                    inputElement.get(0).focus();
                }
            };

            /**
             * Ajoute dans l'input l'élément sélectionné :  item[$scope.displayAttributeInput]
             * @param item
             */
            $scope.addItem = function(item) {
                $scope.openedList = !$scope.openedList;
                $scope.model[$scope.modelAttribute] = item[$scope.displayAttributeInput];
                let inputElement = element.find('input[type=text]');
                if (inputElement !== undefined && inputElement.length > 0) {
                    $scope.validationItemFunction();
                    if (!inputElement.get(0).disabled) {
                        inputElement.get(0).focus();
                    }
                }
                // On est dans le cas d'une suppression
                if($scope.model[$scope.modelAttribute] === ""){
                    $scope.validationItemFunction();
                }
            };

            /**
             * Autocomplete de la $scope.itemsToDisplay.
             * Si les critères de la liste ne sont pas vérifiées on retourne la liste complète
             */
            $scope.searchItems = function() {
                let regExp = /^\D*$/;
                let value = $scope.model[$scope.modelAttribute];
                if (value !== undefined && value !== "" && regExp.test(value)) {
                    $scope.openedList = true;
                    let tempItems = _.filter($scope.itemsToDisplay, function(item) {
                        if ($scope.containsIgnoreCase(item[$scope.displayAttributeList], value)) {return item ; }
                    });
                    if (tempItems !== undefined && tempItems.length > 0) {
                        $scope.itemsToDisplay = tempItems;
                    } else {
                        $scope.itemsToDisplay = $scope.items;
                    }
                } else {
                    $scope.openedList = false;
                }
            };

            /**
             * Vérifie que psString contient le mot clé psKeyword
             * @param psString
             * @param psKeyword
             * @returns {boolean}
             */
            $scope.containsIgnoreCase = function (psString, psKeyword) {
                if (psKeyword !== undefined) {
                    return psString.toLowerCase().indexOf(psKeyword.toLowerCase()) >= 0 && psKeyword.trim() !== "";
                }
                else {
                    return false;
                }
            };

            /**
            * Lance le focus sur la cible de l'évènement
            * @param $event évènement
            */
            $scope.focusMe = function ($event) {
                $event.target.select();
                if ($scope.template !== undefined && $scope.templatePath !== undefined && $scope.focusItems !== undefined && $scope.focusItem !== undefined && $scope.focusItemsAttribute !== undefined) {
                    if (template.isEmpty($scope.template)) template.open($scope.template, $scope.templatePath);
                    $scope.focusItems[$scope.focusItemsAttribute] = $scope.focusItem;
                }
            };
        },
    };
});


