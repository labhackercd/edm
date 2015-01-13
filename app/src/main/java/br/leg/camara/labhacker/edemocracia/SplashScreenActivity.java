package br.leg.camara.labhacker.edemocracia;

import android.content.Intent;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

import br.leg.camara.labhacker.edemocracia.liferay.AuthenticationHelper;


public class SplashScreenActivity extends Activity {

    private IsAuthenticatedTask isAuthenticatedTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (savedInstanceState == null) {
        }

        // Kick off background task to check if the user is authenticated
        showProgress(true);
        isAuthenticatedTask = new IsAuthenticatedTask();
        isAuthenticatedTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void showProgress(final boolean show) {
        // The Splash Screen IS a loading screen. No need to show a spinner here.
        // NOOP
    }

    class IsAuthenticatedTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean authenticated = false;

            try {
                // Check if the stored credentials are still authenticated
                AuthenticationHelper helper = new AuthenticationHelper(Application.SERVICE_LOGIN_URL);

                Application application = (Application) getApplication();

                authenticated = helper.credentialsAreStillValid(application.getCredentials());
            } catch (IOException e) {
                // TODO FIXME Deal with errors
            }

            return authenticated;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (!success) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), GroupListActivity.class));
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
