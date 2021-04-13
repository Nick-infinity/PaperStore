package com.nickinfinity.paperstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

public class ImagePreview extends AppCompatActivity {

    //init
    ImageView img_preview;
    Button btn_next;
    Button btn_back;
    String currentPhotoPath;
    Uri photoURI;
    ExifInterface ei;
    InputImage image = null;
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
     //   TextRecognization textRecognization = new TextRecognization();
       // System.out.println("cp"+currentPhotoPath+"\n pURI"+ photoURI);

        setPic();
        //System.out.println("ImagePreview: "+photoURI);




        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // textRecognization.recognizeText(photoURI,ImagePreview.this);
               //System.out.println("RESULT in imagepreview: "+res);
               recognizeText(photoURI,ImagePreview.this);

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();

            }
        });


    }

public void onPause()
{
    // finish when user moves to upload form screen
    super.onPause();
    finish();
}

    private void setPic() {

        // Get the dimensions of the View
        int targetW = img_preview.getWidth();
        int targetH = img_preview.getHeight();
        targetH=2129;
        targetW=1036;
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        try {
            ei = new ExifInterface(currentPhotoPath);
        }catch (Exception e)
        {

            System.out.println("unable to get image" + e);
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

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
    public void recognizeText(Uri photoURI, Context c) {


        getImage(photoURI,c);
        // [START get_detector_default]
        TextRecognizer recognizer = TextRecognition.getClient();
        // [END get_detector_default]

        // [START run_detector]
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_text]

                                res = processTextBlock(visionText);
                                if(res)
                                {
                                    Intent i = new Intent(ImagePreview.this,UploadForm.class);
                                    i.putExtra("currentPhotoPath", currentPhotoPath);
                                    i.putExtra("photoURI",photoURI.toString());
                                    startActivity(i);
                                }
                                else if(!res)
                                {
                                    Toast.makeText(getApplicationContext(),"Image Invalid",Toast.LENGTH_LONG).show();
                                    finish();
                                }

                                // System.out.println("Result in TEXTRECOG: "+res);

//                                for (Text.TextBlock block : visionText.getTextBlocks()) {
//                                    Rect boundingBox = block.getBoundingBox();
//                                    Point[] cornerPoints = block.getCornerPoints();
//                                    String text = block.getText();
//                                    for (Text.Line line: block.getLines()) {
//                                        // ...
//                                        for (Text.Element element: line.getElements()) {
//                                            // ...
//
//                                        }
//                                    }
//                                }
                                // [END get_text]
                                // [END_EXCLUDE]
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        //res=false;
                                        System.out.println("Recognition Failure");
                                    }
                                });
        // [END run_detector]

        //System.out.println("RETURN IN TEXT RECOG "+ res);
        //return res;
    }

    private boolean processTextBlock(Text result) {
        // [START mlkit_process_text_block]
        String resultText = result.getText();
        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                //  tv.append(lineText+ "\n");
                if(lineText.contains("KURUKSHETRA"))
                {
                    //   tv.append("\n"+" ACCEPTED ");
                    return true;
                }
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
//                for (Text.Element element : line.getElements()) {
//                    String elementText = element.getText();
//
//                    Point[] elementCornerPoints = element.getCornerPoints();
//                    Rect elementFrame = element.getBoundingBox();
//                }
            }
        }
        // [END mlkit_process_text_block]
        return  false;
    }

    private TextRecognizer getTextRecognizer() {
        // [START mlkit_local_doc_recognizer]
        TextRecognizer detector = TextRecognition.getClient();
        // [END mlkit_local_doc_recognizer]

        return detector;
    }
    public void getImage(Uri photoURI, Context c)
    {

        if(!photoURI.equals("")){

            try {
                this.image = InputImage.fromFilePath(c, photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ;


    }

}