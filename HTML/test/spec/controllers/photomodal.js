'use strict';

describe('Controller: PhotomodalCtrl', function () {

  // load the controller's module
  beforeEach(module('htmlApp'));

  var PhotomodalCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PhotomodalCtrl = $controller('PhotomodalCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(PhotomodalCtrl.awesomeThings.length).toBe(3);
  });
});
