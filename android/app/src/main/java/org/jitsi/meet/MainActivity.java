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

package org.jitsi.meet;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.facebook.react.ReactRootView;
import com.facebook.react.modules.core.PermissionListener;
import org.jitsi.meet.sdk.AudioModeModule;
import org.jitsi.meet.sdk.BaseReactView;
import org.jitsi.meet.sdk.ConnectionService;
import org.jitsi.meet.sdk.EventManager;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetFragment;
import org.jitsi.meet.sdk.JitsiMeetOngoingConferenceService;
import org.jitsi.meet.sdk.JitsiMeetViewListener;
import org.jitsi.meet.sdk.ReactInstanceManagerHolder;
import org.jitsi.meet.sdk.log.JitsiMeetLogger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;



/**
 * A base activity for SDK users to embed. It uses {@link JitsiMeetFragment} to do the heavy
 * lifting and wires the remaining Activity lifecycle methods so it works out of the box.
 */
public class MainActivity extends FragmentActivity
    implements JitsiMeetActivityInterface, JitsiMeetViewListener {

    protected static final String TAG = org.jitsi.meet.sdk.JitsiMeetActivity.class.getSimpleName();

    private ReactRootView reactRootView;
    private static final String ACTION_JITSI_MEET_CONFERENCE = "org.jitsi.meet.CONFERENCE";
    private static final String JITSI_MEET_CONFERENCE_OPTIONS = "JitsiMeetConferenceOptions";

    protected static int BACKGROUND_COLOR = 0xFF111111;
    // Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View decorView = getWindow().getDecorView();
        // Show Status Bar.
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_main);
        URL serverURL;
        try {
            // When using JaaS, replace "https://meet.jit.si" with the proper serverURL
            serverURL = new URL("https://meet.jit.si");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid server URL!");
        }
        JitsiMeetConferenceOptions defaultOptions
            = new JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverURL)
            // When using JaaS, set the obtained JWT here
            //.setToken("MyJWT")
            .setWelcomePageEnabled(true)
            .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

    }

    @Override
    public void onDestroy() {
        // Here we are trying to handle the following corner case: an application using the SDK
        // is using this Activity for displaying meetings, but there is another "main" Activity
        // with other content. If this Activity is "swiped out" from the recent list we will get
        // Activity#onDestroy() called without warning. At this point we can try to leave the
        // current meeting, but when our view is detached from React the JS <-> Native bridge won't
        // be operational so the external API won't be able to notify the native side that the
        // conference terminated. Thus, try our best to clean up.
        leave();
        if (AudioModeModule.useConnectionService()) {
            ConnectionService.abortConnections();
        }
        JitsiMeetOngoingConferenceService.abort(this);

        super.onDestroy();
    }

    @Override
    public void finish() {
        leave();

        super.finish();
    }

    public void createReactRootView(String appName, @Nullable Bundle props) {
        if (props == null) {
            props = new Bundle();
        }
        String externalAPIScope = UUID.randomUUID().toString();
        props.putString("externalAPIScope", externalAPIScope);

        if (reactRootView == null) {
            reactRootView = new ReactRootView(getApplicationContext());
            reactRootView.startReactApplication(
                ReactInstanceManagerHolder.getReactInstanceManager(),
                appName,
                props);
        } else {
            reactRootView.setAppProperties(props);
        }
    }
    public void join(JitsiMeetConferenceOptions options) {
            createReactRootView("App",options != null ? options.asProps() : new Bundle());
    }
    public void leave() {
        createReactRootView("App",new Bundle());
    }

    private @Nullable JitsiMeetConferenceOptions getConferenceOptions(Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                return new JitsiMeetConferenceOptions.Builder().setRoom(uri.toString()).build();
            }
        } else if (ACTION_JITSI_MEET_CONFERENCE.equals(action)) {
            return intent.getParcelableExtra(JITSI_MEET_CONFERENCE_OPTIONS);
        }

        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        JitsiMeetActivityDelegate.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }


    @Override
    protected void onUserLeaveHint() {
        leave();
    }


    @Override
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        JitsiMeetActivityDelegate.requestPermissions(this, permissions, requestCode, listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onConferenceJoined(Map<String, Object> data) {
        Toast.makeText(getApplicationContext(),"JOINED",Toast.LENGTH_SHORT).show();
        JitsiMeetLogger.i("Conference joined: " + data);
        // Launch the service for the ongoing notification.
        JitsiMeetOngoingConferenceService.launch(this);
    }

    @Override
    public void onConferenceTerminated(Map<String, Object> data) {
        Toast.makeText(getApplicationContext(),"DISCONNECTED",Toast.LENGTH_SHORT).show();
        JitsiMeetLogger.i("Conference terminated: " + data);
    }

    @Override
    public void onConferenceWillJoin(Map<String, Object> data) {
        Toast.makeText(getApplicationContext(),"WILL JOIN",Toast.LENGTH_SHORT).show();
        JitsiMeetLogger.i("Conference will join: " + data);
    }

    public void onButtonClick(View view) {

        EventManager module = ReactInstanceManagerHolder.getNativeModule(EventManager.class);
        int id = view.getId();
        if (id == org.jitsi.meet.sdk.R.id.btnMute) {
            if (module != null) {
                try {
                    module.sendEvent("audiomute");
                } catch (RuntimeException re) {
                    JitsiMeetLogger.e(re, re.getMessage());
                }
            }
        } else if (id == org.jitsi.meet.sdk.R.id.btnEnd) {
            if (module != null) {
                try {
                    module.sendEvent("disconnect");
                } catch (RuntimeException re) {
                    JitsiMeetLogger.e(re, re.getMessage());
                }
            }
        } else if (id == org.jitsi.meet.sdk.R.id.btnVideoMute) {
            if (module != null) {
                try {
                    module.sendEvent("mutevideo");
                } catch (RuntimeException re) {
                    JitsiMeetLogger.e(re, re.getMessage());
                }
            }
        }else if (id == org.jitsi.meet.sdk.R.id.btnStartCall) {
            JitsiMeetConferenceOptions options
                = new JitsiMeetConferenceOptions.Builder()
                .setRoom("NIKS")
                .setAudioOnly(true)
                .setVideoMuted(true)
                .build();

            join(options);
            JitsiMeetActivityDelegate.onNewIntent(getIntent());
        }
    }
}
