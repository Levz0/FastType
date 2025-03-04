package com.example.fasttype;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordsFragment extends Fragment {
    private DatabaseReference database;
    // Список для хранения последних 5 тестов текущего пользователя (для экспорта)
    private ArrayList<TestResult> userResultsList = new ArrayList<>();

    // Вспомогательный класс для хранения одного тестового результата
    public static class TestResult {
        String dateString;
        double accuracy;
        double wpm;
        long timestamp; // для сортировки по дате

        TestResult(String dateString, double accuracy, double wpm, long timestamp) {
            this.dateString = dateString;
            this.accuracy = accuracy;
            this.wpm = wpm;
            this.timestamp = timestamp;
        }
    }

    // Метод для парсинга даты – пытается преобразовать значение в long
    private long parseDate(String dateString) {
        try {
            // Попытка преобразовать как число (timestamp)
            return Long.parseLong(dateString);
        } catch (NumberFormatException e) {
            try {
                // Если не число, парсим по формату "dd-MM-yyyy HH:mm:ss"
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(dateString);
                if (date != null) return date.getTime();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);
        loadRecord();

        // Кнопка экспорта результатов
        Button btnExport = view.findViewById(R.id.btnExport);
        btnExport.setOnClickListener(v -> showExportDialog());
        return view;
    }

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Выберите формат экспорта:");
        String[] options = {"PDF", "Excel"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                exportToPDF(userResultsList);
            } else if (which == 1) {
                exportToExcel(userResultsList);
            }
        });
        builder.show();
    }

    private void exportToPDF(ArrayList<TestResult> results) {
        // Пример экспорта в PDF с использованием android.graphics.pdf.PdfDocument
        PdfDocument pdfDocument = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);
        int y = 50;
        canvas.drawText("Мои результаты", 20, y, paint);
        y += 30;
        for (TestResult result : results) {
            String line = "Дата: " + result.dateString + ", Точность: " + result.accuracy + "%, WPM: " + result.wpm;
            canvas.drawText(line, 20, y, paint);
            y += 20;
            if (y > pageHeight - 50) {
                break; // Ограничимся одной страницей для примера
            }
        }
        pdfDocument.finishPage(page);

        // Сохраняем PDF-файл во внешнее хранилище
        try {
            File pdfFile = new File(getContext().getExternalFilesDir(null), "my_results.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();
            Toast.makeText(getContext(), "PDF сохранен: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при сохранении PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToExcel(ArrayList<TestResult> results) {
        try {
            // Создаем файл в папке внешнего хранилища приложения
            File file = new File(getContext().getExternalFilesDir(null), "my_results.xls");
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("Мои результаты", 0);

            // Заголовок таблицы

            sheet.addCell(new Label(0, 0, "Дата"));
            sheet.addCell(new Label(1, 0, "Точность"));
            sheet.addCell(new Label(2, 0, "Слов в мин."));

            // Заполняем данными
            for (int i = 0; i < results.size(); i++) {
                TestResult result = results.get(i);
                sheet.addCell(new Label(0, i + 1, result.dateString));
                sheet.addCell(new Number(1, i + 1, result.accuracy));
                sheet.addCell(new Number(2, i + 1, result.wpm));
            }
            workbook.write();
            workbook.close();
            Toast.makeText(getContext(), "Excel файл сохранен: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Ошибка при сохранении Excel файла", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRecord() {
        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference usersRef = database.child("users");

        // Список для хранения глобальных рекордов
        ArrayList<UserRecord> records = new ArrayList<>();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                records.clear();

                // Глобальные рекорды: проходим по всем пользователям
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    DataSnapshot testResultsSnapshot = userSnapshot.child("test_results");

                    double maxWpm = -1;
                    double bestAccuracy = 0;
                    String bestTestDate = "";

                    // Проходим по каждому тесту пользователя
                    for (DataSnapshot testSnapshot : testResultsSnapshot.getChildren()) {
                        if (!testSnapshot.hasChild("wpm")) continue;
                        Double wpm = testSnapshot.child("wpm").getValue(Double.class);
                        if (wpm == null) continue;
                        if (wpm > maxWpm) {
                            maxWpm = wpm;
                            Double accuracy = testSnapshot.child("accuracy").getValue(Double.class);
                            bestAccuracy = (accuracy != null) ? accuracy : 0;
                            Object dateObj = testSnapshot.child("date").getValue();
                            bestTestDate = (dateObj != null) ? dateObj.toString() : "";
                        }
                    }
                    if (maxWpm > 0) {
                        records.add(new UserRecord(username, bestTestDate, bestAccuracy, maxWpm));
                    }
                }

                // Сортировка глобальных рекордов по убыванию wpm
                Collections.sort(records, new Comparator<UserRecord>() {
                    @Override
                    public int compare(UserRecord r1, UserRecord r2) {
                        return Double.compare(r2.getWpm(), r1.getWpm());
                    }
                });

                // Заполнение таблицы глобальных рекордов
                TableLayout recordsTable = getView().findViewById(R.id.recordsTable);
                recordsTable.removeAllViews();
                TableRow headerRow = new TableRow(getContext());
                headerRow.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

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

                recordsTable.addView(headerRow);

                for (UserRecord record : records) {
                    TableRow row = new TableRow(getContext());
                    row.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

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
                    tvAccuracy.setText(record.getAccuracy() + " %");
                    tvAccuracy.setTextColor(getResources().getColor(android.R.color.white));
                    tvAccuracy.setPadding(8, 8, 8, 8);
                    row.addView(tvAccuracy);

                    TextView tvWpm = new TextView(getContext());
                    tvWpm.setText(String.valueOf(record.getWpm()));
                    tvWpm.setTextColor(getResources().getColor(android.R.color.white));
                    tvWpm.setPadding(8, 8, 8, 8);
                    row.addView(tvWpm);

                    recordsTable.addView(row);
                }

                // Обработка таблицы "Мои результаты" – последние 5 тестов текущего пользователя
                TableLayout myResultsTable = getView().findViewById(R.id.myResultsTable);
                myResultsTable.removeAllViews();
                TableRow myResultsHeader = new TableRow(getContext());
                myResultsHeader.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView tvMyDateHeader = new TextView(getContext());
                tvMyDateHeader.setText("Дата");
                tvMyDateHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvMyDateHeader.setPadding(8, 8, 8, 8);
                myResultsHeader.addView(tvMyDateHeader);

                TextView tvMyAccuracyHeader = new TextView(getContext());
                tvMyAccuracyHeader.setText("Точность");
                tvMyAccuracyHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvMyAccuracyHeader.setPadding(8, 8, 8, 8);
                myResultsHeader.addView(tvMyAccuracyHeader);

                TextView tvMyWpmHeader = new TextView(getContext());
                tvMyWpmHeader.setText("Слов в мин.");
                tvMyWpmHeader.setTextColor(getResources().getColor(android.R.color.white));
                tvMyWpmHeader.setPadding(8, 8, 8, 8);
                myResultsHeader.addView(tvMyWpmHeader);

                myResultsTable.addView(myResultsHeader);

                // Получаем текущего пользователя из MainActivity
                String currentUser = ((MainActivity) getActivity()).user.getLogin();
                DataSnapshot currentUserSnapshot = snapshot.child(currentUser);
                if (currentUserSnapshot.exists()) {
                    DataSnapshot testResultsSnapshot = currentUserSnapshot.child("test_results");
                    // Собираем все тесты текущего пользователя
                    ArrayList<TestResult> userResults = new ArrayList<>();
                    for (DataSnapshot testSnapshot : testResultsSnapshot.getChildren()) {
                        if ("placeholder".equals(testSnapshot.getKey())) continue;
                        Object dateObj = testSnapshot.child("date").getValue();
                        String date = (dateObj != null) ? dateObj.toString() : "";
                        Double accuracy = testSnapshot.child("accuracy").getValue(Double.class);
                        if (accuracy == null) accuracy = 0.0;
                        Double wpm = testSnapshot.child("wpm").getValue(Double.class);
                        if (wpm == null) wpm = 0.0;
                        long timestamp = parseDate(date);
                        userResults.add(new TestResult(date, accuracy, wpm, timestamp));
                    }
                    // Сортируем тесты по убыванию даты (новейшие первыми)
                    Collections.sort(userResults, new Comparator<TestResult>() {
                        @Override
                        public int compare(TestResult t1, TestResult t2) {
                            return Long.compare(t2.timestamp, t1.timestamp);
                        }
                    });
                    // Выбираем последние 5 тестов
                    List<TestResult> lastFive = userResults.size() > 5 ? userResults.subList(0, 5) : userResults;
                    // Сохраняем список в глобальную переменную для экспорта
                    userResultsList.clear();
                    userResultsList.addAll(new ArrayList<>(lastFive));

                    for (TestResult result : lastFive) {
                        TableRow row = new TableRow(getContext());
                        row.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView tvDate = new TextView(getContext());
                        tvDate.setText(result.dateString);
                        tvDate.setTextColor(getResources().getColor(android.R.color.white));
                        tvDate.setPadding(8, 8, 8, 8);
                        row.addView(tvDate);

                        TextView tvAccuracy = new TextView(getContext());
                        tvAccuracy.setText(result.accuracy + " %");
                        tvAccuracy.setTextColor(getResources().getColor(android.R.color.white));
                        tvAccuracy.setPadding(8, 8, 8, 8);
                        row.addView(tvAccuracy);

                        TextView tvWpm = new TextView(getContext());
                        tvWpm.setText(String.valueOf(result.wpm));
                        tvWpm.setTextColor(getResources().getColor(android.R.color.white));
                        tvWpm.setPadding(8, 8, 8, 8);
                        row.addView(tvWpm);

                        myResultsTable.addView(row);
                    }
                } else {
                    Toast.makeText(getContext(), "Нет результатов для пользователя " + currentUser, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Database error: " + error.getMessage());
            }
        });
    }
}
