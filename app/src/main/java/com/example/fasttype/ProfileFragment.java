package com.example.fasttype;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private User user;
    private ImageView avatarImageView;

    // Создание фрагмента с передачей объекта User
    public static ProfileFragment newInstance(User user) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Находим кнопку по ID
        Button logoutButton = view.findViewById(R.id.logoutButton);

        // Устанавливаем слушатель нажатий
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        Button selectAvatarButton = view.findViewById(R.id.selectAvatarButton);

        if (user != null) {
            usernameTextView.setText(user.getLogin());
        }

        // Обработчик нажатия кнопки выбора аватара
        selectAvatarButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    // Метод для открытия галереи
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            setAvatar(imageUri);
        }
    }

    // Устанавливаем выбранное изображение в ImageView
    private void setAvatar(Uri imageUri) {
        try {
            Bitmap bitmap;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().getContentResolver(), imageUri));
            } else {
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            }
            avatarImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
        }
    }
}
