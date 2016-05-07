angular.module('htmlApp')
  .config(function ($stateProvider) {
    $stateProvider.state('main.photo',
      {
        url: 'photo?path',
        templateUrl: 'main/photo/view.html',
        controller: 'MainPhotoCtrl as photoCtrl',
        resolve: {
          photoLight: function ($stateParams, PhotosService) {
            return PhotosService.getPhotoFromPath($stateParams.path);
          }
        }
      });
  });
