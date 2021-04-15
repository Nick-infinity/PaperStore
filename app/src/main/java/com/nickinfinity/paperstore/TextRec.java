package com.nickinfinity.paperstore;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;

public class TextRec {
    InputImage image = null;
    Context c;
    private final String STRING_PATTERN = "KURUKSHETRA";


    public TextRec(Context context) {

        this.c = context;
    }

    public interface TextRecResponseListener {
        void onResponse(boolean res);

        void onError(String msg);

    }

    public void recognizeText(Uri photoURI, TextRecResponseListener textRecResponseListener) {
        // Getting image object with data
        getImage(photoURI, c);
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
                                textRecResponseListener.onResponse(processTextBlock(visionText));
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        textRecResponseListener.onError("Recognition Failure");

                                    }
                                });
    }

    private boolean processTextBlock(Text result) {
        // [START mlkit_process_text_block]
        for (Text.TextBlock block : result.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();

                if (lineText.contains(STRING_PATTERN)) {
                    return true;
                }
            }
        }
        // [END mlkit_process_text_block]
        return false;
    }

    private TextRecognizer getTextRecognizer() {
        // [START mlkit_local_doc_recognizer]
        TextRecognizer detector = TextRecognition.getClient();
        // [END mlkit_local_doc_recognizer]
        return detector;
    }

    public void getImage(Uri photoURI, Context c) {

        if (!photoURI.equals("")) {

            try {
                this.image = InputImage.fromFilePath(c, photoURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;


    }
}
