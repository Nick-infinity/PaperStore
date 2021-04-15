package com.nickinfinity.paperstore;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    //Initialise
    Button btn_capture;
    Button btn_gallery;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;
    String currentPhotoPath;
    Uri photoURI;
    static int REQUEST_CODE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set
        btn_capture = findViewById(R.id.btn_capture);
        btn_gallery = findViewById(R.id.btn_gallery);


        currentPhotoPath = "";
        photoURI = null;

        checkPermissions();
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dispatchTakePictureIntent();


            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                gallery.setType("image/*");
                // gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery, PICK_IMAGE);
                //startActivityForResult(Intent.createChooser(gallery,"Select Picture"),PICK_IMAGE);


            }
        });


    }

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Please Grant Permissions");
                    builder.setMessage("Camera, Access Storage");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, REQUEST_CODE
                            );
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, REQUEST_CODE
                    );

                }
            } else {
                // Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
            }

        } else {

            //Permissions are auto granted below marshmallow
            Toast.makeText(MainActivity.this, "Permissions Granted", Toast.LENGTH_SHORT).show();
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            photoURI = data.getData();
            currentPhotoPath = getPathFromURI(photoURI);

            if (currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                photoURI = Uri.fromFile(f);
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {


            //  Bundle extras = data.getExtras();

            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);

            // img.setImageBitmap(imageBitmap);
            // setPic();

        } else if (resultCode == RESULT_CANCELED) {
            return;
        }
        Intent i = new Intent(MainActivity.this, ImagePreview.class);
        System.out.println("currentPhotoPath MAIN " + currentPhotoPath);
        System.out.println("photoURI MAIN " + photoURI.toString());
        i.putExtra("currentPhotoPath", currentPhotoPath);
        i.putExtra("photoURI", photoURI.toString());
        startActivity(i);

    }

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        // System.out.println(currentPhotoPath);

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.nickinfinity.paperstore",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    System.out.println(e + "error");
                }
            }
        }
    }

    //    private void galleryAddPic() {
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        File f = new File(currentPhotoPath);
//        Uri contentUri = Uri.fromFile(f);
//        mediaScanIntent.setData(contentUri);
//        this.sendBroadcast(mediaScanIntent);
//    }


}