package com.example.android.appbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Anirudha.Joshi on 7/11/2016.
 */
public class NewsListAdapter extends ArrayAdapter<NewsItem> {

    Bitmap bitmap;
    View listItemView;
    ViewHolder holder;

    static class ViewHolder {
        public TextView webTitleTextView, sectionNameTextView, urlTextView;
        private ImageView img;
    }


    public NewsListAdapter(Context context, ArrayList<NewsItem> newsItems) {
        super(context, 0, newsItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Implement the ViewHolder pattern to optimize adpater performance
        // View listItemView = convertView;
        listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_item, parent, false);

            holder = new ViewHolder();
            holder.webTitleTextView = (TextView) listItemView.findViewById(R.id.webTitle);
            holder.sectionNameTextView = (TextView) listItemView.findViewById(R.id.sectionName);
            holder.urlTextView = (TextView) listItemView.findViewById(R.id.url);
            holder.img = (ImageView) listItemView.findViewById(R.id.img);
            listItemView.setTag(holder);

        } else {
            holder = (ViewHolder) listItemView.getTag();
        }

        NewsItem currentLocation = getItem(position);

        holder.webTitleTextView.setText(currentLocation.getWebTitle());
        holder.sectionNameTextView.setText(currentLocation.getSectionName());

        // Linkify the links text
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.urlTextView.setText(Html.fromHtml(currentLocation.getUrl(), 0));
        } else {
            holder.urlTextView.setText(Html.fromHtml(currentLocation.getUrl()));
        }
        holder.urlTextView.setAutoLinkMask(Linkify.WEB_URLS);

        new LoadImage().execute(currentLocation.getThumbnailURL());

        return listItemView;
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            try {
                if (image != null) {
                    holder.img = (ImageView) listItemView.findViewById(R.id.img);
                    holder.img.setImageBitmap(image);
                } else {
                    Log.i("Adapter", "Image does not exist");
                }
            } catch (Exception e) {
                Log.i("Adapter", e.toString());
            }
        }
    }
}
