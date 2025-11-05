package com.example.mealconnect;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 *
 * This is the entry point of the application. It displays the main
 * menu, allowing users to navigate to different parts of the app such as:
 *  - Client registration
 *  - Client information
 *  - Volunteer management
 *
 * Each button redirects the user to a different Activity using Intents.
 * This Activity uses activity_main.xml as its layout file.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button registerClientBtn = findViewById(R.id.registerClientBtn);
        Button clientInfoBtn = findViewById(R.id.clientInfoBtn);
        Button volunteerBtn = findViewById(R.id.volunteerBtn);


        registerClientBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ClientRegisterActivity.class));
        });


        clientInfoBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, ClientInfoActivity.class));
        });


        volunteerBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, VolunteerActivity.class));
        });
    }
}
