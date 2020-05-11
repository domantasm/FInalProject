package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class FavouriteActivity extends AppCompatActivity {

    String email;

    int imageId;

    private FirebaseStorage storage = FirebaseStorage.getInstance();


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "FavouriteActivity";

    String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        getExtras(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getExercises();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.fav_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_favourites);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.nav_exercises:
                        Intent intent1 = new Intent(FavouriteActivity.this, MuscleGroupActivity.class);
                        intent1.putExtra("email", email);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_home:
                        Intent intent2 = new Intent(FavouriteActivity.this, HomeActivity.class);
                        intent2.putExtra("email", email);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_favourites:
                        Intent intent3 = new Intent(FavouriteActivity.this, FavouriteActivity.class);
                        intent3.putExtra("email", email);
                        startActivity(intent3);
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        finish();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.fav_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        return super.onOptionsItemSelected(item);
    }

    public void getExercises() {
        // Access a Cloud Firestore instance from your Activity

        String collectionPath = ("Users/" + email + "/Exercises");

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.favouritelayout);
                            ConstraintSet set = new ConstraintSet();

                            //Button programmatically
                            int buttonCounter = 0; //check for toStartOf constraint
                            int buttonMargin = 430;

                            int heartCounter = 0;
                            int heartMargin = 470;

                            int imageCounter = 0;
                            int imageMargin = 470;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                name = document.getString("Name");

                                //Button
                                if(buttonCounter == 0) {
                                    Button button = new Button(getApplicationContext());
                                    button.setId(View.generateViewId());
                                    button.setLayoutParams(new ConstraintLayout.LayoutParams(1050, 200));
                                    layout.addView(button, 0);
                                    button.setText(name);
                                    button.setTextColor(Color.parseColor("#FFFFFF"));
                                    button.setGravity(0);
                                    button.setPadding(175,75, 0, 0);
                                    button.setBackgroundResource(R.drawable.roundmain);
                                    //button.setGravity(20);
                                    set.clone(layout);
                                    set.connect(button.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 215);
                                    set.connect(button.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 12);
                                    set.applyTo(layout);
                                    buttonCounter = 1;

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Button b = (Button)v;
                                            String buttonText = b.getText().toString();
                                            Intent intent = new Intent(FavouriteActivity.this, FavouriteDisplayExercise.class);
                                            intent.putExtra("exercise", buttonText);
                                            intent.putExtra("email", email);
                                            startActivity(intent);
                                        }
                                    });

                                }

                                else {
                                    Button button = new Button(getApplicationContext());
                                    button.setId(View.generateViewId());
                                    button.setLayoutParams(new ConstraintLayout.LayoutParams(1050, 200));
                                    layout.addView(button, 0);
                                    button.setText(name);
                                    button.setTextColor(Color.parseColor("#FFFFFF"));
                                    button.setGravity(0);
                                    button.setPadding(175,75, 0, 0);
                                    button.setBackgroundResource(R.drawable.roundmain);
                                    set.clone(layout);
                                    set.connect(button.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, buttonMargin);
                                    set.connect(button.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 12);
                                    set.applyTo(layout);
                                    buttonMargin += 215;

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Button b = (Button)v;
                                            String buttonText = b.getText().toString();
                                            Intent intent = new Intent(FavouriteActivity.this, FavouriteDisplayExercise.class);
                                            intent.putExtra("exercise", buttonText);
                                            intent.putExtra("email", email);
                                            startActivity(intent);
                                        }
                                    });

                                }

                                //Heart
                                if(heartCounter == 0) {
                                    Button heartButton = new Button(getApplicationContext());
                                    heartButton.setId(View.generateViewId());
                                    heartButton.setLayoutParams(new ConstraintLayout.LayoutParams(120, 120));
                                    layout.addView(heartButton, 0);
                                    heartButton.setBackgroundResource(R.drawable.heart);
                                    heartButton.bringToFront();
                                    //button.setGravity(20);
                                    set.clone(layout);
                                    set.connect(heartButton.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 255);
                                    set.connect(heartButton.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 80);
                                    set.applyTo(layout);
                                    heartCounter = 1;

                                    heartButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
//                                            Button b = (Button)v;
                                            String formattedName = name.toLowerCase();
                                            db.collection("Users").document(email).collection("Exercises").document(formattedName)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                            finish();
                                                            startActivity(getIntent());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error deleting document", e);
                                                        }
                                                    });
                                        }
                                    });
                                }

                                else {
                                    Button heartButton = new Button(getApplicationContext());
                                    heartButton.setId(View.generateViewId());
                                    heartButton.setLayoutParams(new ConstraintLayout.LayoutParams(120, 120));
                                    layout.addView(heartButton, 0);
                                    heartButton.setBackgroundResource(R.drawable.heart);
                                    heartButton.bringToFront();
                                    set.clone(layout);
                                    set.connect(heartButton.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, heartMargin);
                                    set.connect(heartButton.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 80);
                                    set.applyTo(layout);
                                    heartMargin += 215;

                                    heartButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String formattedName = name.toLowerCase();
                                            db.collection("Users").document(email).collection("Exercises").document(formattedName)
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                            finish();
                                                            startActivity(getIntent());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w(TAG, "Error deleting document", e);
                                                        }
                                                    });

                                        }
                                    });
                                }


                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
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
