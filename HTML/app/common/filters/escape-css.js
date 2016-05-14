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
  .filter('escapeForCssUrl', function () {
    return function (toFilter) {
      return toFilter.replace("'", "\\\'").replace("(", "\\(").replace(")", "\\)").replace("\"", "\\\"");
    };
  });
