/**
 Defining internal routes
 **/
routes.define(function($routeProvider){
    $routeProvider
        .when('/appels/list',{action:'listAppels'})
        .otherwise({
            redirectTo : '/releve'
        });
});




/**
 Wrapper controller
 ------------------
 Main controller.
 **/
function ViescolaireController($scope, $rootScope, model, template, route, date){

    route({
        listAppels : function(params){

        }
    });
}
