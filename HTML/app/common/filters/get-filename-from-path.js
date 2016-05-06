'use strict';

/**
 * @ngdoc filter
 * @name htmlApp.filter:getFileNameFromPath
 * @function
 * @description
 * # getFileNameFromPath
 * Filter in the htmlApp.
 */
angular.module('htmlApp')
  .filter('getFileNameFromPath', function () {
    return function (input) {
      return input.substr(Math.max(input.lastIndexOf('/'), input.lastIndexOf('\\')) + 1);
    };
  });
