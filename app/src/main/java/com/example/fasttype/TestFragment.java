package com.example.fasttype;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TestFragment extends Fragment {

    // Элементы интерфейса
    private TextView coloredTextView;
    private EditText hiddenInputField;
    private Spinner wordCountSpinner;
    private RadioGroup testTypeRadioGroup;
    private RadioButton timeRadioButton, wordRadioButton;
    private DatabaseReference database;

    // Таймер для режима Time
    private CountDownTimer countDownTimer;

    // Тексты для теста на количество слов
    private final String[] texts = {
            "The quick brown fox jumps over the lazy dog every time.",
            "A curious cat wandered into the garden where colorful flowers bloomed, and birds sang melodies on a sunny afternoon.",
            "In a small village nestled between the mountains, people celebrated the harvest festival with joy and laughter, sharing delicious food, dancing under the stars, and telling stories of their ancestors."
    };
    // Текст для теста на время (очень длинный текст)
    private final String timedTestText = "In the realm of digital innovation, where countless ideas converge, the relentless pursuit of excellence drives developers to push the boundaries of technology. " +
            "This epic narrative is a testament to the indomitable spirit of creativity and perseverance that fuels our progress. " +
            "Every keystroke is a step forward in a journey of discovery, where challenges transform into opportunities, and passion ignites breakthroughs. " +
            "From the humble beginnings of a single line of code to the grand tapestry of interconnected systems, the adventure continues, inspiring minds across the globe to innovate, create, and transcend.";

    // Целевой текст, который нужно ввести
    private String targetText;

    // Переменные для измерения времени
    private long startTime = 0;
    private boolean testFinished = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Получаем ссылки на элементы интерфейса
        coloredTextView = view.findViewById(R.id.coloredTextView);
        hiddenInputField = view.findViewById(R.id.hiddenInputField);
        wordCountSpinner = view.findViewById(R.id.wordCountSpinner);
        testTypeRadioGroup = view.findViewById(R.id.testTypeRadioGroup);
        timeRadioButton = view.findViewById(R.id.radioTime);
        wordRadioButton = view.findViewById(R.id.radioWord);

        // По умолчанию выбираем режим «Word»
        wordRadioButton.setChecked(true);
        wordCountSpinner.setVisibility(View.VISIBLE);
        targetText = texts[0];
        updateColoredText("");

        // Настраиваем адаптер для Spinner (выбор количества слов)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"10", "20", "30"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wordCountSpinner.setAdapter(adapter);
        wordCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                // Режим «Word»: обновляем целевой текст в зависимости от выбранного количества слов
                if (wordRadioButton.isChecked()) {
                    targetText = texts[position];
                    resetTest();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Обработчик переключения режима теста (Time или Word)
        testTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Отменяем таймер, если он был запущен
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            if (checkedId == R.id.radioTime) {
                // Режим «Time»: скрываем Spinner и устанавливаем длинный текст
                wordCountSpinner.setVisibility(View.GONE);
                targetText = timedTestText;
            } else if (checkedId == R.id.radioWord) {
                // Режим «Word»: показываем Spinner и выбираем текст по умолчанию
                wordCountSpinner.setVisibility(View.VISIBLE);
                targetText = texts[wordCountSpinner.getSelectedItemPosition()];
            }
            resetTest();
        });

        // При нажатии на видимый текст передаём фокус невидимому EditText и открываем клавиатуру
        coloredTextView.setOnClickListener(v -> {
            hiddenInputField.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(hiddenInputField, InputMethodManager.SHOW_IMPLICIT);
        });

        // Отслеживаем ввод в невидимом EditText
        hiddenInputField.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String input = s.toString();

                // При вводе первого символа запускаем таймер для режима Time
                if (input.length() > 0 && startTime == 0) {
                    startTime = System.currentTimeMillis();
                    if (timeRadioButton.isChecked()) {
                        countDownTimer = new CountDownTimer(30000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                // Можно обновлять UI с оставшимся временем, если нужно
                            }

                            @Override
                            public void onFinish() {
                                if (!testFinished) {
                                    finishTest();
                                }
                            }
                        }.start();
                    }
                }

                updateColoredText(input);

                // Для режима Word завершаем тест при полном вводе текста
                if (wordRadioButton.isChecked() && input.length() >= targetText.length() && !testFinished) {
                    finishTest();
                }
                isUpdating = false;
            }
        });
    }

    /**
     * Сбрасывает тест: очищает ввод, обновляет отображение и сбрасывает параметры времени.
     */
    private void resetTest() {
        hiddenInputField.setText("");
        updateColoredText("");
        startTime = 0;
        testFinished = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * Завершает тест, вычисляет результаты и отображает их.
     * Длительность теста теперь рассчитывается в секундах.
     */
    private void finishTest() {
        testFinished = true;
        long finishTime = System.currentTimeMillis();
        // Вычисляем длительность в секундах
        double durationSeconds = (finishTime - startTime) / 1000.0;
        double roundedDurationSeconds = Math.round(durationSeconds * 100.0) / 100.0;
        // Для расчёта WPM используем длительность в минутах
        double durationMinutes = durationSeconds / 60.0;
        String input = hiddenInputField.getText().toString();

        // Подсчитываем правильно введённые символы
        int correctCount = 0;
        int len = Math.min(input.length(), targetText.length());
        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == targetText.charAt(i)) {
                correctCount++;
            }
        }
        double accuracy = (correctCount * 100.0) / targetText.length();

        // Вычисляем WPM: для режима Time — по введённым символам, для режима Word — по длине targetText
        double wpm;
        if (timeRadioButton.isChecked()) {
            wpm = ((double) input.length() / 5.0) / durationMinutes;
        } else {
            wpm = ((double) targetText.length() / 5.0) / durationMinutes;
        }

        showResults(accuracy, wpm, roundedDurationSeconds);
        sendResults(accuracy, wpm, roundedDurationSeconds);
    }

    /**
     * Обновляет содержимое coloredTextView, раскрашивая символы:
     * - Белым, если символ введён правильно,
     * - Красным, если допущена ошибка (без подчёркивания),
     * - Черным для оставшихся символов.
     * При этом символ, на котором находится фокус ввода (следующий к вводу), получает оранжевый фон.
     *
     * @param input Введённый текст.
     */
    private void updateColoredText(String input) {
        Spannable spannable = new SpannableString(targetText);
        int inputLength = input.length();
        int targetLength = targetText.length();

        // Раскрашиваем уже введённые символы
        for (int i = 0; i < inputLength && i < targetLength; i++) {
            if (input.charAt(i) == targetText.charAt(i)) {
                spannable.setSpan(
                        new ForegroundColorSpan(getResources().getColor(android.R.color.white)),
                        i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                // Неправильно введённые символы окрашиваем в красный
                spannable.setSpan(
                        new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_dark)),
                        i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        // Для оставшихся символов устанавливаем черный цвет и выделяем текущую позицию оранжевым фоном
        if (inputLength < targetLength) {
            spannable.setSpan(
                    new ForegroundColorSpan(getResources().getColor(android.R.color.black)),
                    inputLength, targetLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(
                    new BackgroundColorSpan(getResources().getColor(android.R.color.holo_orange_light)),
                    inputLength, inputLength + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        coloredTextView.setText(spannable);
    }

    /**
     * Отображает результаты теста в AlertDialog.
     *
     * @param accuracy       Процент правильно введённых символов.
     * @param wpm            Скорость печати (слов в минуту).
     * @param durationSeconds Длительность теста в секундах.
     */
    private void showResults(double accuracy, double wpm, double durationSeconds) {
        String message = String.format("Time: %.2f seconds\nAccuracy: %.2f%%\nWPM: %.2f",
                durationSeconds, accuracy, wpm);
        new AlertDialog.Builder(getContext())
                .setTitle("Test Results")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> resetTest())
                .setCancelable(false)
                .show();
    }

    /**
     * Сохраняет результаты теста в Firebase, форматируя дату с часовым поясом GMT+4.
     */
    private void sendResults(double accuracy, double wpm, double durationSeconds) {
        database = FirebaseDatabase.getInstance().getReference();

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            User currentUser = mainActivity.user;
            if (currentUser != null) {
                String userId = currentUser.getLogin();
                DatabaseReference userTestResultsRef = database.child("users").child(userId).child("test_results");

                String testId = userTestResultsRef.push().getKey();
                if (testId != null) {
                    DatabaseReference newTestRef = userTestResultsRef.child(testId);

                    // Форматируем дату с часовым поясом GMT+4
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT+4"));
                    String formattedDate = sdf.format(new Date());

                    newTestRef.child("date").setValue(formattedDate);
                    newTestRef.child("accuracy").setValue(accuracy);
                    newTestRef.child("wpm").setValue(wpm);
                    newTestRef.child("duration").setValue(durationSeconds);

                    currentUser.setResults(formattedDate, accuracy, wpm, durationSeconds);
                }
            }
        }
    }
}
