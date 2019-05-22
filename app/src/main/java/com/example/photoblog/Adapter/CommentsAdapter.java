package com.example.photoblog.Adapter;


import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.photoblog.JavaClass.BlogPost;
import com.example.photoblog.JavaClass.Comments;
import com.example.photoblog.JavaClass.User;
import com.example.photoblog.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comments> commentsList;
    private List<User> userList;
    public Context context;
    RequestOptions placeholderRequest;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String blog_post_id;

    public CommentsAdapter(List<Comments> commentsList, List<User> userList, String blog_post_id) {
        this.commentsList = commentsList;
        this.userList = userList;
        this.blog_post_id = blog_post_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_comment_item, viewGroup, false);

        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        //Comments
        Comments currentComments = commentsList.get(position);
        String commentUserId = currentComments.getCurrentUserId();

        //Log.d("id", "onBindViewHolder: "+commentUserId);

        holder.commentMessageTV.setText(currentComments.getMessage());

        //User
        User currentUser = userList.get(position);
        String name = currentUser.getName();
        String image = currentUser.getImage();

        holder.userNameTV.setText(name);

        placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.offwhite_image);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(image).into(holder.userProfileImage);


        //Id
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        final String commentPostId = currentComments.CommentPostId;//Comment Random Id

        if(commentUserId.equals(currentUserId)){
            holder.commentDeleteBtn.setEnabled(true);
            holder.commentDeleteBtn.setVisibility(View.VISIBLE);
        }

        //Log.d("id", "onCreate: "+blog_post_id);

        holder.commentDeleteBtn.setOnClickListener(new View.OnClickListener() {
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

                        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").document(commentPostId).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        commentsList.remove(position);
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


    }

    @Override
    public int getItemCount() {
        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userProfileImage;
        private TextView userNameTV, commentMessageTV;
        private ImageView commentDeleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userNameTV = itemView.findViewById(R.id.userNameTV);
            commentMessageTV = itemView.findViewById(R.id.commentMessageTV);
            commentDeleteBtn = itemView.findViewById(R.id.commentDeleteBtn);
        }
    }
}
