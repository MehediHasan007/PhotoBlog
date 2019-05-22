package com.example.photoblog.Fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.photoblog.Adapter.PostAdapter;
import com.example.photoblog.AddPostActivity;
import com.example.photoblog.JavaClass.BlogPost;
import com.example.photoblog.JavaClass.User;
import com.example.photoblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FloatingActionButton floatingActionBtn;
    private RecyclerView recyclerViewId;
    private List<BlogPost> blogPostList;
    private List<User> userList;
    private FirebaseFirestore firebaseFirestore;
    private PostAdapter postAdapter;
    private FirebaseAuth firebaseAuth;
    private int pageLimit = 500;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        floatingActionBtn = rootView.findViewById(R.id.floatingActionBtn);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerViewId = rootView.findViewById(R.id.recyclerViewId);
        blogPostList = new ArrayList<>();
        userList = new ArrayList<>();
        postAdapter = new PostAdapter(blogPostList, userList);
        recyclerViewId.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerViewId.setAdapter(postAdapter);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerViewId.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if (reachedBottom) {
                        Toast.makeText(getActivity(), "Next Page", Toast.LENGTH_SHORT).show();
                        loadNextPost();
                    }
                }
            });


            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("post_time", Query.Direction.DESCENDING).limit(pageLimit);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blogPostList.clear();
                            userList.clear();

                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);////

                                String blogUserId = doc.getDocument().getString("current_UserId");

                                firebaseFirestore.collection("Users").document(blogUserId).get()
                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    User user = task.getResult().toObject(User.class);

                                                    //Log.d("user", "onComplete: "+user);

                                                    if (isFirstPageFirstLoad) {

                                                        userList.add(user);
                                                        blogPostList.add(blogPost);

                                                    } else {

                                                        userList.add(0, user);
                                                        blogPostList.add(0, blogPost);
                                                    }
                                                    postAdapter.notifyDataSetChanged();

                                                }
                                            }
                                        });
                            }
                        }
                        isFirstPageFirstLoad = false;
                    }//
                }
            });
        }

        floatingActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddPostActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    public void loadNextPost() {

        if (firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("post_time", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(pageLimit);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);

                                String blogUserId = doc.getDocument().getString("current_UserId");

                                firebaseFirestore.collection("Users").document(blogUserId).get()
                                        .addOnCompleteListener(getActivity(), new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {

                                                    User user = task.getResult().toObject(User.class);

                                                    userList.add(user);
                                                    blogPostList.add(blogPost);

                                                    postAdapter.notifyDataSetChanged();

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

}
