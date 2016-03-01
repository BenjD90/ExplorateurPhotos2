'use strict';

describe('Service: PhotosService', function () {

  // load the service's module
  beforeEach(module('htmlApp'));

  // instantiate service
  var PhotosService;
  beforeEach(inject(function (_PhotosService_) {
    PhotosService = _PhotosService_;
  }));

  it('should do something', function () {
    expect(!!PhotosService).toBe(true);
  });

});
