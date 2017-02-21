package avidos.autok.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class ChangePasswordDialog extends DialogFragment {
    private static final String TAG = "param1";
    private View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        view = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText oldPassword = (EditText) view.findViewById(R.id.old_password);
        final EditText newPassword = (EditText) view.findViewById(R.id.new_password);
        final EditText newRepeatPassword = (EditText) view.findViewById(R.id.new_password_repeat);
        final TextInputLayout oldPasswordLayout = (TextInputLayout) view.findViewById(R.id.old_password_layout);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.action_update_password, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    ///
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ChangePasswordDialog.this.getDialog().cancel();
                    }
                });

        final AlertDialog mAlertDialog = builder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        // Get auth credentials from the user for re-authentication. The example below shows
                        // email and password credentials but there are multiple possible providers,
                        // such as GoogleAuthProvider or FacebookAuthProvider.
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(user.getEmail(), oldPassword.getText().toString());

                        if(!newPassword.getText().toString().equals(newRepeatPassword.getText().toString())) {
                            newRepeatPassword.setError("Las contraseñas no coinciden");
                            return;
                        }

                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "User re-authenticated.");
                                        if(!task.isSuccessful()) {
                                            oldPasswordLayout.setError("Contraseña actual incorrecta");
                                        } else {
                                            user.updatePassword(newPassword.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Contraseña actualizada", Toast.LENGTH_LONG).show();
                                                                mAlertDialog.dismiss();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
            }
        });

        return mAlertDialog;
    }
}