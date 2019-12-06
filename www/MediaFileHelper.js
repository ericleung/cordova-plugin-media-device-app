
var exec = require('cordova/exec');

function MediaFileHelper () {

}

MediaFileHelper.prototype.downloadAudioFile = function (filename, base64, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'MediaFileHelper', 'downloadAudioFile', [filename, base64]);
};

MediaFileHelper.prototype.exists = function (filename, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'MediaFileHelper', 'exists', [filename]);
};

module.exports = new MediaFileHelper();