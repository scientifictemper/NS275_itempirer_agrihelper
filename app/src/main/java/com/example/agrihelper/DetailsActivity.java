package com.example.agrihelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.biometrics.BiometricManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
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
import java.util.Objects;

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
            saveImage(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();

        String fname = "Image"+".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            uploadImage(file);
            Log.e("Image", "Saved" + myDir.getAbsolutePath());
        } catch (Exception e) {
            Log.e("File", "Error" + e.toString());
            e.printStackTrace();
        }
    }

    private void uploadImage(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", "Image.jpg", requestFile);
        RequestBody desc = RequestBody.create(MediaType.parse("text/plain"), "Image Type");
        request(body, desc);
    }

    private void request(MultipartBody.Part body,RequestBody desc ) {
        Service service = new Retrofit.Builder().baseUrl("http://10.0.2.2:5000/").build().create(Service.class);
        Call<ResponseBody> req = service.postImage(body, desc);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body() != null) {
                    ResponseBody responseBody = response.body();
                    try {
                        Log.e("Response", responseBody.string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Response", "is null");
                }

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
        @POST("get_land_type")
        Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("Some Data") RequestBody desc);
    }

}