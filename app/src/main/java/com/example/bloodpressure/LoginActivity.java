package com.example.bloodpressure;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button signInButton = findViewById(R.id.sign_in_button);
        EditText usernameEmail = findViewById(R.id.username_email);
        EditText password = findViewById(R.id.password);

        signInButton.setOnClickListener(v -> {
            String username = usernameEmail.getText().toString();
            String pass = password.getText().toString();

            boolean isAuthenticated = authenticateUser(username, pass);
            if (isAuthenticated) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // End previous cleanly
            } else {
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean authenticateUser(String username, String password) {
        return true;
    }
}
