'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:AboutCtrl
 * @description
 * # StateCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('StateCtrl', function ($http, $interval, Config, $scope) {
    function refreshState() {
      $http.get(Config.urlServices + '/state').then(function (response) {
        $scope.state = response.data;
      });
    }

    var intervalPromise = $interval(refreshState, 1000);
    $scope.$on('$destroy', function () {
      if (intervalPromise) {
        $interval.cancel(intervalPromise);
      }
    });


    $scope.launchScan = function () {
      $http.get(Config.urlServices + '/launchScan').catch(function () {
        $scope.errorLaunchScan = "Erreur lors de la demande de lancement du scan.";
      });
    };

    $scope.photosInErrors = [];
    $scope.launchErrors = function () {
      $http.get(Config.urlServices + '/errors').then(function (response) {
        $scope.photosInErrors = response.data;
      });
    };

    $scope.launchErrors();
  });
