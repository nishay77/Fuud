package com.nishay.fuud;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by Nishay on 9/18/2016.
 */
public class SavedRecipesOptionsDialogFragment extends DialogFragment {

    private SavedRecipesOptionListener listener;
    private Typeface customFont;

    public interface SavedRecipesOptionListener {
        void onClickDelete(SavedRecipesOptionsDialogFragment fragment, Bundle bundle);
        void onClickShare(SavedRecipesOptionsDialogFragment fragment, Bundle bundle);
        void onClickViewRecipe(SavedRecipesOptionsDialogFragment fragment, Bundle bundle);
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
        windowParams.dimAmount = 0.9f;

        window.setAttributes(windowParams);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Data data = (Data) getArguments().getSerializable("data");
        customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/BrookeS8.ttf");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.saved_list_options_dialog, null);

        ImageView mainImage = (ImageView) view.findViewById(R.id.saved_list_options_picture);
        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.saved_list_options_progress);
        Glide.with(getActivity())
                .load(data.getImagePath())
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
                .into(mainImage);
        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickViewRecipe(SavedRecipesOptionsDialogFragment.this, getArguments());
            }
        });

        TextView title = (TextView)view.findViewById(R.id.saved_list_options_title);
        title.setText(data.getName());
        title.setTypeface(customFont);

        TextView share = (TextView)view.findViewById(R.id.saved_list_options_share);
        share.setTypeface(customFont);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickShare(SavedRecipesOptionsDialogFragment.this, getArguments());
            }
        });

        TextView viewRecipe = (TextView)view.findViewById(R.id.saved_list_options_view);
        viewRecipe.setTypeface(customFont);
        viewRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickViewRecipe(SavedRecipesOptionsDialogFragment.this, getArguments());
            }
        });

        TextView delete = (TextView)view.findViewById(R.id.saved_list_options_delete);
        delete.setTypeface(customFont);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((TextView) view).getText().equals("Delete")) {
                    //show confirm
                    ((TextView) view).setText("YOU SURE??");
                }
                else {
                    listener.onClickDelete(SavedRecipesOptionsDialogFragment.this, getArguments());
                }
            }
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SavedRecipesOptionListener) {
            listener = (SavedRecipesOptionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement SavedRecipesOptionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
