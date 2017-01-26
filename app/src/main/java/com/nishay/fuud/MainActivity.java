package com.nishay.fuud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nishay.fuud.sql.SQLHelper;
import com.nishay.fuud.swipe.FlingCardListener;
import com.nishay.fuud.swipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements FlingCardListener.ActionDownInterface,NavigationView.OnNavigationItemSelectedListener {

    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ArrayList<Data> al;
    private SwipeFlingAdapterView flingContainer;
    private SQLHelper sql;
    private Typeface customFont;
    private final ArrayList<View> mMenuItems = new ArrayList<>(1);

    private final String LOG_TAG = this.getClass().getSimpleName();

    public static void removeBackground() {
        myAppAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**setup db **/
        sql = new SQLHelper(this);

        customFont = Typeface.createFromAsset(getAssets(), "fonts/BrookeS8.ttf");

        /** setup drawer **/
        setupDrawer();

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        al = new ArrayList<>();
        al.add(sql.getRandomRow());
        al.add(sql.getRandomRow());
        al.add(sql.getRandomRow());

        myAppAdapter = new MyAppAdapter(al, MainActivity.this);
        flingContainer.setAdapter(myAppAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {

            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                al.remove(0);
                al.add(sql.getRandomRow());
                myAppAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRightCardExit(Object dataObject) {

                Data d = al.remove(0);
                al.add(sql.getRandomRow());
                myAppAdapter.notifyDataSetChanged();
                sql.saveRecipe(d);
                Toast.makeText(getApplicationContext(), "Saved " + d.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                View view = flingContainer.getSelectedView();
                if(view != null) {
                    //can be null if swipe too fast
                    view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                }
                else {
                    Log.w(LOG_TAG,"view null, try again");
                }
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                RecipeDialogFragment fragment = new RecipeDialogFragment();
                Bundle b = new Bundle();
                b.putString("url", al.get(0).getLink());
                b.putString("name", al.get(0).getName());
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "Recipe");
            }
        });

        ImageView yesButton = (ImageView) findViewById(R.id.fab_yes);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectRight();
            }
        });

        ImageView noButton = (ImageView) findViewById(R.id.fab_no);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flingContainer.getTopCardListener().selectLeft();
            }
        });

        //setup first time launch
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = preferences.getBoolean("first time", true);
        if(firstLaunch) {
            //setup tutorial
            MainTutorialDialogFragment fragment = new MainTutorialDialogFragment();
            fragment.show(getSupportFragmentManager(), "Help");

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("first time", false);
            editor.apply();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Data current) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, current.getName() + "\n" + current.getLink());
        startActivity(Intent.createChooser(shareIntent, "share"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_item_share) {
            setShareIntent(al.get(0));
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onActionDownPerform() {
        Log.e("action", "bingo");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sql.close();
    }

    private void setupDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        title.setTypeface(customFont);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //set font for navview
        final Menu m = navigationView.getMenu();
        navigationView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remember to remove the installed OnGlobalLayoutListener
                navigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Loop through and find each MenuItem View
                for (int i = 0; i < m.size(); i++) {
                    final MenuItem item = m.getItem(i);
                    navigationView.findViewsWithText(mMenuItems, item.getTitle(), View.FIND_VIEWS_WITH_TEXT);
                }
                // Loop through each MenuItem View and apply your custom Typeface
                for (final View menuItem : mMenuItems) {
                    ((TextView) menuItem).setTypeface(customFont, Typeface.BOLD);
                    ((TextView) menuItem).setTextSize(22);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        int id = item.getItemId();
        if(id == R.id.main_menu_back) {
            startActivity(new Intent(this, SavedRecipesActivity.class));
            return true;
        }
        if(id == R.id.menu_info) {
            //setup tutorial
            MainTutorialDialogFragment fragment = new MainTutorialDialogFragment();
            fragment.show(getSupportFragmentManager(), "Help");
        }
        return false;
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public ImageView cardImage;


    }

    public class MyAppAdapter extends BaseAdapter {


        public List<Data> parkingList;
        public Context context;

        private MyAppAdapter(List<Data> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            final ProgressBar progressBar;

            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.bookText);
                viewHolder.DataText.setTypeface(customFont);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText(parkingList.get(position).getName() + "");
            progressBar = (ProgressBar) rowView.findViewById(R.id.progress);
            progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

            Glide.with(MainActivity.this)
                    .load(parkingList.get(position).getImagePath())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(viewHolder.cardImage);

            return rowView;
        }
    }
}
