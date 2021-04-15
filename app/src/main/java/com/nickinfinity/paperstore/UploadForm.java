package com.nickinfinity.paperstore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashSet;

public class UploadForm extends AppCompatActivity {
    //init
    Button btn_upload;
    ImageView img_upload;
    EditText editText_Branch;
    EditText editText_Semester;
    EditText editText_Subject;
    String currentPhotoPath;
    Uri photoURI;
    ExifInterface ei;
    String branch;
    String semester;
    String subject;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_form);

        // set
        View content = findViewById(android.R.id.content);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        currentPhotoPath = getIntent().getStringExtra("currentPhotoPath");
        photoURI = Uri.parse(getIntent().getStringExtra("photoURI"));
        btn_upload = findViewById(R.id.btn_upload);
        img_upload = findViewById(R.id.img_upload_preveiw);
        editText_Branch = findViewById(R.id.editText_branch);
        editText_Semester = findViewById(R.id.editText_Semester);
        editText_Subject = findViewById(R.id.editText_subject);

        Fire fire = new Fire(this);

        setPic();
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                branch = editText_Branch.getText().toString().toLowerCase();
                semester = editText_Semester.getText().toString();
                subject = editText_Subject.getText().toString().toLowerCase();

                if (isValid(branch, semester, subject)) {


                    fire.uploadPicture(photoURI, content, branch, semester, subject, new Fire.FireResponseListener() {
                        public void onSuccessResponse() {
                            clearFields();
                            finish();
                            Toast.makeText(getApplicationContext(), "Upload Successful", Toast.LENGTH_LONG).show();
                        }
                    });

                }


            }
        });


    }

//    public void refresh() {
//        Intent intent = getIntent();
//        overridePendingTransition(0, 0);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//        finish();
//        overridePendingTransition(0, 0);
//        startActivity(intent);
//    }

    private void clearFields() {
        editText_Subject.setText("");
        editText_Semester.setText("");
        editText_Branch.setText("");
        branch = "";
        semester = "";
        subject = "";
    }

    private boolean isValid(String branch, String semester, String subject) {
        int semInt;
        try {
            semInt = Integer.valueOf(semester);
        } catch (Exception e) {
            editText_Semester.setText("");
            Toast.makeText(getApplicationContext(), "Enter Semester between 1-8", Toast.LENGTH_SHORT).show();
            return false;
        }
        HashSet<String> branches = new HashSet<>();
        branches.add("it");
        branches.add("civil");
        branches.add("electronics");
        branches.add("electrical");
        branches.add("production");
        branches.add("computer");
        branches.add("mechanical");
        branches.add("cse");
        branches.add("ee");
        branches.add("ece");
        branches.add("me");
        branches.add("pie");
        branches.add("ce");
        if (!branches.contains(branch.replaceAll(" ", ""))) {
            editText_Branch.setText("");
            branch = "";
            Toast.makeText(getApplicationContext(), "Invalid Branch", Toast.LENGTH_SHORT).show();
            branches.clear();
            return false;
        } else if (semInt < 1 || semInt > 8) {
            editText_Semester.setText("");
            semester = "";
            Toast.makeText(getApplicationContext(), "Enter Semester between 1-8", Toast.LENGTH_SHORT).show();
            return false;
        } else if (subject.replaceAll(" ", "").equals("")) {
            editText_Subject.setText("");
            subject = "";
            Toast.makeText(getApplicationContext(), "Invalid Subject Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        branch = branch.replaceAll(" ", "");
        subject = subject.replaceAll(" ", "");
        semester = semester.replaceAll(" ", "");
        branch = branch.toLowerCase();
        subject = subject.toLowerCase();
        return true;
    }

    private void setPic() {

        // Get the dimensions of the View
        int targetW = img_upload.getWidth();
        int targetH = img_upload.getHeight();
        targetH = 800;
        targetW = 400;
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
            System.out.println("unable to get image");
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
        img_upload.setImageBitmap(rotatedBitmap);
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


}