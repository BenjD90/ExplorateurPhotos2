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

    PhotosService.getListPhotos().then(function (response) {
      $scope.listPhotos = response.data;
    });
  });
