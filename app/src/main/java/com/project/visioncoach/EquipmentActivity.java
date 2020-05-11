package com.project.visioncoach;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.audiofx.DynamicsProcessing;
import android.os.Bundle;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;

import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.sql.Timestamp;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Ref;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class EquipmentActivity extends AppCompatActivity {

    private static final String TAG = "EquipmentActivity";
    String last_scanned_equipment = "Dumbbell";

    // presets for rgb conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    // options for model interpreter
    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    // tflite graph
    private Interpreter tflite;
    // holds all the possible labels for model
    private List<String> labelList;
    // holds the selected image data as bytes
    private ByteBuffer imgData = null;
    // holds the probabilities of each label for non-quantized graphs
    private float[][] labelProbArray = null;
    // array that holds the labels with the highest probabilities
    private String[] topLables = null;
    // array that holds the highest probabilities
    private String[] topConfidence = null;
    // selected classifier information received from extras
    private String chosen;

    // input image dimensions for the Inception Model
    private int DIM_IMG_SIZE_X = 224;
    private int DIM_IMG_SIZE_Y = 224;
    private int DIM_PIXEL_SIZE = 3;


    // int array to hold image data
    private int[] intValues;

    String email = "";
    String equipment_name;

    // activity elements
    private ImageView selected_image;
    //displays top label as text
    private TextView equipment_found;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // priority queue that will hold the top results from the CNN
    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // get all selected classifier data from classifiers
        super.onCreate(savedInstanceState);

        chosen = (String) getIntent().getStringExtra("chosen");

        email = (String) getIntent().getStringExtra("email");

        getExtras(savedInstanceState);

        if(last_scanned_equipment == null){

            // initialize array that holds image data
            intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];


            //initilize graph and labels
            try{
                tflite = new Interpreter(loadModelFile(), tfliteOptions);
                labelList = loadLabelList();
            } catch (Exception ex){
                ex.printStackTrace();
            }

            // initialize byte array.
            imgData = ByteBuffer.allocateDirect(4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE);

            imgData.order(ByteOrder.nativeOrder());

            // initialize probabilities array.
            labelProbArray = new float[1][labelList.size()];


            setContentView(R.layout.activity_equipment);

            // initialize imageView that displays selected image to the user
            selected_image = (ImageView) findViewById(R.id.selected_image);
            // displays the exercise found

            // initialize array to hold top labels
            topLables = new String[RESULTS_TO_SHOW];
            // initialize array to hold top probabilities
            topConfidence = new String[RESULTS_TO_SHOW];

            // get image from previous activity to show in the imageView
            Uri uri = (Uri)getIntent().getParcelableExtra("resID_uri");
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                selected_image.setImageBitmap(bitmap);
                // not sure why this happens, but without this the image appears on its side
                selected_image.setRotation(selected_image.getRotation() + 90);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // get current bitmap from imageView
            Bitmap bitmap_orig = ((BitmapDrawable)selected_image.getDrawable()).getBitmap();
            // resize the bitmap to the required input size to the CNN
            Bitmap bitmap = getResizedBitmap(bitmap_orig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y);
            // convert bitmap to byte array
            convertBitmapToByteBuffer(bitmap);
            // pass byte data to the graph
            tflite.run(imgData, labelProbArray);

            // display the equipment name
            printTopKLabels();

        }
        else {
            setContentView(R.layout.activity_equipment);
            topLables = new String[RESULTS_TO_SHOW];
            topLables[2] = last_scanned_equipment;
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitle(topLables[2] + " Muscle Groups");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setSupportActionBar(toolbar);

        }

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_exercises:
                        Intent intent1 = new Intent(EquipmentActivity.this, MuscleGroupActivity.class);
                        intent1.putExtra("email", email);
                        startActivity(intent1);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_home:
                        Intent intent2 = new Intent(EquipmentActivity.this, HomeActivity.class);
                        intent2.putExtra("email", email);
                        startActivity(intent2);
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.nav_favourites:
//                        startActivity(new Intent(getApplicationContext(), FavouriteActivity.class));
                        Intent intent3 = new Intent(EquipmentActivity.this, FavouriteActivity.class);
                        intent3.putExtra("email", email);
                        startActivity(intent3);
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        // display the corresponding exercises
        getExercises();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        finish();
        return super.onOptionsItemSelected(item);
    }

    // loads tflite grapg from file
    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(chosen);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // converts bitmap to byte array which is passed in the tflite graph
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);

            }
        }
    }

    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    // print the top labels and respective confidences
    private void printTopKLabels() {
        // add all results to priority queue
        for (int i = 0; i < labelList.size(); ++i) {
            sortedLabels.add(new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArray[0][i]));

            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();
            }
        }

        // get top results from priority queue
        final int size = sortedLabels.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = sortedLabels.poll();
            //topLables [0],[1],[2], where [2] is the highest
            topLables[i] = label.getKey();
            //topConfidence [0],[1],[2], where [2] is the highest
            topConfidence[i] = String.format("%.0f%%",label.getValue()*100);
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(topLables[2] + " Muscle Groups");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setSupportActionBar(toolbar);

    }


    // resizes bitmap to given dimensions
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public void getExercises() {
        // Access a Cloud Firestore instance from your Activity


        String collectionPath = "";
        if(topLables[2].equals("Dumbbell")){
            collectionPath = ("Equipment/dumbbell/Muscle-Groups");

            String exercise = "Dumbbell";

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Name", exercise);
            attributes.put("Timestamp", System.currentTimeMillis());

            db.collection("Users").document(email).collection("Last_Scanned").document("Equipment")
                    .set(attributes)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

        else if(topLables[2].equals("Barbell")){
            collectionPath = ("Equipment/barbell/Muscle-Groups");

            String exercise = "Barbell";

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Name", exercise);
            attributes.put("Timestamp", System.currentTimeMillis());

            db.collection("Users").document(email).collection("Last_Scanned").document("Equipment")
                    .set(attributes)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

        }
        else if(topLables[2].equals("Leg Press")){
            collectionPath = ("Equipment/leg press/Muscle-Groups");

            String exercise = "Leg Press";

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Name", exercise);
            attributes.put("Timestamp", System.currentTimeMillis());

            db.collection("Users").document(email).collection("Last_Scanned").document("Equipment")
                    .set(attributes)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

        else if(topLables[2].equals("Bench Press")){
            collectionPath = ("Equipment/bench press/Muscle-Groups");

            String exercise = "Bench Press";

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("Name", exercise);
            attributes.put("Timestamp", System.currentTimeMillis());

            db.collection("Users").document(email).collection("Last_Scanned").document("Equipment")
                    .set(attributes)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
        }

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.constraintlayout);
                            ConstraintSet set = new ConstraintSet();

                            //Button programmatically
                            int buttonCounter = 0; //check for toStartOf constraint
                            int buttonMargin = 371;
                            

                            int imageCounter = 0;

//                            TextView Programmatically
//                            int textViewCounter = 0;
//                            int textViewMargin = 445;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("Name");

//                                TextView
//                                    if(textViewCounter == 0) {
//                                        TextView view = new TextView(getApplicationContext());
//                                        view.setId(View.generateViewId());
//                                        view.setText(name);
//                                        view.setTextColor(Color.parseColor("#FFFFFF"));
//                                        layout.addView(view, 0);
//                                        set.clone(layout);
//                                        set.connect(view.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, 230);
//                                        set.applyTo(layout);
//                                        textViewCounter = 1;
//                                    }
//
//                                    else{
//                                        TextView view = new TextView(getApplicationContext());
//                                        view.setId(View.generateViewId());
//                                        view.setText(name);
//                                        view.setTextColor(Color.parseColor("#FFFFFF"));
//                                        layout.addView(view, 0);
//                                        set.clone(layout);
//                                        set.connect(view.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP, textViewMargin);
//                                        set.applyTo(layout);
//                                        textViewMargin += 215;
//                                    }


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
                                            //int id = v.getId();
                                            //System.out.println("++> " + id);
                                            Intent intent = new Intent(EquipmentActivity.this, ExerciseListActivity.class);
                                            intent.putExtra("muscle-group", buttonText);
                                            intent.putExtra("equipment", topLables[2]);
                                            intent.putExtra("email", email);
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
                                            Intent intent = new Intent(EquipmentActivity.this, ExerciseListActivity.class);
                                            intent.putExtra("muscle-group", buttonText);
                                            intent.putExtra("equipment", topLables[2]);
                                            intent.putExtra("email", email);
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
                last_scanned_equipment = null;

            } else {
                last_scanned_equipment = extras.getString("last_scanned");

            }
        } else {
            last_scanned_equipment = (String)savedInstanceState.getSerializable("last_scanned");

        }
    }



}