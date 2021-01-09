package org.jitsi.meet.sdk;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.jitsi.meet.sdk.log.JitsiMeetLogger;

/**
 * Module implementing an API for sending events from JavaScript to native code.
 */
@ReactModule(name = EventManager.NAME)
class EventManager
    extends ReactContextBaseJavaModule {

    public static final String NAME = "EventManager";

    private static final String TAG = NAME;

    /**
     * Initializes a new module instance. There shall be a single instance of
     * this module throughout the lifetime of the app.
     *
     * @param reactContext the {@link ReactApplicationContext} where this module
     * is created.
     */
    public EventManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /**
     * Gets the name of this module to be used in the React Native bridge.
     *
     * @return The name of this module to be used in the React Native bridge.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Dispatches an event that occurred on the JavaScript side of the SDK to
     * the specified {@link BaseReactView}'s listener.
     *
     * @param name The name of the event.
     */
    @ReactMethod
    public void sendEvent(String name) {

        WritableMap payload = Arguments.createMap();
        // Put data to map
        payload.putString("EventName", name);
        // The JavaScript App needs to provide uniquely identifying information
        // to the native ExternalAPI module so that the latter may match the
        // former to the native BaseReactView which hosts it.
      this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("APIEvent", payload);
    }
}