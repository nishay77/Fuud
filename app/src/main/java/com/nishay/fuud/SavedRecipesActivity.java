package com.nishay.fuud;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nishay.fuud.sql.SQLHelper;

import java.util.ArrayList;

public class SavedRecipesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SavedRecipesOptionsDialogFragment.SavedRecipesOptionListener,
        SavedRecipesDeleteDialogFragment.SavedRecipesDeleteListener {

    private CustomListAdapter adapter;
    private ListView listView;
    private ArrayList<Data> data;
    private SQLHelper sql;
    private Typeface customFont;
    private final ArrayList<View> mMenuItems = new ArrayList<>(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sql = new SQLHelper(this);
        setContentView(R.layout.activity_saved_recipes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customFont = Typeface.createFromAsset(getAssets(), "fonts/BrookeS8.ttf");

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

        data = sql.getSavedRecipes();

        listView = (ListView) findViewById(R.id.saved_recipes_listview);
        adapter = new CustomListAdapter(this, data, customFont, sql);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String url = data.get(i).getLink();
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                startActivity(browserIntent);
//                RecipeDialogFragment fragment = new RecipeDialogFragment();
//                Bundle b = new Bundle();
//                b.putString("url", data.get(i).getLink());
//                b.putString("name", data.get(i).getName());
//                fragment.setArguments(b);
//                fragment.show(getSupportFragmentManager(), "Recipe");
                SavedRecipesOptionsDialogFragment fragment = new SavedRecipesOptionsDialogFragment();
                Bundle b = new Bundle();
                b.putSerializable("data", data.get(i));
                b.putInt("index", i);
                fragment.setArguments(b);
                fragment.show(getSupportFragmentManager(), "SavedRecipeOptions");
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                return true;
            }
        });

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
    public void onStop() {
        super.onStop();
        sql.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.saved_recipes, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.saved_recipes_menu_back) {
            finish();
            return true;
        }

        if(id == R.id.saved_recipes_menu_clear) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            SavedRecipesDeleteDialogFragment fragment = new SavedRecipesDeleteDialogFragment();
            fragment.show(getSupportFragmentManager(), "delete all");
        }

        return false;
    }

    @Override
    public void onClickDelete(SavedRecipesOptionsDialogFragment fragment, Bundle bundle) {
        Data d = (Data) bundle.getSerializable("data");
        sql.deleteSaved(d.getName(), d.getLink());
        data.remove(bundle.getInt("index"));
        adapter.notifyDataSetChanged();
        fragment.getDialog().cancel();
    }

    @Override
    public void onClickShare(SavedRecipesOptionsDialogFragment fragment, Bundle bundle) {
        Data current = (Data) bundle.getSerializable("data");
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, current.getName() + "\n" + current.getLink());
        startActivity(Intent.createChooser(shareIntent, "share"));

    }

    @Override
    public void onClickViewRecipe(SavedRecipesOptionsDialogFragment oldFragment, Bundle bundle) {
        RecipeDialogFragment fragment = new RecipeDialogFragment();
        Data d = (Data) bundle.getSerializable("data");

        Bundle b = new Bundle();
        b.putString("url", d.getLink());
        b.putString("name", d.getName());
        fragment.setArguments(b);
        fragment.show(getSupportFragmentManager(), "Recipe");
    }

    @Override
    public void onClickDeleteAllYes() {
        sql.deleteSaved("", "");
        data.clear();
        adapter.notifyDataSetChanged();
    }
}
