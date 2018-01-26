package com.pluscubed.plustimer;

import android.app.Application;
import android.os.StrictMode;
import android.support.v7.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;

import com.pluscubed.plustimer.R;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }



        PreferenceManager.setDefaultValues(this,R.xml.preferences , false);

        LeakCanary.install(this);

        /*LockContext.configureLock(
                new Lock.Builder()
                        .loadFromApplication(this)
                        //.useWebView(true)
                        //.defaultDatabaseConnection(null)
                        //.withIdentityProvider(Strategies.UnknownSocial, new WebIdentityProvider(new CallbackParser(), getString(R.string.auth0_client_id), getString(R.string.auth0_domain_name)+"/authorize"))
                        //.useConnections("oauth2")
                        //.defaultDatabaseConnection("oauth2")
                        .closable(true)
        );*/
    }
}
