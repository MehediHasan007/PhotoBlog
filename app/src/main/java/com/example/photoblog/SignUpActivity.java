package com.example.photoblog;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private TextView userEmailET, userPasswordET, userConfirmPasswordET;
    private Button signUpBtn;
    private ProgressBar signUpProgressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        userEmailET = findViewById(R.id.userEmailET);
        userPasswordET = findViewById(R.id.userPasswordET);
        userConfirmPasswordET = findViewById(R.id.userConfirmPasswordET);
        signUpBtn = findViewById(R.id.signUpBtn);
        signUpProgressBar = findViewById(R.id.signUpProgressBar);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = userEmailET.getText().toString();
                String password = userPasswordET.getText().toString();
                String confirmPassword = userConfirmPasswordET.getText().toString();
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if (email.equals("")) {
                    Toast.makeText(SignUpActivity.this, "Please enter email", Toast.LENGTH_SHORT).show();
                } else if (!email.matches(emailPattern)) {
                    Toast.makeText(SignUpActivity.this, "Please enter valid email", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                } else {
                    signUpProgressBar.setVisibility(View.VISIBLE);
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(SignUpActivity.this,SetupActivity.class));
                                finish();
                            }else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }

                            signUpProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

    public void signInOnclick(View view) {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
