package avidos.autok.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import avidos.autok.R;

/**
 * Created by Alan on 10/20/2016.
 */

public class SignInDialog extends DialogFragment {
    private static final String TAG = "param1";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.dialog_signin, null);

        final EditText editTextUsername = (EditText) view.findViewById(R.id.username_dialog);
        final EditText editTextPassword = (EditText) view.findViewById(R.id.password_dialog);

        editTextUsername.setText(user.getEmail());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.action_sign_in_short, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // Get auth credentials from the user for re-authentication. The example below shows
                        // email and password credentials but there are multiple possible providers,
                        // such as GoogleAuthProvider or FacebookAuthProvider.
                        final AuthCredential credential = EmailAuthProvider
                                .getCredential(editTextUsername.getText().toString(), editTextPassword.getText().toString());
                        final String password = editTextPassword.getText().toString();
                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Log.d(TAG, "User re-authenticated.");
                                        }
                                        try {
                                            throw task.getException();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SignInDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}