package com.redesegura.modules;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class OverlayModule extends ReactContextBaseJavaModule {

    private static final String TAG = "OverlayModule";

    public OverlayModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "OverlayModule";
    }

    @ReactMethod
    public void canDrawOverlays(Promise promise) {
        try {
            boolean canDraw = Settings.canDrawOverlays(getReactApplicationContext());
            promise.resolve(canDraw);
        } catch (Exception e) {
            promise.reject("OVERLAY_CHECK_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void requestOverlayPermission(Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(getReactApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getReactApplicationContext().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getReactApplicationContext().startActivity(intent);
                }
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("OVERLAY_PERMISSION_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void startOverlayService(Promise promise) {
        try {
            // Iniciar serviço de overlay
            Intent intent = new Intent(getReactApplicationContext(), OverlayService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getReactApplicationContext().startForegroundService(intent);
            } else {
                getReactApplicationContext().startService(intent);
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("START_OVERLAY_SERVICE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void stopOverlayService(Promise promise) {
        try {
            Intent intent = new Intent(getReactApplicationContext(), OverlayService.class);
            getReactApplicationContext().stopService(intent);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("STOP_OVERLAY_SERVICE_ERROR", e.getMessage());
        }
    }
}