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

import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import org.apache.cordova.CordovaPlugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {

    /**
     * Removes the "file://" prefix from the given URI string, if applicable.
     * If the given URI string doesn't have a "file://" prefix, it is returned unchanged.
     *
     * @param uriString the URI string to operate on
     * @return a path without the "file://" prefix
     */
    public static String stripFileProtocol(String uriString) {
        if (uriString.startsWith("file://")) {
            return Uri.parse(uriString).getPath();
        }
        return uriString;
    }

    public static String getFullFilename(CordovaPlugin handler, String singleFilename) {
        String fullFilename = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            fullFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + singleFilename;
        } else {
            fullFilename = "/data/data/" + handler.cordova.getActivity().getPackageName() + "/cache/" + singleFilename;
        }

        return fullFilename;
    }

    public static String exists(CordovaPlugin handler, String filename)  {
        String full = getFullFilename(handler, filename);
        File file = getFile(full);
        if(file != null) {
            return full;
        }
        return null;
    }

    public static String downloadAudioFile(CordovaPlugin handler, String filename, String base64) throws IOException {
        String full = getFullFilename(handler, filename);
        File file = getFile(full);
        if(file != null) {
            return full;
        }

        base64ToFile(base64, full);

        return full;
    }

    public static File getFile(String fullFilename) {
        File file = new File(Environment.getExternalStorageDirectory(), fullFilename);
        if (file.exists()) {
            return file;
        }

        return null;
    }

    public static File base64ToFile(String base64, String fullFilename) throws IOException {
        File file = null;
        String fileName = fullFilename;
        FileOutputStream out = null;
        try {
            // 解码，然后将字节转换为文件
            file = new File(Environment.getExternalStorageDirectory(), fileName);
            if (!file.exists())
                file.createNewFile();
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            byte[] buffer = new byte[1024];
            out = new FileOutputStream(file);
            int bytesum = 0;
            int byteread = 0;
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread); // 文件写操作
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }
}
