package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class ChangePasswordActivity extends AppCompatActivity {

    TextView current_password;
    TextView new_password;
    TextView confirm_password;
    TextView errorText;
    Button check_btn;

    String email;
    String current_pass;
    String confirm_pass;
    String new_pass;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final String TAG = "ChangePasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getExtras(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        current_password = (TextView) findViewById(R.id.current_password);
        new_password = (TextView) findViewById(R.id.new_password);
        confirm_password = (TextView) findViewById(R.id.confirm_password);
        errorText = (TextView) findViewById(R.id.errorText);
        check_btn = (Button) findViewById(R.id.check_btn);

                check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void changePassword(){

        current_pass = current_password.getText().toString();
        new_pass = new_password.getText().toString();
        confirm_pass = confirm_password.getText().toString();


        AuthCredential credential = EmailAuthProvider.getCredential(email, current_pass);


        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if(new_pass.equals(confirm_pass)) {
                                user.updatePassword(new_pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "++> Password updated");
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intToMain = new Intent(ChangePasswordActivity.this, MainActivity.class);
                                            startActivity(intToMain);

                                        } else {
                                            Log.d(TAG, "++> Error password not updated");
                                            errorText.setText("Password is too weak");
                                        }
                                    }
                                });
                            }
                            else {
                                errorText.setText("Passwords do not match, try again!");
                            }
                        } else {
                            Log.d(TAG, "++> Error auth failed");
                            errorText.setText("Incorrect password, try again!");
                        }

                    }
                });

    }

    public void getExtras(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                email = null;

            } else {
                email = extras.getString("email");

            }
        } else {
            email = (String)savedInstanceState.getSerializable("email");

        }
    }
}
