package com.example.mealconnect;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 *
 * This activity handles the volunteer management section of the MealConnect app.
 * It displays information or actions related to volunteers (e.g., viewing, adding,
 * or managing volunteer profiles — functionality to be implemented).
 *
 * The layout file used is activity_volunteer.xml. Edge-to-edge mode is enabled to
 * allow UI content to extend behind system bars. Insets are applied dynamically to
 * prevent UI overlap with the status and navigation bars.
 *

 */
public class VolunteerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}