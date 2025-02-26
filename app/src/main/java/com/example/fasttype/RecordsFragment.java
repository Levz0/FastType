package com.example.fasttype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RecordsFragment extends Fragment {
    private DatabaseReference database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        loadRecord();
        return view;
    }

    private void loadRecord() {
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");

        // Список для хранения лучших результатов каждого пользователя
        ArrayList<UserRecord> records = new ArrayList<>();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                records.clear();

                // Проходим по каждому пользователю
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    DataSnapshot testResultsSnapshot = userSnapshot.child("test_results");

                    double maxWpm = -1;
                    double bestAccuracy = 0;
                    String bestTestDate = "";

                    // Проходим по каждому тесту пользователя
                    for (DataSnapshot testSnapshot : testResultsSnapshot.getChildren()) {
                        // Если тест не содержит поля "wpm", пропускаем его
                        if (!testSnapshot.hasChild("wpm")) {
                            continue;
                        }
                        Double wpm = testSnapshot.child("wpm").getValue(Double.class);
                        if (wpm == null) continue;

                        if (wpm > maxWpm) {
                            maxWpm = wpm;
                            Double accuracy = testSnapshot.child("accuracy").getValue(Double.class);
                            bestAccuracy = accuracy != null ? accuracy : 0;

                            // Получаем дату, которая может быть сохранена как String или число
                            Object dateObj = testSnapshot.child("date").getValue();
                            String date = (dateObj != null) ? dateObj.toString() : "";
                            bestTestDate = date;

                        }
                    }

                    // Если найден хотя бы один тест с положительным wpm, добавляем результат
                    if (maxWpm > 0) {
                        records.add(new UserRecord(username, bestTestDate, bestAccuracy, maxWpm));
                    }
                }

                // Сортируем записи по убыванию wpm
                Collections.sort(records, new Comparator<UserRecord>() {
                    @Override
                    public int compare(UserRecord r1, UserRecord r2) {
                        return Double.compare(r2.getWpm(), r1.getWpm());
                    }
                });

                // Обновляем TableLayout с полученными данными
                // Предполагается, что данный метод вызывается уже после того, как view создано
                TableLayout tableLayout = getView().findViewById(R.id.recordsTable);
                tableLayout.removeAllViews();

                // Добавляем заголовок таблицы
                TableRow headerRow = new TableRow(getContext());
                headerRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                TextView tvUsernameHeader = new TextView(getContext());
                tvUsernameHeader.setText("Никнейм");
                tvUsernameHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvUsernameHeader.setPadding(8, 8, 8, 8);
                headerRow.addView(tvUsernameHeader);

                TextView tvDateHeader = new TextView(getContext());
                tvDateHeader.setText("Дата");
                tvDateHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvDateHeader.setPadding(8, 8, 8, 8);
                headerRow.addView(tvDateHeader);

                TextView tvAccuracyHeader = new TextView(getContext());
                tvAccuracyHeader.setText("Точность");
                tvAccuracyHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvAccuracyHeader.setPadding(8, 8, 8, 8);
                headerRow.addView(tvAccuracyHeader);

                TextView tvWpmHeader = new TextView(getContext());
                tvWpmHeader.setText("Слов в мин.");
                tvWpmHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvWpmHeader.setPadding(8, 8, 8, 8);
                headerRow.addView(tvWpmHeader);

                tableLayout.addView(headerRow);

                // Добавляем данные для каждого пользователя
                for (UserRecord record : records) {
                    TableRow row = new TableRow(getContext());
                    row.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));

                    TextView tvUsername = new TextView(getContext());
                    tvUsername.setText(record.getUsername());
                    tvUsername.setTextColor(getResources().getColor(android.R.color.white));
                    tvUsername.setPadding(8, 8, 8, 8);
                    row.addView(tvUsername);

                    TextView tvDate = new TextView(getContext());
                    tvDate.setText(record.getDate());
                    tvDate.setTextColor(getResources().getColor(android.R.color.white));
                    tvDate.setPadding(8, 8, 8, 8);
                    row.addView(tvDate);

                    TextView tvAccuracy = new TextView(getContext());
                    tvAccuracy.setText(String.valueOf(record.getAccuracy()) + " %");
                    tvAccuracy.setTextColor(getResources().getColor(android.R.color.white));
                    tvAccuracy.setPadding(8, 8, 8, 8);
                    row.addView(tvAccuracy);

                    TextView tvWpm = new TextView(getContext());
                    tvWpm.setText(String.valueOf(record.getWpm()));
                    tvWpm.setTextColor(getResources().getColor(android.R.color.white));
                    tvWpm.setPadding(8, 8, 8, 8);
                    row.addView(tvWpm);

                    tableLayout.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Database error: " + error.getMessage());
            }
        });
    }

}
