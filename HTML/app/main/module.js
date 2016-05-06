angular.module('htmlApp')
  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('main',
      {
        url: '/',
        templateUrl: 'main/view.html',
        controller: 'MainCtrl as main'
      });
  });
