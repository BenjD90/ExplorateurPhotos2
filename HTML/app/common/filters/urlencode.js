'use strict';

/**
 * @ngdoc filter
 * @name htmlApp.filter:urlEncode
 * @function
 * @description
 * # urlEncode
 * Filter in the htmlApp.
 */
angular.module('htmlApp')
  .filter('urlEncode', function () {
    return window.encodeURIComponent;
  });
