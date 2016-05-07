'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:PhotomodalCtrl
 * @description
 * # PhotomodalCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('MainPhotoCtrl', function ($scope, $q, photoLight, Config, photoModalService) {
    $scope.photo = photoLight;
    $scope.urlThumbnail = Config.urlServices + "/thumbnail";


    $('#photoModal').attr('src', '');
    var photoDisplayArea = $('#photoDisplayArea');
    var maxWidth = photoDisplayArea.width() - $('.arrowRight').width() - $('.arrowLeft').width();
    var maxHeight = window.innerHeight - 10;

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


    $scope.$parent.$watch('listPhotosToDisplay', function (newValue, oldValue) {
      if (!newValue) {
        return;
      }
      getNextPhoto().then(function (p) {
        $scope.nextPhoto = p;
      });

      getPreviousPhoto().then(function (p) {
        $scope.previousPhoto = p;
      });
    });

    function getNextPhoto() {
      var deferred = $q.defer();
      $scope.$parent.listPhotosToDisplay.forEach(function (line, lineIndex) {
        line.forEach(function (photo, index) {
          if (photo.size === photoLight.size && photo.path === photoLight.path) {
            if (index < line.length - 1) {
              deferred.resolve(line[index + 1]);
            } else if (lineIndex < $scope.listPhotosToDisplay.length - 1) {
              deferred.resolve($scope.listPhotosToDisplay[lineIndex + 1][0]);
            } else {
              deferred.resolve($scope.listPhotosToDisplay[0][0]);
            }
          }
        });
      });
      return deferred.promise;
    }

    function getPreviousPhoto() {
      var deferred = $q.defer();
      $scope.$parent.listPhotosToDisplay.forEach(function (line, lineIndex) {
        line.forEach(function (photo, index) {
          if (photo.size === photoLight.size && photo.path === photoLight.path) {
            if (index > 0) {
              deferred.resolve(line[index - 1]);
            } else if (lineIndex > 0) {
              deferred.resolve($scope.listPhotosToDisplay[lineIndex - 1][$scope.listPhotosToDisplay[lineIndex - 1].length - 1]);
            } else {
              deferred.resolve($scope.listPhotosToDisplay[$scope.listPhotosToDisplay.length - 1][$scope.listPhotosToDisplay[$scope.listPhotosToDisplay.length - 1].length - 1]);
            }
          }
        });
      });
      return deferred.promise;
    }


  });
