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
import android.widget.TextView;

/**
 * Created by Nishay on 9/20/2016.
 */

public class SavedRecipesDeleteDialogFragment extends DialogFragment {

    private SavedRecipesDeleteDialogFragment.SavedRecipesDeleteListener listener;
    private Typeface customFont;

    public interface SavedRecipesDeleteListener {
        void onClickDeleteAllYes();
    }

    public SavedRecipesDeleteDialogFragment() {
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

        customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/BrookeS8.ttf");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.saved_list_delete_dialog, null);
        TextView main = (TextView)view.findViewById(R.id.saved_list_delete_text);
        main.setTextSize(32);
        main.setTypeface(customFont);
        TextView yes = (TextView)view.findViewById(R.id.saved_list_delete_yes);
        yes.setTextSize(18);
        yes.setTypeface(customFont);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
                listener.onClickDeleteAllYes();
            }
        });

        TextView no = (TextView)view.findViewById(R.id.saved_list_delete_no);
        no.setTextSize(30);
        no.setTypeface(customFont);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });



        builder.setView(view);

        AlertDialog dialog = builder.create();

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SavedRecipesDeleteDialogFragment.SavedRecipesDeleteListener) {
            listener = (SavedRecipesDeleteDialogFragment.SavedRecipesDeleteListener) context;
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
