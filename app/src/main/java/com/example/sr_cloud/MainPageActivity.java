package com.example.sr_cloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class MainPageActivity extends AppCompatActivity {

    private FirebaseAuth _firebaseAuth;

    private int _count;
    private String[] arrPath;
    private String base64encoded;


    Bitmap allImages;
    ImageView imageView;
    ProgressBar progressBar;
    TextView textView;

    File f = null;
    int sayacdeneme = 0;
    int silSayac = 0;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private FirebaseFunctions mFunctions;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.cloud_options_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.upload) {
            //Alttaki if izinler verilmişmi diye kontrol eder.
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
            else {
                //Bütün resimleri bulan bölümdür. Alttaki while döngüsü sayesinde. Program 2. defa çalıştırılınca girilecek yerdir.
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
                final String orderBy = MediaStore.Images.Media._ID;
                Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy
                );
                _count = cursor.getCount();
                arrPath = new String[_count];
                int i=0;
                while (cursor.moveToNext()) {
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    arrPath[i] = cursor.getString(dataColumnIndex);
                    try {
                        if(Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), Uri.fromFile(new File(arrPath[i])));
                            allImages = ImageDecoder.decodeBitmap(source);

                            //Convert bitmap to base64 encoded string
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            allImages.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();
                            base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                            //Convert bitmap to base64 encoded string FINISH
                        }
                        else {
                            allImages = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(arrPath[i])));
                        }

                        f = new File(arrPath[i]);
                        String imageName = f.getName();
                        imageView.setImageBitmap(allImages);
                        upload(imageView, imageName, Uri.fromFile(new File(arrPath[i])), arrPath, allImages, base64encoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                cursor.close();
            }
        }
        else if(item.getItemId() == R.id.signout) {
            _firebaseAuth.signOut();
            Intent intentOut = new Intent(MainPageActivity.this, SignActivity.class);
            startActivity(intentOut);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1) {
            //Bütün resimleri bulan bölümdür. Alttaki while döngüsü sayesinde. Program ilk defa çalıştırılınca girilecek yerdir.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
                final String orderBy = MediaStore.Images.Media._ID;
                Cursor cursor = getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy
                );
                _count = cursor.getCount();
                arrPath = new String[_count];
                int i=0;

                while (cursor.moveToNext()) {
                    int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    arrPath[i] = cursor.getString(dataColumnIndex);

                    try {
                        if(Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), Uri.fromFile(new File(arrPath[i])));
                            allImages = ImageDecoder.decodeBitmap(source);

                            //Convert bitmap to base64 encoded string
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            allImages.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] imageBytes = byteArrayOutputStream.toByteArray();
                            base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                            //Convert bitmap to base64 encoded string FINISH

                        }
                        else {
                            allImages = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(arrPath[i])));
                        }

                        f = new File(arrPath[i]);
                        String imageName = f.getName();
                        upload(imageView, imageName, Uri.fromFile(new File(arrPath[i])), arrPath, allImages, base64encoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
                cursor.close();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void upload(View view, String imageName, Uri allImages, String[] arrPath, Bitmap allImagess, String base64encoded) {
        silSayac++;
        /////Vision Api ML
        FirebaseVisionImage image;
        try {
            image = FirebaseVisionImage.fromFilePath(getApplicationContext(), allImages);
            FirebaseVisionImageLabeler detector = FirebaseVision.getInstance()
                    .getCloudImageLabeler();

            Task<List<FirebaseVisionImageLabel>> result = detector.processImage(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                                    String text = "";
                                    String entityId;
                                    float confidence;

                                    for (FirebaseVisionImageLabel label: labels) {
                                        text = label.getText();
                                        entityId = label.getEntityId();
                                        confidence = label.getConfidence();
                                        System.out.println("Sonuç : "+silSayac+" "+imageName+" "+text+" "+entityId+" "+confidence);
                                        break;
                                    }
                                    StorageMetadata metadata = new StorageMetadata.Builder()
                                            .setContentType("image/jpg")
                                            .setCustomMetadata("label",text)
                                            .build();


                                    ///Image Upload
                                    progressBar.setMax(arrPath.length);
                                    textView.setText("%0");

                                    FirebaseUser firebaseUser = _firebaseAuth.getCurrentUser();
                                    storageReference.child("images").child(firebaseUser.getEmail()).child(imageName).putFile(allImages, metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            imageView.setImageBitmap(allImagess);
                                            progressBar.setProgress(sayacdeneme+1);
                                            sayacdeneme++;
                                            textView.setText("%"+new DecimalFormat("###").format((float) sayacdeneme/arrPath.length*100));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainPageActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    ///Image Upload FINISH
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainPageActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_LONG).show();
                                    // ...
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        /////Vision Api ML FINISH
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);

        _firebaseAuth = FirebaseAuth.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        mFunctions = FirebaseFunctions.getInstance();
    }
}