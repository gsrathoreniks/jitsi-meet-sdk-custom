/*
 * Copyright @ 2017-present 8x8, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.meet.sdk;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.log.JitsiMeetLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Module implementing a simple API to select the appropriate audio device for a
 * conference call.
 *
 * Audio calls should use {@code AudioModeModule.AUDIO_CALL}, which uses the
 * builtin earpiece, wired headset or bluetooth headset. The builtin earpiece is
 * the default audio device.
 *
 * Video calls should should use {@code AudioModeModule.VIDEO_CALL}, which uses
 * the builtin speaker, earpiece, wired headset or bluetooth headset. The
 * builtin speaker is the default audio device.
 *
 * Before a call has started and after it has ended the
 * {@code AudioModeModule.DEFAULT} mode should be used.
 */
@ReactModule(name = JitsiCallbackModule.NAME)
class JitsiCallbackModule extends ReactContextBaseJavaModule{
    public static final String NAME = "JitsiCallbackModule";
    private static final String TAG = "JitsiCallbackModule";

    public JitsiCallbackModule(@NonNull @NotNull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /**
     * Gets the name for this module to be used in the React Native bridge.
     *
     * @return a string with the module name.
     */
    @Override
    public String getName() {
        return NAME;
    }


    @ReactMethod
    public void isCallDisconnected(String temp) {
        Log.d(TAG, "isCallDisconnected: "+temp);
        Toast.makeText(getReactApplicationContext(),"Callback says\n\nCall Disconnected : "+temp,Toast.LENGTH_SHORT).show();
    }

    @ReactMethod
    public void isAudioMuted(String temp) {
        Log.d(TAG, "isAudioMuted: "+temp);
        Toast.makeText(getReactApplicationContext(),"Callback says\n\nAudio muted : "+temp,Toast.LENGTH_SHORT).show();
    }

    @ReactMethod
    public void isVideoMuted(Boolean temp) {
        Log.d(TAG, "isVideoMuted: "+temp);
        Toast.makeText(getReactApplicationContext(),"Callback says\n\nVideo muted : "+temp,Toast.LENGTH_SHORT).show();
    }
    @ReactMethod
    public void isCallStarted(String temp) {
        Log.d(TAG, "isCallStarted: "+temp);
        Toast.makeText(getReactApplicationContext(),"Callback says\n\nCall started : "+temp,Toast.LENGTH_SHORT).show();
    }
}
