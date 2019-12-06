/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.media;

import android.Manifest;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import static org.apache.cordova.media.AudioHandler.RECORD_AUDIO;

/**
 * This class called by CordovaActivity to play and record audio.
 * The file can be local or over a network using http.
 * <p>
 * Audio formats supported (tested):
 * .mp3, .wav
 * <p>
 * Local audio files must reside in one of two places:
 * android_asset: 		file name must start with /android_asset/sound.mp3
 * sdcard:				file name is just sound.mp3
 */
public class MediaFileHelperHandler extends CordovaPlugin {

    public static String TAG = "MediaFileHelperHandler";


    public static String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static int WRITE_EXTERNAL_STORAGE = 1;

    public static final int PERMISSION_DENIED_ERROR = 20;

    private String action;
    private String filename;
    private String base64;
    private CallbackContext tempCallbackContext;

    /**
     * Constructor.
     */
    public MediaFileHelperHandler() {
    }


    protected void getWritePermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, permissions[WRITE_EXTERNAL_STORAGE]);
    }


    protected void getMicPermission(int requestCode) {
        PermissionHelper.requestPermission(this, requestCode, permissions[RECORD_AUDIO]);
    }


    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        CordovaResourceApi resourceApi = webView.getResourceApi();
        String result = "";

        if (action.equals("downloadAudioFile")) {
            final String filename = args.getString(0);
            final String base64 = args.getString(1);

            promptForAction(action, filename, base64, callbackContext);

            return true;

        } else if (action.equals("exists")) {
            final String filename = args.getString(0);

            promptForAction(action, filename, null, callbackContext);

            return true;
        } else { // Unrecognized action.
            return false;
        }

        //callbackContext.sendPluginResult(new PluginResult(status, result));

        //return true;
    }


    /**
     * Stop all audio players and recorders on navigate.
     */
    @Override
    public void onReset() {

    }


    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.tempCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }
        doActon();
    }

    /*
     * This little utility method catch-all work great for multi-permission stuff.
     *
     */

    private void promptForAction(String action, String filename, String base64, CallbackContext callbackContext ) {

        this.action = action;
        this.filename = filename;
        this.base64 = base64;
        this.tempCallbackContext = callbackContext;

        if (PermissionHelper.hasPermission(this, permissions[WRITE_EXTERNAL_STORAGE]) &&
                PermissionHelper.hasPermission(this, permissions[RECORD_AUDIO])) {
            //this.startRecordingAudio(recordId, FileHelper.stripFileProtocol(fileUriStr));
            doActon();

        } else {

            if (PermissionHelper.hasPermission(this, permissions[RECORD_AUDIO])) {
                getWritePermission(WRITE_EXTERNAL_STORAGE);
            } else {
                getMicPermission(RECORD_AUDIO);
            }
        }

    }

    private void doActon(){
        final CordovaPlugin handler = this;
        //String action, String filename, String base64
        if("downloadAudioFile".equals(this.action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String realFilename = FileHelper.downloadAudioFile(handler, filename, base64);
                        tempCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, realFilename));
                    } catch (IOException ex) {
                        tempCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, ex.getMessage()));
                    }

                }
            });
        } else if ("exists".equals(this.action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    String realFilename = FileHelper.exists(handler, filename);
                    tempCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, realFilename));

                }
            });
        }


    }

}
