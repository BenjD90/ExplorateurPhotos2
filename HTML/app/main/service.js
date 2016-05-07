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

      var thumbnailWidth = 100;
      if (width) {
        var ratio = width / height;
        thumbnailWidth = Math.ceil(thumbnailHeight * ratio);
      }

      return {
        heightThumbnail: thumbnailHeight,
        widthThumbnail: thumbnailWidth
      };
    };

    /**
     * Can change photo.hieghtThumbnail
     * @param listPhotos
     * @param width
     * @param photoMargin
     * @returns {Array}
     */
    var transformListPhotoToBeDisplayed = function (listPhotos, width, photoMargin) {
      var ret = [];
      for (var i = 0; i < listPhotos.length;) {
        var line = [];
        var sumWidth = 0;
        while (sumWidth < width && i < listPhotos.length) {
          var photo = listPhotos[i];
          if (sumWidth + photo.widthThumbnail + photoMargin <= width) {
          } else if (photo.widthThumbnail + photoMargin > width) {
            var ratio = photo.heightThumbnail / photo.widthThumbnail;
            photo.widthThumbnail = width - photoMargin;
            photo.heightThumbnail = Math.ceil(ratio * photo.widthThumbnail);
          } else {
            break;
          }
          sumWidth += photo.widthThumbnail + photoMargin;
          line.push(photo);
          i++;
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
      getListPhotosToDisplay: function (listPhotos, width, photoMargin) {
        return transformListPhotoToBeDisplayed(listPhotos, width, photoMargin);
      },
      getPhotoFromPath: function (path) {
        var deferred = $q.defer();
        this.getListPhotos().then(function () {
          listPhotos.forEach(function (photo) {
            if (photo.path === path) {
              deferred.resolve(photo);
            }
          });
        });
        return deferred.promise;
      },
      filterPhotos: function (listPhotos, filter) {
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
            });
          }
          if (filter.dateEnd) {
            var dateEnd = filter.dateEnd.getTime() + 86400000;//24h
            ret = ret.filter(function (photo) {
              return photo.date <= dateEnd;
            });
          }


          if (filter.dateLastModifiedStart) {
            ret = ret.filter(function (photo) {
              return photo.date >= filter.dateLastModifiedStart.getTime();
            });
          }
          if (filter.dateLastModifiedEnd) {
            var dateEnd2 = filter.dateLastModifiedEnd.getTime() + 86400000;
            ret = ret.filter(function (photo) {
              return photo.date <= dateEnd2;
            });
          }

          if (filter.resolutionMin) {
            ret = ret.filter(function (photo) {
              return photo.width * photo.height >= filter.resolutionMin * 1000000;
            });
          }
          if (filter.resolutionMax) {
            ret = ret.filter(function (photo) {
              return photo.width * photo.height <= filter.resolutionMax * 1000000;
            });
          }


          if (filter.selected) {
            ret = ret.filter(function (photo) {
              return photo.selected !== null;
            });

            if (filter.dateSelectedStart) {
              ret = ret.filter(function (photo) {
                return filter.selected0 || photo.selected >= filter.dateSelectedStart.getTime();
              });
            }
            if (filter.dateSelectedEnd) {
              ret = ret.filter(function (photo) {
                return filter.selected0 || photo.selected <= filter.dateSelectedEnd.getTime();
              });
            }
          }


        }
        var deferred = $q.defer();
        deferred.resolve(ret);
        return deferred.promise;
      }
    };
  }
)
;
