package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import id.zelory.compressor.Compressor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {

    private ImageView imageViewId;
    private EditText postDescriptionET;
    private Button addPostBtn;
    private ProgressBar setUpProgressBar;
    private Uri postImageUri = null;
    private String currentUser;
    private Bitmap compressedImageBitmap;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        imageViewId = findViewById(R.id.imageViewId);
        postDescriptionET = findViewById(R.id.postDescriptionET);
        addPostBtn = findViewById(R.id.addPostBtn);
        setUpProgressBar = findViewById(R.id.setUpProgressBar);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser().getUid();

        imageViewId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(AddPostActivity.this);
            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String description = postDescriptionET.getText().toString();

                if (!TextUtils.isEmpty(description) && postImageUri != null) {

                    setUpProgressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    // PHOTO UPLOAD
                    File actualImageFile = new File(postImageUri.getPath());
                    try {
                        compressedImageBitmap = new Compressor(AddPostActivity.this)
                                .setMaxWidth(720)
                                .setMaxHeight(720)
                                .setQuality(50)
                                .compressToBitmap(actualImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();


                    UploadTask  post_image_path = storageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
                    post_image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            final String dwonloadUri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()) {

                                // PHOTO UPLOAD
                                File actualImageFile = new File(postImageUri.getPath());
                                try {
                                    compressedImageBitmap = new Compressor(AddPostActivity.this)
                                            .setMaxWidth(240)
                                            .setMaxHeight(240)
                                            .setQuality(6)
                                            .compressToBitmap(actualImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child("post_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                       final String dwonloadThumbUri = taskSnapshot.getDownloadUrl().toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", dwonloadUri);
                                        postMap.put("thumb_url", dwonloadThumbUri);
                                        postMap.put("description", description);
                                        postMap.put("current_UserId", currentUser);
                                        postMap.put("post_time", FieldValue.serverTimestamp());

                                        //FirebaseFirestore
                                        firebaseFirestore.collection("Posts").add(postMap).
                                                addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(AddPostActivity.this, "Post was added", Toast.LENGTH_SHORT).show();
                                                    imageViewId.setImageResource(R.drawable.memories);
                                                    postDescriptionET.setText("");
                                               /*     Intent intent = new Intent(AddPostActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();*/

                                                } else {
                                                    String errorMessage = task.getException().getMessage();
                                                    Toast.makeText(AddPostActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                                }

                                                setUpProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                setUpProgressBar.setVisibility(View.INVISIBLE);

                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(AddPostActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                postImageUri = result.getUri();
                imageViewId.setImageURI(postImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
