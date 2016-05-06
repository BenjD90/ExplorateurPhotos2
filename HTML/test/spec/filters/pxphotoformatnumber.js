'use strict';

describe('Filter: pxPhotoFormatNumber', function () {

  // load the filter's module
  beforeEach(module('htmlApp'));

  // initialize a new instance of the filter before each test
  var pxPhotoFormatNumber;
  beforeEach(inject(function ($filter) {
    pxPhotoFormatNumber = $filter('pxPhotoFormatNumber');
  }));

  it('should return the input prefixed with "pxPhotoFormatNumber filter:"', function () {
    var text = 'angularjs';
    expect(pxPhotoFormatNumber(text)).toBe('pxPhotoFormatNumber filter: ' + text);
  });

});
