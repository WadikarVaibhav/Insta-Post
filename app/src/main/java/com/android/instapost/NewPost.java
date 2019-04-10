package com.android.instapost;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class NewPost extends Fragment {

    public static final int REQUEST_CODE_CAMERA_UPLOAD = 1;
    public static final int REQUEST_CODE_GALLERY_UPLOAD = 2;
    AlertDialog alertDialog;
    private static final double IMAGE_SIZE = 1000000.0;
    private static final double MAX_IMAGE_SIZE = 5.0;
    private byte[] bytes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_newpost, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Picture");
        builder.setItems(R.array.upload_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        clickPictureAndUpload();
                        break;
                    case 1:
                        selectPictureFromGallery();
                        break;
                    default:
                        break;
                }
            }
        });
        if (alertDialog == null) {
            alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void clickPictureAndUpload() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA_UPLOAD);
    }

    private void selectPictureFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY_UPLOAD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA_UPLOAD && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            uploadNewImage(bitmap);
        } else if (requestCode == REQUEST_CODE_GALLERY_UPLOAD && resultCode == Activity.RESULT_OK) {
            Uri selectedImgUri = data.getData();
            uploadNewImage(selectedImgUri);
        }
    }

    private void uploadNewImage(Uri selectedImgUri) {
        if (selectedImgUri != null) {
            BackgroundImageResize resize = new BackgroundImageResize(null);
            resize.execute(selectedImgUri);
        }
    }

    private void uploadNewImage(Bitmap imageBitmap) {
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void uploadImageOnCloud() {
        ImageFilePath imageFilePath = new ImageFilePath();
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(imageFilePath.FIREBASE_IMAGE_STORAGE + "/" + UUID.randomUUID().toString());
        if (bytes.length / IMAGE_SIZE < MAX_IMAGE_SIZE) {
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg").setContentLanguage("en")
                    .build();
            UploadTask uploadTask = storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("success", "image upload");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), "could not upload photo", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap bitmap;

        public BackgroundImageResize(Bitmap bitmap) {
            if (bitmap != null) {
                this.bitmap = bitmap;
            }
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            if (bitmap == null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), params[0]);
                } catch (IOException e) {
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++) {
                if (i == 10) {
                    break;
                }
                bytes = getBytesFromBitmap(bitmap, 100 / i);
                if (bytes.length / IMAGE_SIZE < MAX_IMAGE_SIZE) {
                    return bytes;
                }
            }
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            NewPost.this.bytes = bytes;
            uploadImageOnCloud();
        }
    }


}
