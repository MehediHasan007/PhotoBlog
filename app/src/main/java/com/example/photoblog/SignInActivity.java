package com.example.photoblog;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText userEmailET, userPasswordET;
    private Button signInBtn;
    private ProgressBar singInProgressBar;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        userEmailET = findViewById(R.id.userEmailET);
        userPasswordET = findViewById(R.id.userPasswordET);
        signInBtn = findViewById(R.id.signInBtn);
        singInProgressBar = findViewById(R.id.singInProgressBar);


        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = userEmailET.getText().toString();
                String password = userPasswordET.getText().toString();
                if (email.equals("") || password.equals("")) {
                    Toast.makeText(SignInActivity.this, "Please fill the all fields", Toast.LENGTH_SHORT).show();
                } else {
                    singInProgressBar.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendToMain();
                                userEmailET.setText("");
                                userPasswordET.setText("");
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(SignInActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                            singInProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(auth.getCurrentUser() != null){
            Intent intent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    private void sendToMain() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void signUpOnclick(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

}
