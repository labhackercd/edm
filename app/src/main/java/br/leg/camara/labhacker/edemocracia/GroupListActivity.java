package br.leg.camara.labhacker.edemocracia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;

public class GroupListActivity extends ActionBarActivity implements GroupListFragment.OnFragmentInteractionListener {

    private List<String> groups = null;
    private LoadGroupsTask loadGroupsTask = null;

    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        if (savedInstanceState == null) {
            progressView = findViewById(R.id.group_list_update_progress);
            updateList();
        }
    }

    private void updateList() {
        showProgress(true);
        loadGroupsTask = new LoadGroupsTask();
        loadGroupsTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the progress UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            */

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onFragmentInteraction(String id) {
        // TODO FIXME Can I leave this empty?
    }

    public class LoadGroupsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            LiferayClient client = ((Application) getApplication()).getLiferayClient();


            JSONArray result;
            try {
                result = client.listGroups(1);
            } catch (Exception e) {
                // TODO FIXME Notify error
                result = new JSONArray();
                Log.e(this.getClass().getName(), "Failed to retrieve group list: " + e.toString());
                return false;
            }

            groups = new ArrayList<String>(result.length());

            for (int i = 0; i < result.length(); i++) {
                try {
                    JSONObject o = result.getJSONObject(i);
                    groups.add(o.getString("name"));
                } catch (JSONException e) {
                    // FIXME XXX Silently ignore problem
                    Log.w(this.getClass().getSimpleName(), "Failed to get Group object from JSONArray: " + e.toString());
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            loadGroupsTask = null;
            showProgress(false);

            if (success) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, (Fragment) GroupListFragment.newInstance(groups))
                        .commit();
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            loadGroupsTask = null;
            showProgress(false);
        }
    }
}
