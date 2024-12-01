package com.example.android.notepad;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SettingActivity extends Activity {

    private int selectedColor;


    private static final int SELECT_IMAGE_REQUEST_CODE = 1;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);

        // 获取上一次保存的颜色值，如果没有则默认为黑色
        selectedColor = sharedPreferences.getInt("selectedColor", Color.BLACK);



        findViewById(R.id.btSelectBg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
            }
        });
    }

    // 点击颜色块后保存到临时变量，点击保存按钮后保存到SharedPreferences
    public void onColorClick(View view) {
        int id = view.getId();
        if (id == R.id.viewBlue) {
            selectedColor = getResources().getColor(android.R.color.holo_blue_light);
        } else if (id == R.id.viewGreen) {
            selectedColor = getResources().getColor(android.R.color.holo_green_light);
        } else if (id == R.id.viewOrange) {
            selectedColor = getResources().getColor(android.R.color.holo_orange_light);
        } else if (id == R.id.viewRed) {
            selectedColor = getResources().getColor(android.R.color.holo_red_light);
        } else if (id == R.id.viewBlack) {
            selectedColor = getResources().getColor(android.R.color.black);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("selectedColor", selectedColor);
        editor.apply();

        Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data!= null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri!= null) {
                long fileSize = 0;
                try {
                    fileSize = getFileSize(this, selectedImageUri);
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (fileSize > 5 * 1024 * 1024) {
                    Toast.makeText(this, "文件不能超过 5MB", Toast.LENGTH_SHORT).show();
                    return;
                }


                try {
                    Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    float scaleFactor;
                    int originalWidth = originalBitmap.getWidth();
                    int originalHeight = originalBitmap.getHeight();

                    if (originalWidth > originalHeight && originalWidth > 700) {
                        scaleFactor = (float) 700 / originalWidth;
                    } else if (originalHeight > originalWidth && originalHeight > 700) {
                        scaleFactor = (float) 700 / originalHeight;
                    } else {
                        scaleFactor = 1;
                    }

                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleFactor, scaleFactor);

                    Bitmap scaledBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalWidth, originalHeight, matrix, true);

                    saveBg(scaledBitmap);

                    if (!originalBitmap.isRecycled()) {
                        originalBitmap.recycle();
                    }
                    originalBitmap = null;

                    if (!scaledBitmap.isRecycled()) {
                        scaledBitmap.recycle();
                    }
                    scaledBitmap = null;

                    Toast.makeText(this, "设置成功", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "选择背景图片失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 把选择的图片保存到app目录下
    protected void saveBg(Bitmap bitmap) throws IOException {
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (directory!= null) {
            File imageFile = new File(directory, "bg_img.jpg");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            } catch (IOException e) {
                throw e;
            } finally {
                if (fos!= null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static long getFileSize(Context context, Uri uri) throws IOException {
        long fileSize = 0;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor!= null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex!= -1) {
                fileSize = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }
        return fileSize;
    }
}