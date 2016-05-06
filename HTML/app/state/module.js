angular.module('htmlApp')
  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider.state('state',
      {
        url: '/state',
        templateUrl: 'state/view.html',
        controller: 'StateCtrl as state'
      });
  });
