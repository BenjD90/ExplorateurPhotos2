'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('MainCtrl', function (PhotosService, $scope, Config, $timeout) {
    $scope.urlThumbnail = Config.urlServices + "/thumbnail";

    $scope.isFilterPanelOpen = true;

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
    );

    var resizeListPhotos = function () {
      $scope.listPhotosDivHeight = $scope.windowHeight - 2 - $('.header').outerHeight(true) - $('.mainPage .filter').outerHeight(true);
    };

    $scope.$watch('windowHeight', resizeListPhotos);
    $scope.$watch('isFilterPanelOpen', function () {
      $timeout(resizeListPhotos, 200);
    });

  });
