package com.guy_gueta.post_pc_6;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogSms extends AppCompatDialogFragment {
    private final static String DIALOG_TITLE = "please Enter Phone Number, leave empty if you wish to delete";
    private EditText inputPhoneNumber;
    private SmsDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SmsDialogListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_layout, null);
        inputPhoneNumber = view.findViewById(R.id.phone_number_input);
        builder.setView(view)
                .setTitle(DIALOG_TITLE)
                .setNegativeButton("abort", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}})
                .setPositiveButton("ok", numberConfirmation());
        return builder.create();
    }

    private DialogInterface.OnClickListener numberConfirmation() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String number = inputPhoneNumber.getText().toString();
                listener.saveNumber(number);
            }
        };
    }

    public interface SmsDialogListener {
        void saveNumber(String phoneNumber);
    }
}

