package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.photoblog.Adapter.CommentsAdapter;
import com.example.photoblog.JavaClass.Comments;
import com.example.photoblog.JavaClass.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private EditText commentsET;
    private ImageView commentsBtn;

    private RecyclerView commentsRecyclerViewId;
    private List<Comments> commentsList;
    private CommentsAdapter commentsAdapter;

    //Test
    private List<User> userList;


    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String blog_post_id;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //Get Intent
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        //Log.d("id", "onCreate: "+blog_post_id);

        commentsET = findViewById(R.id.commentsET);
        commentsBtn = findViewById(R.id.commentsBtn);

        commentsRecyclerViewId = findViewById(R.id.commentsRecyclerViewId);
        commentsList = new ArrayList<>();
        userList = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(commentsList,userList,blog_post_id);
        commentsRecyclerViewId.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerViewId.setAdapter(commentsAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();




        commentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comments = commentsET.getText().toString();

                if(comments.equals("")){
                    Toast.makeText(CommentsActivity.this, "Enter Your Comments", Toast.LENGTH_SHORT).show();
                }else{

                    Map<String,Object> commentsMap = new HashMap<>();
                    commentsMap.put("message",comments);
                    commentsMap.put("currentUserId",currentUserId);
                    commentsMap.put("post_time", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else {
                                commentsET.setText("");
                            }
                        }
                    });

                }
            }
        });

        //Get comments from FirebaseFirestore
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(CommentsActivity.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty()){

                    for(DocumentChange doc: documentSnapshots.getDocumentChanges()){

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String commentPostId = doc.getDocument().getId();//Document random Id
                            String commentUserId = doc.getDocument().getString("currentUserId");
                            //Log.d("test", "onEvent: "+commentPostId);

                            final Comments comments = doc.getDocument().toObject(Comments.class).withCommentId(commentPostId);

                            firebaseFirestore.collection("Users").document(commentUserId).get()
                                    .addOnCompleteListener(CommentsActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()){

                                             /*   String name = task.getResult().getString("Name");
                                                Log.d("test", "onEvent: "+name);*/

                                                User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                commentsList.add(comments);

                                                commentsAdapter.notifyDataSetChanged();

                                                //Log.d("test", "onEvent: "+commentUser);

                                            }else{
                                                Toast.makeText(CommentsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });

                        }
                    }
                }

            }
        });

    }
}
