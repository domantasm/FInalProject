package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MGDisplayExerciseActivity extends AppCompatActivity {

    String muscle_group;
    String equipment;
    String exercise;

    String name;

    String email = "";

    private static final String TAG = "DisplayExerciseActivity";

    TextView exerciseView;
    TextView textView8;
    TextView textView9;
    TextView textView10;
    TextView instructions;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mgdisplay_exercise);
        getExtras(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(exercise);
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
                        Intent intent1 = new Intent(MGDisplayExerciseActivity.this, MuscleGroupActivity.class);
                        intent1.putExtra("email", email);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_home:
                        Intent intent2 = new Intent(MGDisplayExerciseActivity.this, HomeActivity.class);
                        intent2.putExtra("email", email);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_favourites:
//                        startActivity(new Intent(getApplicationContext(), FavouriteActivity.class));
                        Intent intent3 = new Intent(MGDisplayExerciseActivity.this, FavouriteActivity.class);
                        intent3.putExtra("email", email);
                        startActivity(intent3);
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });


        displayExercise();
        getImage();
        checkFavourite();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        finish();
        return super.onOptionsItemSelected(item);
    }

    public void displayExercise(){
        String formatMG = muscle_group.toLowerCase();
        String formatExercise = exercise.toLowerCase();


        String collectionPath = ("Muscle-Groups/" + formatMG + "/Exercises/" + formatExercise);
        db.document(collectionPath)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){

                            String difficulty = documentSnapshot.getString("Difficulty");
                            String eType = documentSnapshot.getString("Exercise_Type");
                            String text = documentSnapshot.getString("Instructions");

                            ArrayList<String> list = splitInstructions(text);

                            textView8 = (TextView) findViewById(R.id.textView8);
                            textView8.setText(muscle_group);
                            textView9 = (TextView) findViewById(R.id.textView9);
                            textView9.setText(difficulty);
                            textView10 = (TextView) findViewById(R.id.textView10);
                            textView10.setText(eType);

                            instructions = (TextView) findViewById(R.id.instructions);
                            for(int i = 0; i < list.size(); i++){
                                if(i == 0){
                                    instructions.setText(list.get(i) + "\n\n");
                                }
                                else{
                                    instructions.append(list.get(i) + "\n\n");
                                }
                            }
                        }
                        else{
                            Toast.makeText(MGDisplayExerciseActivity.this, "Document Does Not Exist", Toast.LENGTH_LONG);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MGDisplayExerciseActivity.this, "Error!", Toast.LENGTH_SHORT);

                    }
                });
    }

    public ArrayList<String> splitInstructions(String text){
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        Matcher reMatcher = re.matcher(text);
        ArrayList<String> formatted = new ArrayList<String>();
        int i = 0;
        while (reMatcher.find()) {
            formatted.add(reMatcher.group());
        }

        return formatted;
    }


    public void getImage(){
        StorageReference storageRef = storage.getReference();
        String exerciseLower = exercise.replace(" ", "_");
        String child = "exercises/" + exerciseLower.toLowerCase() + "_1.jpg";
        storageRef.child(child).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView imageView = findViewById(R.id.imageViewLeft);
                Glide.with(MGDisplayExerciseActivity.this)
                        .load(uri)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error");
            }
        });

        String exerciseLower2 = exercise.replace(" ", "_");
        String child2 = "exercises/" + exerciseLower.toLowerCase() + "_2.jpg";
        storageRef.child(child2).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                ImageView imageView = findViewById(R.id.imageViewRight);
                Glide.with(MGDisplayExerciseActivity.this)
                        .load(uri)
                        .into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error");
            }
        });

    }

    public void checkFavourite() {
        String formatExercise = exercise.toLowerCase();
        String collectionPath = ("Users/" + email + "/Exercises/" + formatExercise);
        System.out.println("++> " + collectionPath);
        db.document(collectionPath)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //If Favourited
                        if (documentSnapshot.exists()){
                            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mgexerciselayout);
                            ConstraintSet set = new ConstraintSet();
                            Button heartButton = new Button(getApplicationContext());
                            heartButton.setId(View.generateViewId());
                            heartButton.setLayoutParams(new ConstraintLayout.LayoutParams(120, 120));
                            layout.addView(heartButton, 0);
                            heartButton.setBackgroundResource(R.drawable.heart);
                            //button.setGravity(20);
                            set.clone(layout);
                            set.connect(heartButton.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 25);
                            set.connect(heartButton.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 50);
                            set.applyTo(layout);

                            heartButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String lowerExercise = exercise.toLowerCase();
                                    db.collection("Users").document(email).collection("Exercises").document(lowerExercise)
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
                        //If Not Favourited
                        else{
                            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.mgexerciselayout);
                            ConstraintSet set = new ConstraintSet();
                            Button heartButton = new Button(getApplicationContext());
                            heartButton.setId(View.generateViewId());
                            heartButton.setLayoutParams(new ConstraintLayout.LayoutParams(120, 120));
                            layout.addView(heartButton, 0);
                            heartButton.setBackgroundResource(R.drawable.whiteheart);
                            set.clone(layout);
                            set.connect(heartButton.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 25);
                            set.connect(heartButton.getId(), ConstraintSet.RIGHT, layout.getId(), ConstraintSet.RIGHT, 50);
                            set.applyTo(layout);

                            heartButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String lowerExercise = exercise.toLowerCase();

                                    Map<String, Object> attributes = new HashMap<>();
                                    System.out.println("++> EXERCISE NAME" + exercise);
                                    attributes.put("Name", exercise);

                                    db.collection("Users").document(email).collection("Exercises").document(lowerExercise)
                                            .set(attributes)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                                    finish();
                                                    startActivity(getIntent());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                }
                                            });
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MGDisplayExerciseActivity.this, "++> Error!", Toast.LENGTH_SHORT);

                    }
                });
    }



    public void getExtras(Bundle savedInstanceState){
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                muscle_group = null;
                exercise = null;
                email = null;
            } else {
                muscle_group = extras.getString("muscle-group");
                exercise = extras.getString("exercise");
                email = extras.getString("email");
            }
        } else {
            muscle_group = (String)savedInstanceState.getSerializable("muscle-group");
            exercise = (String)savedInstanceState.getSerializable("exercise");
            email = (String)savedInstanceState.getSerializable("email");
        }


    }
}
