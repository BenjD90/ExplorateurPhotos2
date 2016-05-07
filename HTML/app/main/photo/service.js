'use strict';

/**
 * @ngdoc service
 * @name htmlApp.photoModalService
 * @description
 * # photoModalService
 * Factory in the htmlApp.
 */
angular.module('htmlApp')
  .factory('photoModalService', function () {
    var modalInstance;
    return {
      getModalInstance: function () {
        return modalInstance;
      },
      setModalInstance: function (modal) {
        modalInstance = modal;
      }
    };
  });
