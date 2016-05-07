'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('MainCtrl', function (PhotosService, $scope, $q, Config, $state) {
    function compareWithField(a, b, sortField) {
      return compareTwoFields(a[sortField], b[sortField], a, b);
    }

    function compareWithResolution(a, b) {
      var aResolution = a.height * a.width;
      var bResolution = b.height * b.width;
      return compareTwoFields(aResolution, bResolution, a, b);
    }

    function compareTwoFields(fieldAValue, fieldBValue, a, b) {
      if (fieldAValue < fieldBValue) {
        return $scope.sortDesc ? 1 : -1;
      } else if (fieldAValue > fieldBValue) {
        return $scope.sortDesc ? -1 : 1;
      } else {
        if (a.path < b.path) {
          return $scope.sortDesc ? 1 : -1;
        } else {
          return $scope.sortDesc ? -1 : 1;
        }
      }
    }

    function getListPhotosToDisplay(listPhotos) {
      var photoMargin = 4;
      return PhotosService.getListPhotosToDisplay(listPhotos, window.innerWidth - 17, photoMargin);
    }

    $scope.urlThumbnail = Config.urlServices + "/thumbnail";
    $scope.$state = $state;
    $scope.showFilter = false;
    $scope.filter = {};
    $scope.sortField = 'dateLastModified';
    $scope.sortDesc = true;

    $scope.Config = Config;

    $scope.time = PhotosService.time;
    $scope.loading = false;

    $scope.reset = function () {
      $scope.filter = {};
    };

    $scope.$watch('windowHeight', function () {
      $scope.listPhotosDivHeight = $scope.windowHeight - 2 - $('.header').outerHeight(true) - $('.subMenu').outerHeight(true);
    });


    $scope.$watchCollection('filter', function (newValue) {
      $scope.loading = 'Téléchargement de la liste des photos.';
      PhotosService.getListPhotos()
        .then(function (array) {
          $scope.loading = 'Application des filtres.';
          $scope.nbreTotalPhotos = array.length;
          return PhotosService.filterPhotos(array, newValue);
        }).then(function (photosFiltered) {
          $scope.loading = 'Répartition des photos par ligne.';
          $scope.nbrPhotosFiltered = photosFiltered.length;
          return getListPhotosToDisplay(photosFiltered);
        }).then(function (array) {
          $scope.listPhotosToDisplay = array;
          $scope.loading = false;
          document.querySelector('.photosList').scrollTop = 0;
        });
    });

    $scope.sort = function (sortField) {
      //if user doesn't change field
      if ($scope.sortField === sortField) {
        $scope.sortDesc = !$scope.sortDesc;
      }
      $scope.sortField = sortField;
      PhotosService.getListPhotos()
        .then(function (allPhotos) {
          return PhotosService.filterPhotos(allPhotos, $scope.filter);
        })
        .then(function (filteredPhotos) {
          var deferred = $q.defer();
          if (sortField !== 'resolution') {
            deferred.resolve(filteredPhotos.sort(function (a, b) {
              return compareWithField(a, b, sortField);
            }));
          } else {
            deferred.resolve(filteredPhotos.sort(function (a, b) {
              return compareWithResolution(a, b, sortField);
            }));
          }
          return deferred.promise;
        }).then(function (photosSorted) {
          return getListPhotosToDisplay(photosSorted);
        }).then(function (array) {
          $scope.listPhotosToDisplay = array;
          document.querySelector('.photosList').scrollTop = 0;
        });
    };

  });
