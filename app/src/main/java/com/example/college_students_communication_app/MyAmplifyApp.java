package com.example.college_students_communication_app;

import android.app.Application;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class MyAmplifyApp extends Application {

    public void onCreate() {
        super.onCreate();

        try {
            // Add these lines to add the `AWSApiPlugin` and `AWSCognitoAuthPlugin`
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());

            Log.i("MyAmplifyApp", "Initialized Amplify.");
        } catch (AmplifyException error) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify.", error);
        }
    }
}
