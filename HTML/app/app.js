'use strict';

/**
 * @ngdoc overview
 * @name htmlApp
 * @description
 * # htmlApp
 *
 * Main module of the application.
 */
angular
  .module('htmlApp', [
    'htmlApp-config',
    'ngAnimate',
    'ngCookies',
    'ngMessages',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'vs-repeat',
    'ui.bootstrap',
    'ui.router',
    'snap',
    'angular-loading-bar'
  ])
  .config(function ($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/');



    //$routeProvider
    //  .when('/', {
    //    templateUrl: 'views/main.html',
    //    controller: 'MainCtrl',
    //    controllerAs: 'main'
    //  })
    //  .when('/state', {
    //    templateUrl: 'views/state.html',
    //    controller: 'StateCtrl',
    //    controllerAs: 'state'
    //  })
    //  .otherwise({
    //    redirectTo: '/'
    //  });
  })
  .config(function (snapRemoteProvider) {
    snapRemoteProvider.globalOptions.touchToDrag = false;
  });
