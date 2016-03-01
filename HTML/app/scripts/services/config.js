'use strict';

/**
 * @ngdoc service
 * @name htmlApp.Config
 * @description
 * # Config
 * Constant in the htmlApp.
 */
angular.module('htmlApp')
  .constant('Config', {
    urlServices: '//' + location.hostname + ':8081/api'
  });
