package com.nishay.fuud;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeDialogFragment extends DialogFragment {

    private final String LOG_TAG = this.getClass().getSimpleName();
    private FetchRecipe fetch;
    private Typeface customFont;
    private ProgressBar progressBar;


    public RecipeDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.8f;

        window.setAttributes(windowParams);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/BrookeS8.ttf");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_recipe_dialog, null);
        builder.setView(view);
        final Bundle bundle = getArguments();

        TextView title = ((TextView)view.findViewById(R.id.dialog_title));
        title.setText(bundle.getString("name"));
        title.setTypeface(customFont);

        TextView linkText = (TextView) (view.findViewById(R.id.dialog_link));
        linkText.setTypeface(customFont);

        view.findViewById(R.id.dialog_link_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = bundle.getString("url");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                getDialog().cancel();
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.dialog_progress);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        AlertDialog dialog = builder.create();


        String link = bundle.getString("url");
        fetch = new FetchRecipe();
        fetch.execute(link);


        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fetch.cancel(true);
    }

    public ArrayList<String> parse(String domain, String html) {
        ArrayList<String> ingredients = new ArrayList<>();
        ArrayList<String> instructions = new ArrayList<>();
        ArrayList<String> toPrint = new ArrayList<>();
        Pattern p = Pattern.compile("");

        if(domain.equals("allrecipes.com")) {
            p = Pattern.compile("itemprop=\"ingredients\">(.*?)</span>", Pattern.DOTALL);
            Matcher m = p.matcher(html);
            while(m.find()) {
                ingredients.add(m.group(1));
            }
            toPrint.add(tohtmlList(ingredients, "INGREDIENTS"));

            p = Pattern.compile("<span class=\"recipe-directions__list--item\">(.*?)</span></li>", Pattern.DOTALL);
            m = p.matcher(html);
            while(m.find()) {
                instructions.add(m.group(1));
            }
            toPrint.add(tohtmlList(instructions, "INSTRUCTIONS"));
        }

        else if(domain.equals("www.epicurious.com")) {
            p = Pattern.compile("class=\"ingredient\" itemprop=\"ingredients\">(.*?)</li>", Pattern.DOTALL);
            Matcher m = p.matcher(html);
            while(m.find()) {
                ingredients.add(m.group(1));
            }
            toPrint.add(tohtmlList(ingredients, "INGREDIENTS"));

            p = Pattern.compile("<li class=\"preparation-step\">(.*?)</li>", Pattern.DOTALL);
            m = p.matcher(html);
            while(m.find()) {
                instructions.add(m.group(1));
            }
            toPrint.add(tohtmlList(instructions, "INSTRUCTIONS"));
        }

        else if(domain.equals("www.food.com")) {
            p = Pattern.compile("<li data-ingredient=.*?><span>(.*?)</span> (.*?)</li>", Pattern.DOTALL);
            Matcher m = p.matcher(html);
            while(m.find()) {
                String ingredient = m.group(2);
                //may need to be further stripped
                if(ingredient.contains("<a href")) {
                    Pattern p2 = Pattern.compile("(.*)(  <a.*?>(.*?)</a>)");
                    Matcher m2 = p2.matcher(ingredient);
                    if(m2.find()) {
                        ingredient = m2.group(1) + " " + m2.group(3);
                    }
                }

                ingredient = m.group(1) + " " + ingredient;

                ingredients.add(ingredient);
            }
            toPrint.add(tohtmlList(ingredients, "INGREDIENTS"));

            //fixed food.com instructions regex
            p = Pattern.compile("<h3>Directions[\\W\\S]*?(<li>[\\W\\S]*</li>)[\\W\\S]*?<div class=\"recipe-tools\">", Pattern.DOTALL);
            m = p.matcher(html);
            while(m.find()) {
                //needs further regex
                Pattern p2 = Pattern.compile("<li>(.*?)</li>");
                Matcher m2 = p2.matcher(m.group(1));
                while(m2.find()) {
                    instructions.add(m2.group(1));
                }
            }

        }
            toPrint.add(tohtmlList(instructions, "INSTRUCTIONS"));


        return toPrint;
    }

    public static String tohtmlList(ArrayList<String> list, String header) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h3>" + header + "</h3>");
        for(String s : list) {
            html.append("<div style=\"margin-left:50px;\">");
            html.append("&#xb7; " + s);
            html.append("</div>");
        }

        html.append("</body></html>");

        return html.toString();
    }


    class FetchRecipe extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if(isCancelled()) {return null;}

            //do a uri connection on it, parse the html and send it to postExecute
            String html = null;
            try {
                html = new APIConnection().getResult(strings[0]);
            }
            catch (InterruptedIOException e) {
                Log.w(LOG_TAG, "thread interrupted");
            }

            if(isCancelled()) {return null;}

            return html;
        }

        protected void onPostExecute(String html) {
            if(isCancelled()) {return;}
            if(html == null) {return;}

            Dialog dialog = getDialog();

            if(html.equals("cannot connect")) {
                ((TextView)dialog.findViewById(R.id.ingredients)).setText(getString(R.string.error_cannot_connect));
                ((TextView)dialog.findViewById(R.id.ingredients)).setTypeface(customFont);
                progressBar.setVisibility(View.INVISIBLE);

                return;
            }

            String domain = getArguments().getString("url").split("/")[2];

            //pass to parser with domain, so it knows how to parse
            ArrayList<String> toPrint = parse(domain, html);

            if(isCancelled()) {return;}
            if(toPrint.size()!=2) {return;}

            progressBar.setVisibility(View.INVISIBLE);

            ((TextView)dialog.findViewById(R.id.ingredients)).setText(Html.fromHtml(toPrint.get(0)));
            ((TextView)dialog.findViewById(R.id.instructions)).setText(Html.fromHtml(toPrint.get(1)));

            ((TextView)dialog.findViewById(R.id.ingredients)).setTypeface(customFont);
            ((TextView)dialog.findViewById(R.id.instructions)).setTypeface(customFont);

        }
    }
}
