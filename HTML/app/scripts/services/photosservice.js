'use strict';

/**
 * @ngdoc service
 * @name htmlApp.PhotosService
 * @description
 * # PhotosService
 * Service in the htmlApp.
 */
angular.module('htmlApp')
  .service('PhotosService', function (Config, $http) {
    this.getListPhotos = function () {
      return $http.get(Config.urlServices + '/photos');
    };
  });
