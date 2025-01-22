package com.example.fasttype;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TestFragment extends Fragment {

    private EditText inputField;
    private String targetText;
    private final String[] texts = {
            "The quick brown fox jumps over the lazy dog every time.", // 10 слов
            "A curious cat wandered into the garden where colorful flowers bloomed, and birds sang melodies on a sunny afternoon.", // 20 слов
            "In a small village nestled between the mountains, people celebrated the harvest festival with joy and laughter, sharing delicious food, dancing under the stars, and telling stories of their ancestors." // 30 слов
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputField = view.findViewById(R.id.inputField);
        Spinner wordCountSpinner = view.findViewById(R.id.wordCountSpinner);

        // Устанавливаем начальный текст по умолчанию (10 слов)
        targetText = texts[0];
        inputField.setHint(targetText);

        // Создаем адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{"10", "20", "30"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wordCountSpinner.setAdapter(adapter);

        // Обработчик выбора в Spinner
        wordCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetText = texts[position];
                inputField.setText(""); // Очищаем поле ввода
                inputField.setHint(targetText); // Обновляем подсказку
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Логика проверки ввода
        inputField.addTextChangedListener(new TextWatcher() {
            boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                isUpdating = true;
                String input = s.toString();
                Spannable spannable = new SpannableString(targetText);

                int inputLength = input.length();
                int targetLength = targetText.length();

                for (int i = 0; i < inputLength; i++) {
                    if (i >= targetLength) break;

                    if (input.charAt(i) == targetText.charAt(i)) {
                        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.white)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_dark)), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

                if (inputLength < targetLength) {
                    spannable.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)), inputLength, targetLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                inputField.setText(spannable);
                inputField.setSelection(Math.min(inputLength, targetLength));
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

}
