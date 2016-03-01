'use strict';

describe('Filter: fileSizeFormat', function () {

  // load the filter's module
  beforeEach(module('htmlApp'));

  // initialize a new instance of the filter before each test
  var fileSizeFormat;
  beforeEach(inject(function ($filter) {
    fileSizeFormat = $filter('fileSizeFormat');
  }));

  it('should return the input prefixed with "fileSizeFormat filter:"', function () {
    var text = 'angularjs';
    expect(fileSizeFormat(text)).toBe('fileSizeFormat filter: ' + text);
  });

});
