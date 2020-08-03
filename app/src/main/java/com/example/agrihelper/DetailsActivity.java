package com.example.agrihelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.biometrics.BiometricManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class DetailsActivity extends AppCompatActivity {

    ImageView image;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        image = findViewById(R.id.mapImage);
        bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(this.openFileInput("myImage"));
            image.setImageBitmap(bitmap);
            detectImage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void detectImage() {
        File f = new File(this.getCacheDir(), "image.jpg");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

//Convert bitmap to byte array
        Bitmap bp = bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bp.compress(Bitmap.CompressFormat.JPEG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), f);
        Log.e("FileName", f.getName()+ " "+ reqFile.toString());
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", f.getName());
        request(body);

    }

    private void request(MultipartBody.Part body) {
        Service service = new Retrofit.Builder().baseUrl("https://10.0.2.2:5000/").build().create(Service.class);
        Call<ResponseBody> req = service.postImage(body);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("some res", "No s");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                //failure message
                Log.e("Error", "Api Error" + t.getMessage());
                t.printStackTrace();
            }
        });
    }
    interface Service {
        @Multipart
        @POST("/get_land_type")
        Call<ResponseBody> postImage(@Part MultipartBody.Part image);
    }
}