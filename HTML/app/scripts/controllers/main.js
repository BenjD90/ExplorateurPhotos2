'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('MainCtrl', function (PhotosService, $scope, Config) {
    $scope.urlThumbnail = Config.urlServices + "/thumbnail";

    $scope.Config = Config;

    $scope.a = 5;
    $scope.time = PhotosService.time;
    PhotosService.getListPhotos().then(function (data) {
      $scope.listPhotos = data;
    });

    var photoMargin = 4;
    PhotosService.getListPhotosToDisplay(window.innerWidth - 17, photoMargin).then(function (array) {
        $scope.listPhotosToDisplay = array;
      }
    )
    ;
  });
