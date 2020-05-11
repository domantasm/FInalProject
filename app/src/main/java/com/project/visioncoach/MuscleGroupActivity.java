package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MuscleGroupActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "MuscleGroupActivity";

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    String email = "";

    private Button abs_btn;
    private Button biceps_btn;
    private Button calves_btn;
    private Button chest_btn;
    private Button hamstrings_btn;
    private Button lowerback_btn;
    private Button quadriceps_btn;
    private Button shoulders_btn;
    private Button upperback_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_muscle);

        getExtras(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_exercises);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_exercises:
                        Intent intent1 = new Intent(MuscleGroupActivity.this, MuscleGroupActivity.class);
                        intent1.putExtra("email", email);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_home:
                        Intent intent2 = new Intent(MuscleGroupActivity.this, HomeActivity.class);
                        intent2.putExtra("email", email);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_favourites:
//                        startActivity(new Intent(getApplicationContext(), FavouriteActivity.class));
                        Intent intent3 = new Intent(MuscleGroupActivity.this, FavouriteActivity.class);
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
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        // handle arrow click here
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void getExercises(){
        abs_btn = (Button) findViewById(R.id.abs_btn);
        abs_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Abs");
            }
        });

        biceps_btn = (Button) findViewById(R.id.biceps_btn);
        biceps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Biceps");
            }
        });

        calves_btn = (Button) findViewById(R.id.calves_btn);
        calves_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Calves");
            }
        });

        chest_btn = (Button) findViewById(R.id.chest_btn);
        chest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Chest");
            }
        });

        hamstrings_btn = (Button) findViewById(R.id.hamstrings_btn);
        hamstrings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Hamstrings");
            }
        });

        lowerback_btn = (Button) findViewById(R.id.lowerback_btn);
        lowerback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Lower Back");
            }
        });

        quadriceps_btn = (Button) findViewById(R.id.quadriceps_btn);
        quadriceps_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Quadriceps");
            }
        });

        shoulders_btn = (Button) findViewById(R.id.shoulders_btn);
        shoulders_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Shoulders");
            }
        });

        upperback_btn = (Button) findViewById(R.id.upperback_btn);
        upperback_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openExerciseList("Upper Back");
            }
        });
    }

    public void openExerciseList(String muscle_group){
        Intent intent = new Intent(MuscleGroupActivity.this, MGExerciseListActivity.class);
        intent.putExtra("muscle-group", muscle_group);
        intent.putExtra("email", email);
        startActivity(intent);
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