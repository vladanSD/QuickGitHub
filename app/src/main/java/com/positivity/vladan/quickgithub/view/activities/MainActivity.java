package com.positivity.vladan.quickgithub.view.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class MainActivity extends AppCompatActivity implements ItemAdapter.OnListItemClickInterface{

    private ItemAdapter adapter;
    private RecyclerView view;
    private Toast mToast;

    private EditText searchText;
    private ProgressBar progressBar;

    ArrayList<Item> listOfItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(":)(:");
        setContentView(R.layout.activity_main);

        searchText = (EditText) findViewById(R.id.et_search);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        view = (RecyclerView) findViewById(R.id.rv_list);

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


    //creating layout manager and adapter for recycler viewer
    private void displayData(){

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        view.setLayoutManager(layoutManager);
        view.setHasFixedSize(true);

        adapter = new ItemAdapter(listOfItems, this);
        view.setAdapter(adapter);
    }

    //calling function for creating url, and pass it to new query task
    private void makeGithubSearchQuery() {
        String githubQuery = searchText.getText().toString();
        URL githubSearchUrl = GitHubUrlBuilder.buildUrl(githubQuery);
        new GithubQueryTask().execute(githubSearchUrl);
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

    //get Data
    public class GithubQueryTask extends AsyncTask<URL, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... params) {
            try{

            URL searchUrl = params[0];
            String githubSearchResults = null;
            NetworkUtils utils = new NetworkUtils();
            try {
                githubSearchResults = utils.run(searchUrl.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return githubSearchResults;}catch (Exception e){
                System.out.println(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String githubSearchResults) {
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

