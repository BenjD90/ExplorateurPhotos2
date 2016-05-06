'use strict';

describe('Filter: getFileNameFromPath', function () {

  // load the filter's module
  beforeEach(module('htmlApp'));

  // initialize a new instance of the filter before each test
  var getFileNameFromPath;
  beforeEach(inject(function ($filter) {
    getFileNameFromPath = $filter('getFileNameFromPath');
  }));

  it('should return the input prefixed with "getFileNameFromPath filter:"', function () {
    var text = 'angularjs';
    expect(getFileNameFromPath(text)).toBe('getFileNameFromPath filter: ' + text);
  });

});
