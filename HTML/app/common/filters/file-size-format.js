'use strict';

/**
 * @ngdoc AngularJS byte format filter
 * @name htmlApp.filter:fileSizeFormat
 * @function
 * @description
 * # fileSizeFormat
 * Filter in the htmlApp.
 */
angular.module('htmlApp')
  .filter('fileSizeFormat', function () {
    return function (bytes, precision) {
      if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
        return '-';
      }
      if (typeof precision === 'undefined') {
        precision = 1;
      }
      var units = ['o', 'ko', 'Mo', 'Go', 'To', 'Po'],
        number = Math.floor(Math.log(bytes) / Math.log(1024));
      return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
    };
  });
