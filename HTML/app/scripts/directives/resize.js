'use strict';

/**
 * @ngdoc directive
 * @name htmlApp.directive:resize
 * @description
 * # resize
 */
angular.module('htmlApp')
  .directive('resize', function ($window) {
    return {
      restrict: 'A',
      link: function postLink(scope) {
        var w = angular.element($window);
        scope.getWindowDimensions = function () {
          return {
            'h': w.height(),
            'w': w.width()
          };
        };
        scope.$watch(scope.getWindowDimensions, function (newValue) {
          scope.windowHeight = newValue.h;
          scope.windowWidth = newValue.w;
        }, true);
        w.bind('resize', function () {
          scope.$apply();
        });
      }
    };
  });
