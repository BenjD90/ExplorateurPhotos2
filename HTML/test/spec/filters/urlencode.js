'use strict';

describe('Filter: urlEncode', function () {

  // load the filter's module
  beforeEach(module('htmlApp'));

  // initialize a new instance of the filter before each test
  var urlEncode;
  beforeEach(inject(function ($filter) {
    urlEncode = $filter('urlEncode');
  }));

  it('should return the input prefixed with "urlEncode filter:"', function () {
    var text = 'angularjs';
    expect(urlEncode(text)).toBe('urlEncode filter: ' + text);
  });

});
