package com.example.android.appbar;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String mUrlText = "";
    private String DEBUG_MSG = "DEBUG_NEWSAPP";
    private ArrayList<NewsItem> mNewsList;
    private ListView listView;
    Bitmap bitmap;
    ImageView img;
    NewsListAdapter mNewslistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        listView = (ListView) findViewById(R.id.list_item);

        // Hide the emptyview on create
        TextView newlistempty = (TextView) findViewById(R.id.newslistempty);
        newlistempty.setVisibility(View.GONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // Account for spaces if any
                query = query.replace(" ", "%20");
                mUrlText = "http://content.guardianapis.com/search?show-fields=thumbnail&q=%27" + query + "%27&api-key=test";
                // mUrlText = "http://content.guardianapis.com/search?q=%27" + query + "%27&api-key=test";
                Log.i("MainActivity", mUrlText);

                // Check to see if a network connection is available
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // We have a network connection - fetch new item data on a seperate thread
                    new DownloadWebpageTask().execute(mUrlText);
                } else {
                    // display error
                    Toast.makeText(getApplicationContext(), "No network connection", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("MainActivity", "Text Changed");
                // notifyDataSetChanged
                return false;
            }
        });

        return true;
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.d(DEBUG_MSG, getResources().getString(R.string.invalidurl));
                return getResources().getString(R.string.invalidurl);
            }
        }

        // Take the URL string and create the URL object
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            HttpURLConnection conn = null;

            try {
                // Valid URL
                URL url = new URL(myurl);

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(1000);
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Start the query
                conn.connect();
                int response = conn.getResponseCode();
                switch (response) {     // Validate result of request. On error, log to console
                    case HttpURLConnection.HTTP_OK:
                        Log.d("DEBUG_NEWSAPP", "Server responded");
                        // Get server response
                        is = conn.getInputStream();
                        // Convert the InputStream into a string
                        String contentAsString = readIt(is);
                        return contentAsString;
                    case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                        Log.d("DEBUG_NEWSAPP", "Server timed out");
                        break;// retry
                    case HttpURLConnection.HTTP_UNAVAILABLE:
                        Log.d("DEBUG_NEWSAPP", "Server unavailable");
                        break;// server is unstable - how would you retry?
                    default:
                        Log.d("DEBUG_NEWSAPP", "Server returned an unknown response code: " + response);
                        break;
                }
                // To keep compiler happy
                return "";
            } catch (Exception e) {
                Log.d("DEBUG_NEWSAPP", e.toString());
                return "";
            } finally {
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
                if (is != null) {
                    is.close();
                }
            }
        }

        // Convert the input stream to a String
        public String readIt(InputStream stream) throws IOException {

            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            String result = total.toString();
            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            mNewsList = new ArrayList<NewsItem>();

            try {
                JSONObject jsonRootObject = new JSONObject(result);

                JSONObject resultObj = jsonRootObject.getJSONObject("response");


                int totalItems = resultObj.getInt("total");   // Make sure total items returned by server > 0
                if (totalItems > 0) {
                    // We have results
                    //Get the instance of JSONArray that contains JSONObjects
                    JSONArray jsonArray = resultObj.optJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        NewsItem newsItem = new NewsItem();

                        // Get the section Name
                        JSONObject resultItem = jsonArray.getJSONObject(i);
                        if (!resultItem.isNull("sectionName")) {
                            String sectionName = resultItem.getString("sectionName");
                            newsItem.setSectionName(sectionName);
                        } else
                            newsItem.setSectionName(getResources().getString(R.string.nosectionname));

                        // Get the section Name
                        if (!resultItem.isNull("webTitle")) {
                            String webTitle = resultItem.getString("webTitle");
                            newsItem.setWebTitle(webTitle);
                        } else
                            newsItem.setSectionName(getResources().getString(R.string.nowebtitle));

                        // Get the news item URL
                        if (!resultItem.isNull("webUrl")) {
                            String webURL = resultItem.getString("webUrl");
                            newsItem.setUrl(webURL);
                        } else
                            newsItem.setSectionName(getResources().getString(R.string.noweburl));

                        // Get the image
                        // JSONArray jsonArrayForImage = resultItem.getJSONArray("fields");
                        JSONObject image = resultItem.getJSONObject("fields");

                        if (!image.isNull("thumbnail")) {
                            String newsitemURL = image.getString("thumbnail");
                            newsItem.setThumnailURL(newsitemURL);
                        } else
                            newsItem.setSectionName(getResources().getString(R.string.noimageurl));

                        mNewsList.add(newsItem);
                    }
                }
            } catch (JSONException e) {
                Log.d("DEBUG_", "JSON exception reading string - partial or no string returned from server");
                e.printStackTrace();
            }
            mNewslistAdapter = new NewsListAdapter(MainActivity.this, mNewsList);

            // Show empty view if nothing to display otherwise show the pulled down list
            if (mNewsList.isEmpty()) {
                Log.i(DEBUG_MSG, "News list is empty");
                // Show empty view here
                listView.setEmptyView(findViewById(R.id.newslistempty));
                mNewslistAdapter.notifyDataSetChanged();
            }

            listView.setAdapter(mNewslistAdapter);
        }
    }
}