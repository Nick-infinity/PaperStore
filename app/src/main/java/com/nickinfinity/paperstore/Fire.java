package com.nickinfinity.paperstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Fire  extends Intent {

    private FirebaseStorage storage;
    private StorageReference storageReference;


    public  Fire()
    {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

    }


     public void  uploadPicture(Uri photoURI, View view, Context context, Context pdContext , String branch, String semester, String subject)
     {
        switch (branch)
        {
            case "ece":
            {
                branch="electronics";
                break;
            }
            case "ee":
            {
                branch="electrical";
                break;
            }
            case "me":
            {
                branch="mechanical";
                break;
            }
            case "pie":
            {
            branch="production";
            break;
             }
            case "cse":
            {
                branch="computer";
                break;
            }
            case "ce":
            {
                branch="civil";
                break;
            }
        }
        final ProgressDialog pd = new ProgressDialog(pdContext);
        pd.setTitle("Image Uploading...");

        pd.show();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String path = branch+"/"+semester+"/"+subject+"/"+"IMG_"+subject.toUpperCase()+"_"+timeStamp+".jpg";
        StorageReference reference =storageReference.child(path);
        reference.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Snackbar.make(view,"Image Uploaded",Snackbar.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context,"Failed to Upload",Toast.LENGTH_LONG).show();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercentage = (100.00 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                pd.setMessage("Progress: "+ (int) progressPercentage+"%");
            }
        });
    }
}
