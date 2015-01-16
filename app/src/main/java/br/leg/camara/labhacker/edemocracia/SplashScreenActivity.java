package br.leg.camara.labhacker.edemocracia;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import br.leg.camara.labhacker.edemocracia.liferay.auth.CookieAuthenticator;
import br.leg.camara.labhacker.edemocracia.liferay.Session;


public class SplashScreenActivity extends Activity {

    private IsAuthenticatedTask isAuthenticatedTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        // Kick off background task to check if the user is authenticated
        showProgress(true);
        isAuthenticatedTask = new IsAuthenticatedTask();
        isAuthenticatedTask.execute((Void) null);
    }

    public void showProgress(final boolean show) {
        // The Splash Screen IS a loading screen. No need to show a spinner here.
    }

    class IsAuthenticatedTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean authenticated = false;

            try {
                // Check if the stored credentials are still authenticated
                Session session = SessionProvider.getSession(getApplication());
                authenticated = CookieAuthenticator.isAuthenticated(session);
            } catch (Exception e) {
                // TODO FIXME Deal with errors
                Log.e(getClass().getSimpleName(), "Failed to check if the user is authenticated. " + e.toString());
            }

            return authenticated;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
            finish();
        }

        @Override
        protected void onCancelled() {
            isAuthenticatedTask = null;
            showProgress(false);
        }
    }
}
