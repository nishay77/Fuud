package com.nishay.fuud;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nishay.fuud.sql.SQLHelper;

import java.util.ArrayList;

/**
 * Created by Nishay on 9/6/2016.
 */
public class CustomListAdapter extends BaseAdapter {

    private ArrayList<Data> data;
    private Context context;
    private static LayoutInflater inflater;
    private Typeface customFont;
    private SQLHelper sql;

    public CustomListAdapter(Context context, ArrayList<Data> data, Typeface tf, SQLHelper sql) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.customFont = tf;
        this.sql = sql;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public class ViewHolder {
        private TextView title;
        private TextView url;
        private ImageView image;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;

        if(view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.saved_list_item, viewGroup, false);
            holder.title = (TextView) view.findViewById(R.id.saved_list_title);
            holder.url = (TextView) view.findViewById(R.id.saved_list_url);
            holder.image = (ImageView) view.findViewById(R.id.saved_list_image);

            view.setTag(holder);

        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        //view properly inflated, set text now
        final Data line = data.get(i);

        holder.title.setText(line.getName());
        holder.title.setTypeface(customFont);

        holder.url.setText(line.getLink().split("/")[2]);
        holder.url.setTypeface(customFont);

        Glide.with(context).load(line.getImagePath()).into(holder.image);

        return view;
    }
}
