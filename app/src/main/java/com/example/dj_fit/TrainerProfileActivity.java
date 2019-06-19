package com.example.dj_fit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import javax.annotation.Nullable;

public class TrainerProfileActivity extends BaseActivity {

    String TAG = "Trainer Profile Activity";
    ImageView profileImageView;
    TextView profileNameText, employerText, experienceText, aboutMeText;
    String imageName;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profileImageView = findViewById(R.id.profileImageView);
        profileNameText = findViewById(R.id.profileNameText);
        employerText = findViewById(R.id.employerText);
        experienceText = findViewById(R.id.experienceText);
        aboutMeText = findViewById(R.id.aboutMeText);
        imageName = null;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        checkIfTrainerProfileExists();

    }

    private void populateProfilePage(Map<String, Object> docData)
    {
        imageName = (String) docData.get("profilePic");
        String fullName = docData.get("first_name").toString() + " " + docData.get("last_name").toString();
        profileNameText.setText(fullName);
        employerText.setText(docData.get("employment").toString());
        experienceText.setText(docData.get("experience").toString());
        aboutMeText.setText(docData.get("aboutYou").toString());
        if(imageName == null)
        {
            System.out.println("Image is null");
        }
        else
        {
            System.out.println(imageName);
            downloadFile();
        }
    }

    private void checkIfTrainerProfileExists()
    {
        final long start = System.currentTimeMillis();
        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference docRef = mDatabase.collection("users").document(userID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null)
                {
                    Log.w(TAG, "Listen failed", e);
                }

                if (documentSnapshot != null && documentSnapshot.exists())
                {
                    long end = System.currentTimeMillis();
                    Log.d(TAG, "Current data: " + documentSnapshot.getData());
                    Log.d(TAG, "Logged at " + (end - start));
                    populateProfilePage(documentSnapshot.getData());
                }
                else
                {
                    Log.d (TAG, "Current data: null");
                    //splashImage.setVisibility(View.GONE);
                    //backgroundScroll.setVisibility(View.VISIBLE);
                    //backgroundText.setVisibility(View.VISIBLE);
                    //backgroundBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void downloadFile()
    {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imageName);

        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        imageRef.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Toast.makeText(TrainerProfileActivity.this, "Download success", Toast.LENGTH_SHORT).show();
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bmp);
                roundDrawable.setCircular(true);
                final float scale = TrainerProfileActivity.this.getResources().getDisplayMetrics().density;
                profileImageView.setMaxHeight((int) (120 * scale + 0.5f));
                profileImageView.setMaxWidth((int) (120 * scale + 0.5f));
                profileImageView.setImageDrawable(roundDrawable);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(TrainerProfileActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
                // Handle any errors
            }
        });
    }

}
