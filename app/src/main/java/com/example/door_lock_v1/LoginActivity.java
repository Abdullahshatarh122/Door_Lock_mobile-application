//package com.example.door_lock_v1;
//
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import com.google.firebase.auth.FirebaseAuth;
//
//public class LoginActivity extends AppCompatActivity {
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//        EditText emailField = findViewById(R.id.etEmail);
//        EditText passwordField = findViewById(R.id.etPassword);
//        Button loginButton = findViewById(R.id.btnLogin);
//
//        loginButton.setOnClickListener(v -> {
//            String email = emailField.getText().toString();
//            String password = passwordField.getText().toString();
//
//            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
//                } else {
//                    Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        });
//    }
//}
