'use strict';

describe('Service: photoModalService', function () {

  // load the service's module
  beforeEach(module('htmlApp'));

  // instantiate service
  var photoModalService;
  beforeEach(inject(function (_photoModalService_) {
    photoModalService = _photoModalService_;
  }));

  it('should do something', function () {
    expect(!!photoModalService).toBe(true);
  });

});
