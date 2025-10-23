package com.example.truthanddare;

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView bottle;
    private RelativeLayout playerContainer;
    private Button truthButton, dareButton, nhieButton;

    private ArrayList<String> playerNames;
    private ArrayList<TextView> playerTextViews = new ArrayList<>();
    private Random random;
    private int lastDirection;
    private boolean spinning;
    private MediaPlayer mediaPlayer;

    private String selectedPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottle = findViewById(R.id.bottle);
        playerContainer = findViewById(R.id.playerContainer);

        truthButton = findViewById(R.id.truthButton);
        dareButton = findViewById(R.id.dareButton);
        nhieButton = findViewById(R.id.nhieButton);

        random = new Random();
        mediaPlayer = MediaPlayer.create(this, R.raw.spin);

        // Get player names
        playerNames = getIntent().getStringArrayListExtra("PLAYER_NAMES");
        if (playerNames == null || playerNames.isEmpty()) {
            Toast.makeText(this, "No player names received", Toast.LENGTH_SHORT).show();
            playerNames = new ArrayList<>();
            playerNames.add("Player 1");
            playerNames.add("Player 2");
        }

        // Limit to maximum 10 players
        if (playerNames.size() > 10) {
            playerNames = new ArrayList<>(playerNames.subList(0, 10));
            Toast.makeText(this, "Maximum 10 players allowed.", Toast.LENGTH_SHORT).show();
        }

        // Create TextViews dynamically
        createPlayerTextViews(playerNames);

        bottle.setOnClickListener(v -> {
            if (!spinning) {
                // Hide buttons during spin
                truthButton.setVisibility(View.GONE);
                dareButton.setVisibility(View.GONE);
                nhieButton.setVisibility(View.GONE);

                spinBottle();
            }
        });

        // Button click listeners
        truthButton.setOnClickListener(v -> fetchQuestion("https://api.truthordarebot.xyz/v1/truth", "Truth"));
        dareButton.setOnClickListener(v -> fetchQuestion("https://api.truthordarebot.xyz/api/dare", "Dare"));
        nhieButton.setOnClickListener(v -> fetchQuestion("https://api.truthordarebot.xyz/api/nhie", "Never Have I Ever"));
    }

    private void createPlayerTextViews(ArrayList<String> players) {
        playerContainer.removeAllViews();
        playerTextViews.clear();

        int totalPlayers = players.size();
        float radius = 450; // distance from bottle center

        bottle.post(() -> {
            float centerX = playerContainer.getWidth() / 2f;
            float centerY = playerContainer.getHeight() / 2f;


            for (int i = 0; i < totalPlayers; i++) {
                TextView playerText = new TextView(this);
                playerText.setText(players.get(i));
                playerText.setTextSize(16);
                playerText.setTextColor(Color.BLACK);
                playerText.setPadding(16, 16, 16, 16);
                playerText.setBackgroundColor(Color.parseColor("#DDDDDD"));
                playerText.setId(View.generateViewId());
                playerText.setTextSize(12);


                int left, top;

                if (totalPlayers == 2) {
                    // Measure the TextView
                    playerText.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int textWidth = playerText.getMeasuredWidth();

                    // Two players: top and bottom, properly centered
                    left = (int) (centerX - textWidth / 2);
                    top = (i == 0) ? (int) (centerY - radius) : (int) (centerY + radius);
                } else {
                    // Original circular placement for 3+ players
                    float angle = 360f / totalPlayers * i;
                    left = (int) (centerX + radius * Math.cos(Math.toRadians(angle)) - 50);
                    top = (int) (centerY + radius * Math.sin(Math.toRadians(angle)) - 50);
                }

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.leftMargin = left;
                params.topMargin = top;

                playerContainer.addView(playerText, params);
                playerTextViews.add(playerText);
            }
        });
    }

    private void spinBottle() {
        int newDirection = random.nextInt(3600) + 360;
        float pivotX = bottle.getWidth() / 2f;
        float pivotY = bottle.getHeight() / 2f;

        RotateAnimation rotate = new RotateAnimation(lastDirection, newDirection, pivotX, pivotY);
        rotate.setDuration(2000);
        rotate.setFillAfter(true);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                spinning = true;
                mediaPlayer.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                spinning = false;
                determinePlayer();

                // Show buttons after spin
                truthButton.setVisibility(View.VISIBLE);
                dareButton.setVisibility(View.VISIBLE);
                nhieButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        lastDirection = newDirection;
        bottle.startAnimation(rotate);
    }

    private void determinePlayer() {
        int degrees = lastDirection % 360;
        int selectedIndex = degrees / (360 / playerNames.size());
        if (selectedIndex >= playerNames.size()) selectedIndex = playerNames.size() - 1;
        selectedPlayer = playerNames.get(selectedIndex);

        // Do NOT display resultTextView
        // resultTextView.setText("🎯 " + selectedPlayer + " it's your turn!");
    }


    private void fetchQuestion(String apiUrl, String type) {
        new Thread(() -> {
            String question = null;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String jsonResponse = response.body().string();
                        JSONObject json = new JSONObject(jsonResponse);
                        question = json.getString("question");
                    } else {
                        question = "Failed to fetch question! HTTP Error: " + response.code();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                question = "Failed to fetch question! Error: " + e.getMessage();
            }

            final String finalQuestion = question;
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(type);
                builder.setMessage(finalQuestion);
                builder.setPositiveButton("Done", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        }).start();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }
}
