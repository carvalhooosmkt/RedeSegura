package com.redesegura.modules;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.redesegura.accessibility.RedeSeguraAccessibilityService;

public class AccessibilityModule extends ReactContextBaseJavaModule {

    private static final String TAG = "AccessibilityModule";
    private ReactApplicationContext reactContext;

    public AccessibilityModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AccessibilityModule";
    }

    @ReactMethod
    public void isAccessibilityEnabled(Promise promise) {
        try {
            boolean isEnabled = isAccessibilityServiceEnabled();
            promise.resolve(isEnabled);
        } catch (Exception e) {
            promise.reject("ACCESSIBILITY_CHECK_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void openAccessibilitySettings(Promise promise) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reactContext.startActivity(intent);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("ACCESSIBILITY_SETTINGS_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void startAccessibilityService(Promise promise) {
        try {
            if (isAccessibilityServiceEnabled()) {
                // Ativar proteção no serviço
                RedeSeguraAccessibilityService service = getAccessibilityServiceInstance();
                if (service != null) {
                    service.setProtectionActive(true);
                }
                promise.resolve(true);
            } else {
                promise.reject("SERVICE_NOT_ENABLED", "Serviço de acessibilidade não está habilitado");
            }
        } catch (Exception e) {
            promise.reject("START_SERVICE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void stopAccessibilityService(Promise promise) {
        try {
            RedeSeguraAccessibilityService service = getAccessibilityServiceInstance();
            if (service != null) {
                service.setProtectionActive(false);
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("STOP_SERVICE_ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void updateSettings(String settingsJson, Promise promise) {
        try {
            RedeSeguraAccessibilityService service = getAccessibilityServiceInstance();
            if (service != null) {
                service.updateAISettings(settingsJson);
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("UPDATE_SETTINGS_ERROR", e.getMessage());
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String enabledServices = Settings.Secure.getString(
            reactContext.getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabledServices == null) return false;

        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);

        while (splitter.hasNext()) {
            String serviceName = splitter.next();
            if (serviceName.contains("RedeSeguraAccessibilityService")) {
                Log.d(TAG, "Serviço de acessibilidade está habilitado");
                return true;
            }
        }

        Log.d(TAG, "Serviço de acessibilidade NÃO está habilitado");
        return false;
    }

    private RedeSeguraAccessibilityService getAccessibilityServiceInstance() {
        // Implementar obtenção da instância do serviço
        // Isso requer um singleton ou registry pattern
        return RedeSeguraAccessibilityServiceRegistry.getInstance();
    }
}