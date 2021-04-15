package com.nickinfinity.paperstore;

import android.Manifest;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UploadScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadScreen extends Fragment {

    //Initialise
    Button btn_capture;
    Button btn_gallery;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE = 2;
    String currentPhotoPath;
    Uri photoURI;
    static int REQUEST_CODE = 123;
    View view;

    public UploadScreen() {
        // Required empty public constructor
    }


    public static UploadScreen newInstance() {
        UploadScreen fragment = new UploadScreen();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_upload_screen, container, false);

        //set
        btn_capture = view.findViewById(R.id.btn_capture);
        btn_gallery = view.findViewById(R.id.btn_gallery);

        currentPhotoPath = "";
        photoURI = null;
        askPermissions();
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    dispatchTakePictureIntent();
                } else {
                    askPermissions();
                    Toast.makeText(getActivity(), "Please grant required permissions", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    gallery.setType("image/*");
                    // gallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(gallery, PICK_IMAGE);
                    //startActivityForResult(Intent.createChooser(gallery,"Select Picture"),PICK_IMAGE);
                } else {
                    askPermissions();
                    Toast.makeText(getActivity(), "Please grant required permissions", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return view;
    }

    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Please Grant Permissions");
                    builder.setMessage("Camera & Storage permissions are required for this app to function properly");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                            Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, REQUEST_CODE
                            );
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                            }, REQUEST_CODE
                    );

                }
            } else {
                // Toast.makeText(getActivity(), "Permissions Granted", Toast.LENGTH_SHORT).show();

            }

        } else {

            //Permissions are auto granted below marshmallow
            Toast.makeText(getActivity(), "Permissions Granted", Toast.LENGTH_SHORT).show();
        }
    }

    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == getActivity().RESULT_OK) {
            photoURI = data.getData();
            currentPhotoPath = getPathFromURI(photoURI);

            if (currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                photoURI = Uri.fromFile(f);
            }
        } else if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {


            //  Bundle extras = data.getExtras();

            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);

            // img.setImageBitmap(imageBitmap);
            // setPic();

        } else if (resultCode == getActivity().RESULT_CANCELED) {
            return;
        }
        Intent i = new Intent(getActivity(), ImagePreview.class);
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getActivity(),
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
}