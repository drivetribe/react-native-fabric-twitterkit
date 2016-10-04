package com.tkporter.fabrictwitterkit;

import android.app.Activity;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;


class FabricTwitterKitModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    // 112 is the average ascii value for every letter in 'twitter'
    private static final int REQUEST_CODE = 112112;

    private TwitterLoginButton loginButton;
    private Callback callback = null;

    FabricTwitterKitModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @Override
    public String getName() {
        return "FabricTwitterKit";
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                sendCallback(true, false, false);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                sendCallback(false, true, false);
            }
        }
        if (loginButton != null) {
            loginButton.onActivityResult(requestCode, resultCode, data);
            loginButton = null;
        }
    }

    public void onNewIntent(Intent intent) {
    }

    private void sendCallback(Boolean completed, Boolean cancelled, Boolean error) {
        if (callback != null) {
            callback.invoke(completed, cancelled, error);
            callback = null;
        }
    }

    @ReactMethod
    public void composeTweet(ReadableMap options, final Callback callback) {
        try {
            this.callback = callback;

            String body = options.hasKey("body") ? options.getString("body") : "";

            ReactApplicationContext reactContext = getReactApplicationContext();
            if (reactContext != null) {
                TweetComposer.Builder builder = new TweetComposer.Builder(reactContext).text(body);
                final Intent intent = builder.createIntent();
                reactContext.startActivityForResult(intent, REQUEST_CODE, intent.getExtras());
            }

        } catch (Exception e) {
            sendCallback(false, false, true);
            throw e;
        }
    }

    @ReactMethod
    public void login(final Callback callback) {

        loginButton = new TwitterLoginButton(getCurrentActivity());
        loginButton.setCallback(new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> sessionResult) {
                WritableMap result = new WritableNativeMap();
                result.putString("authToken", sessionResult.data.getAuthToken().token);
                result.putString("authTokenSecret", sessionResult.data.getAuthToken().secret);
                result.putString("userID", sessionResult.data.getUserId() + "");
                result.putString("userName", sessionResult.data.getUserName());
                callback.invoke(null, result);
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
                callback.invoke(exception.getMessage());
            }
        });

        loginButton.performClick();
    }

    @ReactMethod
    public void logOut() {
        TwitterCore.getInstance().logOut();
    }

    private boolean hasValidKey(String key, ReadableMap options) {
        return options.hasKey(key) && !options.isNull(key);
    }

}

