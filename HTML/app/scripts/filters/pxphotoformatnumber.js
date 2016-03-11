'use strict';

/**
 * @ngdoc filter
 * @name htmlApp.filter:pxPhotoFormatNumber
 * @function
 * @description
 * # pxPhotoFormatNumber
 * Filter in the htmlApp.
 */
angular.module('htmlApp')
  .filter('pxPhotoFormatNumber', function () {
    return function (nbPx, precision) {
      if (isNaN(parseFloat(nbPx)) || !isFinite(nbPx)) return '-';
      if (typeof precision === 'undefined') precision = 1;
      var units = ['px', 'Kpx', 'Mpx', 'Gpx', 'Tpx', 'Ppx'],
        number = Math.floor(Math.log(nbPx) / Math.log(1000));
      return (nbPx / Math.pow(1000, Math.floor(number))).toFixed(precision) + ' ' + units[number];
    }
  });
