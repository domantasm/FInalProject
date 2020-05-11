package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MGExerciseListActivity extends AppCompatActivity {

    TextView mg_found;

    String muscle_group;

    String email = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ExerciseListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mgexercise_list);

        getExtras(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Exercises for " + muscle_group);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setSupportActionBar(toolbar);


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_exercises);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_exercises:
                        Intent intent1 = new Intent(MGExerciseListActivity.this, MuscleGroupActivity.class);
                        intent1.putExtra("email", email);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_home:
                        Intent intent2 = new Intent(MGExerciseListActivity.this, HomeActivity.class);
                        intent2.putExtra("email", email);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_favourites:
//                        startActivity(new Intent(getApplicationContext(), FavouriteActivity.class));
                        Intent intent3 = new Intent(MGExerciseListActivity.this, FavouriteActivity.class);
                        intent3.putExtra("email", email);
                        startActivity(intent3);
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });


        getExercises();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void getExercises() {
        // Access a Cloud Firestore instance from your Activity


        String formatMG = muscle_group.toLowerCase();

        String collectionPath = ("Muscle-Groups/" + formatMG + "/Exercises");

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mglayout2);
                            ConstraintSet set = new ConstraintSet();

                            //Button programmatically
                            int buttonCounter = 0; //check for toStartOf constraint
                            int buttonMargin = 371;


                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                String name = document.getString("Name");

                                //Button
                                if(buttonCounter == 0) {
                                    Button button = new Button(getApplicationContext());
                                    button.setId(View.generateViewId());
                                    button.setLayoutParams(new ConstraintLayout.LayoutParams(1040, 200));
                                    layout.addView(button, 0);
                                    button.setText(name);
                                    button.setTextColor(Color.parseColor("#FFFFFF"));
                                    button.setBackgroundResource(R.drawable.roundmain);
                                    //button.setGravity(20);
                                    set.clone(layout);
                                    set.connect(button.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 156);
                                    set.connect(button.getId(), ConstraintSet.LEFT, layout.getId(), ConstraintSet.LEFT, 12);
                                    set.applyTo(layout);
                                    buttonCounter = 1;

                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Button b = (Button)v;
                                            String buttonText = b.getText().toString();
                                            Intent intent = new Intent(MGExerciseListActivity.this, MGDisplayExerciseActivity.class);
                                            intent.putExtra("muscle-group", muscle_group);
                                            intent.putExtra("exercise", buttonText);
                                            intent.putExtra(("email"), email);
                                            startActivity(intent);
                                        }
                                    });

                                }

                                else {
                                    Button button = new Button(getApplicationContext());
                                    button.setId(View.generateViewId());
                                    button.setLayoutParams(new ConstraintLayout.LayoutParams(1040, 200));
                                    layout.addView(button, 0);
                                    button.setText(name);
                                    button.setTextColor(Color.parseColor("#FFFFFF"));
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
                                            Intent intent = new Intent(MGExerciseListActivity.this, MGDisplayExerciseActivity.class);
                                            intent.putExtra("muscle-group", muscle_group);
                                            intent.putExtra("exercise", buttonText);
                                            intent.putExtra(("email"), email);
                                            startActivity(intent);
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
                muscle_group = null;
                email = null;
            } else {
                muscle_group = extras.getString("muscle-group");
                email = extras.getString("email");
            }
        } else {
            muscle_group = (String)savedInstanceState.getSerializable("muscle-group");
            email = (String) savedInstanceState.getSerializable("email");
        }

    }
}
