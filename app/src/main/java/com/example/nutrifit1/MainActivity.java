package com.example.nutrifit1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.doinglab.foodlens.sdk.core.model.Food;
import com.doinglab.foodlens.sdk.core.model.Nutrition;
import com.doinglab.foodlens.sdk.core.model.result.RecognitionResult;
import com.doinglab.foodlens.sdk.core.type.FoodLensType;
import com.doinglab.foodlens.sdk.ui.FoodLensUI;
import com.doinglab.foodlens.sdk.ui.FoodLensUIService;
import com.doinglab.foodlens.sdk.ui.UIServiceResultHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FoodLensUIService foodLensUiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. FoodLens UI Service 초기화
        foodLensUiService = FoodLensUI.createFoodLensService(this, FoodLensType.FoodLens);

        // 2. ActivityResultLauncher 등록
        ActivityResultLauncher<Intent> foodLensActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        foodLensUiService.onActivityResult(result.getResultCode(), result.getData());
                    }
                }
        );

        // 3. 버튼 클릭 시 카메라 실행
        Button btn = findViewById(R.id.btn_start);
        btn.setOnClickListener(view -> {
            foodLensUiService.startFoodLensCamera(
                    MainActivity.this,
                    foodLensActivityResult,
                    new UIServiceResultHandler() {
                        @Override
                        public void onSuccess(@Nullable RecognitionResult result) {
                            if (result == null || result.getFoods() == null || result.getFoods().isEmpty()) {
                                Log.d("FoodLens", "인식 결과 없음");
                                return;
                            }

                            try {
                                //유저 정보
                                //TODO : 유저 정보 아무렇게나 해놓은 상태
                                JSONObject user = new JSONObject();
                                user.put("weight", 60);
                                user.put("height", 170);
                                user.put("age", 25);
                                user.put("gender", "1");   // "1" 남자, "2" 여자
                                user.put("activity", 3);
                                user.put("taste1", 1);
                                user.put("taste2", 11);
                                JSONArray userArray = new JSONArray();
                                userArray.put(user);

                                //TODO : 지금은 음식 한개로만 추천 나중에는 List로 보내기
                                Food food = result.getFoods().get(0);
                                List<Nutrition> nutritionList = food.getCandidates();

                                if (nutritionList == null || nutritionList.isEmpty()) {
                                    Log.d("FoodLens", "⚠️ nutritionList 비어 있음");
                                    return;
                                }

                                Nutrition n = nutritionList.get(0);  // nutrition 하나만 사용

                                JSONObject nutri = new JSONObject();
                                nutri.put("calories", n.getEnergy());
                                nutri.put("protein", n.getProtein());
                                nutri.put("fat", n.getFat());
                                nutri.put("carbonhydrate", n.getCarbohydrate());
                                nutri.put("sugar", n.getTotalSugars());
                                nutri.put("dietrayfiber", n.getTotalDietaryFiber());
                                nutri.put("calcium", n.getCalcium());
                                nutri.put("iron", n.getIron());
                                nutri.put("sodium", n.getSodium());
                                nutri.put("vitamina", n.getVitaminA());
                                nutri.put("vitaminc", n.getVitaminC());

                                JSONObject foodJson = new JSONObject();
                                foodJson.put("nutrition", nutri);
                                foodJson.put("eatAmount", 1.0);  // 먹은 양

                                JSONArray foods = new JSONArray();
                                foods.put(foodJson);

                                JSONObject root = new JSONObject();
                                root.put("user", userArray);
                                root.put("foods", foods);

                                //서버 전송
                                OkHttpClient client = new OkHttpClient();
                                RequestBody body = RequestBody.create(root.toString(), MediaType.get("application/json; charset=utf-8"));
                                Request request = new Request.Builder()
                                        .url("http://43.203.201.216:5000/recommend")
                                        .post(body)
                                        .build();

                                client.newCall(request).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.e("FoodLens", "서버 전송 실패", e);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        if (!response.isSuccessful()) {
                                            Log.e("FoodLens", "서버 오류: " + response.code());
                                            return;
                                        }

                                        String responseBody = response.body().string();
                                        try {
                                            JSONArray arr = new JSONArray(responseBody);
                                            ArrayList<String> displayList = new ArrayList<>();

                                            for (int i = 0; i < arr.length(); i++) {
                                                JSONObject rec = arr.getJSONObject(i);
                                                String name = rec.getString("대표식품명");
                                                double kcal = rec.getDouble("에너지(kcal)");
                                                displayList.add("🍱 " + name + "\n칼로리: " + kcal + " kcal");
                                            }

                                            //커스텀 UI 화면 전환
                                            runOnUiThread(() -> {
                                                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                                                intent.putStringArrayListExtra("recommend_results", displayList);
                                                startActivity(intent);
                                            });
                                        } catch (JSONException e) {
                                            Log.e("FoodLens", "응답 파싱 실패", e);
                                        }
                                    }
                                });

                            } catch (JSONException e) {
                                Log.e("FoodLens", "JSON 생성 실패", e);
                            }
                        }

                        @Override
                        public void onError(@Nullable com.doinglab.foodlens.sdk.core.error.BaseError baseError) {
                            Log.e("FoodLens", "에러 발생: " + baseError.getMessage());
                        }

                        @Override
                        public void onCancel() {
                            Log.d("FoodLens", "사용자 취소");
                        }
                    }
            );
        });
    }
}