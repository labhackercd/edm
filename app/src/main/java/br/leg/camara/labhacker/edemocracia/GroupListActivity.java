package br.leg.camara.labhacker.edemocracia;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.app.Fragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.leg.camara.labhacker.edemocracia.content.Group;
import br.leg.camara.labhacker.edemocracia.liferay.LiferayClient;


public class GroupListActivity extends Activity implements GroupListFragment.OnGroupSelectedListener {

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
    public void onGroupSelected(Uri groupUri) {
        Log.v(getClass().getSimpleName(), "Group selected: " + groupUri.toString());
    }

    public class LoadGroupsTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            LiferayClient client = ((Application) getApplication()).getLiferayClient();

            JSONArray result;
            try {
                // 10131 is the default companyId for edemocracia
                // FIXME this should be parametrized or global or w/e
                int companyId = 10131;
                result = client.listGroups(companyId);
            } catch (Exception e) {
                // TODO FIXME Notify error
                Log.e(this.getClass().getName(), "Failed to retrieve group list: " + e.toString());
                return false;
            }

            groups = new ArrayList<String>(result.length());

            for (int i = 0; i < result.length(); i++) {
                try {
                    Group group = Group.fromJSONObject(result.getJSONObject(i));

                    // Ignore non public (type != 1) or inactive (active != true) groups
                    // FIXME We should probably place this filter at some other layer.
                    if (!group.isActive() || group.getType() != 1) {
                        continue;
                    }

                    groups.add(group.getName());
                } catch (JSONException e) {
                    // FIXME XXX Silently ignore problem
                    Log.w(this.getClass().getSimpleName(), "Failed to load Group from JSONArray: " + e.toString());
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            loadGroupsTask = null;
            showProgress(false);

            if (!success) {
                groups = new ArrayList<String>();
            }

            getFragmentManager().beginTransaction()
                    .add(R.id.container, (Fragment) GroupListFragment.newInstance(groups))
                    .commit();
        }

        @Override
        protected void onCancelled() {
            loadGroupsTask = null;
            showProgress(false);
        }
    }
}
