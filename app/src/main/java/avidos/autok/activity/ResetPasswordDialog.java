package avidos.autok.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import avidos.autok.R;

/**
 * Created by Alan on 10/20/2016.
 */

public class ResetPasswordDialog extends DialogFragment {
    private static final String TAG = "param1";
    private FirebaseAuth mAuth;
    private View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.dialog_signin, null);

        final EditText editTextUsername = (EditText) view.findViewById(R.id.username_dialog);

        mAuth = FirebaseAuth.getInstance();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.action_send_email, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(!TextUtils.isEmpty(editTextUsername.getText().toString())) {
                            mAuth.sendPasswordResetEmail(editTextUsername.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ResetPasswordDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}