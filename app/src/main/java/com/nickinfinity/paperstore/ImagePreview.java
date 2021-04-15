package com.nickinfinity.paperstore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ImagePreview extends AppCompatActivity {

    //init
    ImageView img_preview;
    Button btn_next;
    Button btn_back;
    String currentPhotoPath;
    Uri photoURI;
    ExifInterface ei;
    boolean res;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        //set
        img_preview = findViewById(R.id.imgpreview);
        btn_next = findViewById(R.id.btn_next);
        btn_back = findViewById(R.id.btn_back);
        currentPhotoPath = getIntent().getStringExtra("currentPhotoPath");
        photoURI = Uri.parse(getIntent().getStringExtra("photoURI"));
        TextRec textRec = new TextRec(this);
        // System.out.println("cp"+currentPhotoPath+"\n pURI"+ photoURI);

        setPic();
        //System.out.println("ImagePreview: "+photoURI);


        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textRec.recognizeText(photoURI, new TextRec.TextRecResponseListener() {
                    @Override
                    public void onResponse(boolean res) {
                        if (res) {
                            Intent i = new Intent(ImagePreview.this, UploadForm.class);
                            i.putExtra("currentPhotoPath", currentPhotoPath);
                            i.putExtra("photoURI", photoURI.toString());
                            startActivity(i);

                        } else {
                            Toast.makeText(getApplicationContext(), "Image Invalid", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        Toast.makeText(getApplicationContext(), msg + " Please Try Again", Toast.LENGTH_LONG).show();

                    }
                });
                //System.out.println("RESULT in imagepreview: "+res);
                //   recognizeText(photoURI,ImagePreview.this);

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });


    }

    public void onPause() {
        // finish when user moves to upload form screen
        super.onPause();
        finish();
    }

    private void setPic() {

        // Get the dimensions of the View
        int targetW = img_preview.getWidth();
        int targetH = img_preview.getHeight();
        targetH = 2129;
        targetW = 1036;
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        try {
            ei = new ExifInterface(currentPhotoPath);
        } catch (Exception e) {

            System.out.println("unable to get image" + e);
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;

        }
        img_preview.setImageBitmap(rotatedBitmap);
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


}