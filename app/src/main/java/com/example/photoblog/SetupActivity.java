package com.example.photoblog;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView imageViewId;
    private EditText userNameET;
    private ProgressBar setUpProgressBar;
    private Button createAccountBtn;
    private String currentUser;
    private Uri mainImageURI = null;
    private boolean isChanged = false;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser().getUid();


        imageViewId = findViewById(R.id.imageViewId);
        userNameET = findViewById(R.id.userNameET);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        setUpProgressBar = findViewById(R.id.setUpProgressBar);

        setUpProgressBar.setVisibility(View.VISIBLE);
        createAccountBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("Name");
                        String image = task.getResult().getString("Image");

                        mainImageURI = Uri.parse(image);
                        userNameET.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.image_icon);
                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(imageViewId);

                    }

                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Retrive Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
                setUpProgressBar.setVisibility(View.INVISIBLE);
                createAccountBtn.setEnabled(true);
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = userNameET.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {

                    setUpProgressBar.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        final StorageReference image_path = storageReference.child("profile_images").child(currentUser + ".jpg");

                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);

                                } else {
                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Image Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        storeFirestore(null, user_name);
                    }
                }
            }
        });

        imageViewId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        imagePicker();
                    }
                } else {
                    imagePicker();
                }
            }
        });
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name) {

        Uri dwonload_Uri;
        if (task != null) {
            dwonload_Uri = task.getResult().getDownloadUrl();
        } else {
            dwonload_Uri = mainImageURI;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("Name", user_name);
        userMap.put("Image", dwonload_Uri.toString());

        firebaseFirestore.collection("Users").document(currentUser).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(SetupActivity.this, "Create Account successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
                setUpProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void imagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mainImageURI = result.getUri();
                imageViewId.setImageURI(mainImageURI);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
