'use strict';

/**
 * @ngdoc service
 * @name htmlApp.PhotosService
 * @description
 * # PhotosService
 * Service in the htmlApp.
 */
angular.module('htmlApp')
  .factory('PhotosService', function (Config, $http, $q) {

    var listPhotos;

    var addThumbnailSizesToPhotos = function (listPhotos) {

      listPhotos.forEach(function (e, i) {
        angular.extend(listPhotos[i], calculateNewSizes(e, Config.thumbnailsHeight));
      });

      return listPhotos;
    };

    var calculateNewSizes = function (photo, thumbnailHeight) {
      var height = photo.height;
      var width = photo.width;

      var ratio = width / height;

      var thumbnailWidth = Math.ceil(thumbnailHeight * ratio);

      return {
        heightThumbnail: thumbnailHeight,
        widthThumbnail: thumbnailWidth
      };
    };

    var transformListPhotoToBeDisplayed = function (listPhotos, width, photoMargin) {
      var ret = [];
      for (var i = 0; i < listPhotos.length; i++) {
        var line = [];
        var sumWidth = 0;
        while (sumWidth < width && i < listPhotos.length) {
          var photo = listPhotos[i];
          if (sumWidth + photo.widthThumbnail + photoMargin <= width) {
            line.push(photo);
            i++;
            sumWidth += photo.widthThumbnail + photoMargin;
          } else if (photo.widthThumbnail + photoMargin > width) {
            i++;
            console.error('Photo ignored');
          } else {
            break;
          }
        }
        ret.push(line);
        sumWidth = 0;
      }
      return ret;
    };

    return {
      getListPhotos: function () {
        if (!listPhotos) {
          return $http.get(Config.urlServices + '/photos').then(function (response) {
            listPhotos = addThumbnailSizesToPhotos(response.data);
            return listPhotos;
          });
        } else {
          var deferred = $q.defer();
          deferred.resolve(listPhotos);
          return deferred.promise;
        }
      },
      getListPhotosToDisplay: function (width, photoMargin) {
        var ref = this;
        var ret = ref.getListPhotos().then(function (listPhotos) {
          return transformListPhotoToBeDisplayed(listPhotos, width, photoMargin);
        });
        return ret;
      }
    }
  }
)
;
