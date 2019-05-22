package com.example.photoblog.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.photoblog.CommentsActivity;
import com.example.photoblog.JavaClass.BlogPost;
import com.example.photoblog.JavaClass.User;
import com.example.photoblog.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<BlogPost> blogPostList;
    private List<User> userList;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    RequestOptions placeholderRequest;

    public PostAdapter(List<BlogPost> blogPostList, List<User> userList) {
        this.blogPostList = blogPostList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_list_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        BlogPost currentBlogPost = blogPostList.get(position);
        User currentUser = userList.get(position);

        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final String blogPostId = blogPostList.get(position).BlogPostId;
        String blogUserId = currentBlogPost.getCurrent_UserId();

        //Log.d("blogUserId", "onBindViewHolder: "+blogUserId);

        //Set Blog Content
        holder.userPostedDescriptionTV.setText(String.valueOf(currentBlogPost.getDescription()));

        placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.offwhite_image);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(currentBlogPost.getImage_url()).into(holder.userPostedImages);

        //Date
        try {
            long millisecond = currentBlogPost.getPost_time().getTime();
            String dateString = DateFormat.format("dd MMM yyyy", new Date(millisecond)).toString();
            holder.blogPostDateTV.setText(String.valueOf(dateString));
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //User Data will be retrieved here...
        final String name = currentUser.getName();
        final String image = currentUser.getImage();

        //Log.d("name", "onBindViewHolder: "+name);

        holder.userNameTV.setText(name);

        placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.offwhite_image);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(image).into(holder.userProfileImage);

        //Delete Blog Post
        if(blogUserId.equals(currentUserId)){

            holder.postDeleteBtn.setEnabled(true);
            holder.postDeleteBtn.setVisibility(View.VISIBLE);
        }

        holder.postDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Delete Alert");
                alert.setMessage("Are you sure you want to delete it?");
                alert.setIcon(R.drawable.question);
                alert.setCancelable(false);
                //Positive
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Delete post
                        firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                blogPostList.remove(position);
                                userList.remove(position);
                            }
                        });

                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
        });



        //Get Likes Count
        firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            holder.likesCountTV.setText(count + " Likes");

                        } else {
                            holder.likesCountTV.setText(0 + " Likes");
                        }

                    }
                });

        //Get Likes
        firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes")
                .document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_red_24dp);
                } else {
                    holder.postLikeBtn.setImageResource(R.drawable.ic_favorite_gray_24dp);
                }

            }
        });

        //Likes feature
        holder.postLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes").document(currentUserId)
                        .get().addOnCompleteListener((Activity) context, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("post_time", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes").document(currentUserId).set(likesMap);

                        } else {
                            firebaseFirestore.collection("Posts").document(blogPostId).collection("Likes").document(currentUserId).delete();
                        }

                    }
                });
            }
        });

        //Log.d("Test","error "+ blogPostId);

        //Comments
        holder.postCommentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentsActivity.class);
                intent.putExtra("blog_post_id", blogPostId);
                context.startActivity(intent);
            }
        });

        //Get Comments Count
        firebaseFirestore.collection("Posts").document(blogPostId).collection("Comments")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {

                            int count = documentSnapshots.size();

                            holder.commentsCountTV.setText(count + " Comments");

                        } else {
                            holder.commentsCountTV.setText(0 + " Comments");
                        }

                    }
                });

    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userNameTV, blogPostDateTV, userPostedDescriptionTV, likesCountTV, commentsCountTV;
        private ImageView userPostedImages, postLikeBtn, postCommentsBtn,postDeleteBtn;
        private CircleImageView userProfileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTV = itemView.findViewById(R.id.userNameTV);
            blogPostDateTV = itemView.findViewById(R.id.blogPostDateTV);
            userPostedDescriptionTV = itemView.findViewById(R.id.userPostedDescriptionTV);
            userPostedImages = itemView.findViewById(R.id.userPostedImages);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            likesCountTV = itemView.findViewById(R.id.likesCountTV);
            postLikeBtn = itemView.findViewById(R.id.postLikeBtn);
            postCommentsBtn = itemView.findViewById(R.id.postCommentsBtn);
            commentsCountTV = itemView.findViewById(R.id.commentsCountTV);
            postDeleteBtn = itemView.findViewById(R.id.postDeleteBtn);
        }
    }
}
