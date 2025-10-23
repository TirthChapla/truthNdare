package com.example.truthanddare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PlayerSetupActivity extends AppCompatActivity {

    private EditText playerCountInput;
    private Button nextButton, startGameButton;
    private LinearLayout namesContainer;
    private ArrayList<EditText> nameFields = new ArrayList<>();
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setup);
        titleText = findViewById(R.id.enterNamesLabel);
        playerCountInput = findViewById(R.id.playerCountInput);
        nextButton = findViewById(R.id.nextButton);
        namesContainer = findViewById(R.id.namesContainer);
        startGameButton = findViewById(R.id.startGameButton);

        nextButton.setOnClickListener(v -> {
            String input = playerCountInput.getText().toString().trim();

            titleText.setText("Enter Player Names");

            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter number of players", Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            try {
                count = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count < 2) {
                Toast.makeText(this, "At least 2 players required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count > 10) {
                Toast.makeText(this, "Maximum 10 players allowed", Toast.LENGTH_SHORT).show();
                count = 10; // limit to 10
            }

            nextButton.setVisibility(View.GONE);
            playerCountInput.setVisibility(View.GONE);

            namesContainer.removeAllViews();
            nameFields.clear();

            for (int i = 1; i <= count; i++) {
                EditText nameInput = new EditText(this);
                nameInput.setHint("Player " + i + " name");
                nameInput.setPadding(16, 16, 16, 16);
                nameInput.setTextColor(getResources().getColor(android.R.color.black));
                nameInput.setHintTextColor(getResources().getColor(android.R.color.holo_orange_light));
                namesContainer.addView(nameInput);
                nameFields.add(nameInput);
            }

            startGameButton.setVisibility(View.VISIBLE);
        });


        startGameButton.setOnClickListener(v -> {
            ArrayList<String> playerNames = new ArrayList<>();

            for (EditText nameField : nameFields) {
                String name = nameField.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter all player names", Toast.LENGTH_SHORT).show();
                    return;
                }
                playerNames.add(name);
            }

            Intent intent = new Intent(PlayerSetupActivity.this, MainActivity.class);
            intent.putStringArrayListExtra("PLAYER_NAMES", playerNames);
            startActivity(intent);
            finish();
        });
    }
}
