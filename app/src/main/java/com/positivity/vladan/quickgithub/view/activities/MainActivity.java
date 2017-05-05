package com.positivity.vladan.quickgithub.view.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.positivity.vladan.quickgithub.R;
import com.positivity.vladan.quickgithub.model.Item;
import com.positivity.vladan.quickgithub.adapters.ItemAdapter;
import com.positivity.vladan.quickgithub.utilities.GitHubUrlBuilder;
import com.positivity.vladan.quickgithub.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnListItemClickInterface,
        LoaderManager.LoaderCallbacks<String>{

    private static final String SEARCH_QUERY_EXTRA = "query";
    private static final int GITHUB_LOADER = 28;
    private static final int OTHER_LOADER = 30;

    private ItemAdapter mAdapter;
    private RecyclerView view;
    private Toast mToast;

    private EditText searchText;
    private ProgressBar progressBar;

    private static ArrayList<Item> listOfItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(":)(:");
        setContentView(R.layout.activity_main);

        searchText = (EditText) findViewById(R.id.et_search);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        view = (RecyclerView) findViewById(R.id.rv_list);

        mAdapter = new ItemAdapter(listOfItems, this);

        getSupportLoaderManager().initLoader(GITHUB_LOADER, null, this);

        //displaying data to avoid skipping null attachment on recycler viewer
        displayData();

        //setting up onFocusChange
        setFocusChangedEvent(searchText);


    }

    //interface function for handling clicking on item from list
    //in this case used to create intent to url of a repository
    @Override
    public void onListItemClick(int clickedItemIndex) {
        Item item = listOfItems.get(clickedItemIndex);

        if (mToast != null) {
            mToast.cancel();
        }

        String toastMessage = "Hope you will find what you are looking for =)";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();


        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(item.getUrl()));
        startActivity(intent);
    }


    //creating layout manager and mAdapter for recycler viewer
    private void displayData(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        view.setLayoutManager(layoutManager);
        view.setHasFixedSize(true);
        view.setAdapter(mAdapter);
    }

    //starting Search
    private void makeGithubSearchQuery() {
        String githubQuery = searchText.getText().toString();
        URL githubSearchUrl = GitHubUrlBuilder.buildUrl(githubQuery);

        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_EXTRA, githubSearchUrl.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(GITHUB_LOADER);

        if(loader == null ) {
            loaderManager.initLoader(GITHUB_LOADER, bundle,this);
        }else {
            loaderManager.restartLoader(GITHUB_LOADER, bundle, this);
        }

    }

    //hiding keyboard from screen
    //used after search
    private void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //onFocusChange to start search after filling editText
    private void setFocusChangedEvent(EditText editText){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    makeGithubSearchQuery();
                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String mGitHubJson;

            @Override
            protected void onStartLoading() {
                if(args==null){
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                if(mGitHubJson!=null){
                    deliverResult(mGitHubJson);
                }else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String githubSearch = args.getString(SEARCH_QUERY_EXTRA);

                if(githubSearch==null || TextUtils.isEmpty(githubSearch)){
                    return null;
                }

                try{
                    NetworkUtils networkUtils = new NetworkUtils();
                    return networkUtils.run(githubSearch);
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String data) {
                mGitHubJson = data;
                super.deliverResult(data);
            }
        };


    }

    @Override
    public void onLoadFinished(Loader<String> loader, String githubSearchResults) {
            progressBar.setVisibility(View.INVISIBLE);
            hideKeyboard();
        if (githubSearchResults != null && !githubSearchResults.equals("")) {
            try {
                JSONObject mainJSON = new JSONObject(githubSearchResults);
                JSONArray jsonArray = (JSONArray) mainJSON.get("items");
                listOfItems.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    int id = object.getInt("id");
                    String ime = object.getString("name");
                    String description = object.getString("description");
                    String url = object.getString("html_url");
                    Item item = new Item(id, ime, description, url);
                    listOfItems.add(item);
                }

                displayData();



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

