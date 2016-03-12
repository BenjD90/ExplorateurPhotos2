'use strict';

/**
 * @ngdoc function
 * @name htmlApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the htmlApp
 */
angular.module('htmlApp')
  .controller('MainCtrl', function (PhotosService, $scope, Config, $uibModal, photoModalService) {
    $scope.urlThumbnail = Config.urlServices + "/thumbnail";

    $scope.showFilter = false;
    $scope.filter = {};
    $scope.sortField = 'dateLastModified';
    $scope.sortDesc = true;

    $scope.Config = Config;

    $scope.a = 5;
    $scope.time = PhotosService.time;

    $scope.reset = function () {
      $scope.filter = {};
    };

    $scope.open = function (photoLight) {
      photoModalService.setModalInstance($uibModal.open({
        templateUrl: 'views/modals/photo.html',
        controller: 'PhotoModalCtrl',
        size: 'lg',
        resolve: {
          photoLight: photoLight
        }
      }));
    }

    getListPhotosToDisplay(PhotosService.getListPhotos()).then(function (array) {
      $scope.listPhotosToDisplay = array;
    });

    $scope.$watch('windowHeight', function () {
      $scope.listPhotosDivHeight = $scope.windowHeight - 2 - $('.header').outerHeight(true) - $('.subMenu').outerHeight(true);
    });


    $scope.$watchCollection('filter', function (newValue) {
      getListPhotosToDisplay(PhotosService.getListPhotos().then(function (array) {
        return filterPhotos(array, newValue);
      })).then(function (array) {
        $scope.listPhotosToDisplay = array;
        document.querySelector('.photosList').scrollTop = 0;
      });
    });

    $scope.sort = function (sortField) {
      //if user doesn't change field
      if ($scope.sortField == sortField) {
        $scope.sortDesc = !$scope.sortDesc;
      }
      $scope.sortField = sortField;
      getListPhotosToDisplay(PhotosService.getListPhotos().then(function (array) {
          if (sortField !== 'resolution') {
            return filterPhotos(array, $scope.filter).sort(function (a, b) {
              return compareWithField(a, b, sortField);
            });
          } else {
            return filterPhotos(array, $scope.filter).sort(function (a, b) {
              return compareWithResolution(a, b, sortField);
            });
          }
        }
      )).
        then(function (array) {
          $scope.listPhotosToDisplay = array;
          document.querySelector('.photosList').scrollTop = 0;
        });
    };

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
        if (a['path'] < b['path']) {
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

    function filterPhotos(listPhotos, filter) {
      var ret = listPhotos;

      if (filter) {
        if (filter.text && filter.text.length > 0) {
          filter.text.split(' ').forEach(function (e) {
            ret = ret.filter(function (photo) {
              return photo.path.toUpperCase().indexOf(e.toUpperCase()) !== -1;
            });
          });
        }
        if (filter.dateStart) {
          ret = ret.filter(function (photo) {
            return photo.date >= filter.dateStart.getTime();
          })
        }
        if (filter.dateEnd) {
          var dateEnd = filter.dateEnd.getTime() + 86400000;
          ret = ret.filter(function (photo) {
            return photo.date <= dateEnd;
          })
        }


        if (filter.dateLastModifiedStart) {
          ret = ret.filter(function (photo) {
            return photo.date >= filter.dateLastModifiedStart.getTime();
          })
        }
        if (filter.dateLastModifiedEnd) {
          var dateEnd = filter.dateLastModifiedEnd.getTime() + 86400000;
          ret = ret.filter(function (photo) {
            return photo.date <= dateEnd;
          })
        }

        if (filter.resolutionMin) {
          ret = ret.filter(function (photo) {
            return photo.width * photo.height >= filter.resolutionMin * 1000000;
          })
        }
        if (filter.resolutionMax) {
          ret = ret.filter(function (photo) {
            return photo.width * photo.height <= filter.resolutionMax * 1000000;
          })
        }
      }

      return ret;
    }

  }
)
;
