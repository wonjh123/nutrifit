package com.example.nutrifit1;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView resultText = findViewById(R.id.result_text);

        ArrayList<String> results = getIntent().getStringArrayListExtra("recommend_results");

        if (results != null && !results.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String line : results) {
                sb.append(line).append("\n\n");
            }
            resultText.setText(sb.toString());
        } else {
            resultText.setText("추천 결과가 없습니다.");
        }
    }
}