'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:PhotomodalCtrl
 * @description
 * # PhotomodalCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('PhotoModalCtrl', function ($scope, photoLight, Config, photoModalService) {
    $scope.photo = photoLight;
    $scope.urlThumbnail = Config.urlServices + "/thumbnail";

    $scope.updateWidthAndHeight = function () {
      $('#photoModal').attr('src', '');
      var photoDisplayArea = $('#photoDisplayArea');
      var maxWidth = photoDisplayArea.width() - $('.arrowRight').width() - $('.arrowLeft').width();
      var maxHeight = window.innerHeight - ($('.modal-dialog').outerHeight(true) - photoDisplayArea.height());

      var ratio = $scope.photo.height / $scope.photo.width;

      if (maxHeight < $scope.photo.height || maxWidth < $scope.photo.width) {
        if (ratio * maxWidth > maxHeight) {
          $scope.height = maxHeight;
          $scope.width = Math.ceil(maxHeight / ratio);
        }
        else {
          $scope.height = Math.ceil(ratio * maxWidth);
          $scope.width = maxWidth;
        }
      } else {
        $scope.height = null;
        $scope.width = null;
      }
    };

    photoModalService.getModalInstance().rendered.then(function () {
      $scope.updateWidthAndHeight();
    });
  });
