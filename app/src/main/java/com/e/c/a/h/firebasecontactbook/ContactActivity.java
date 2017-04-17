package com.e.c.a.h.firebasecontactbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.e.c.a.h.firebasecontactbook.data.Contact;
import com.e.c.a.h.firebasecontactbook.listener.EditTextDatePickerClickListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

@SuppressWarnings("VisibleForTests")

public class ContactActivity extends AppCompatActivity {
    FirebaseDatabase db;
    DatabaseReference dbRef;

    ImageView imageView;
    EditText editTextName;
    EditText editTextBirthDate;
    Button buttonSave;

    File picFile;

    private boolean picLocallyLoaded = false;

    public static final String EDIT_CONTACT = "EDIT_CONTACT";
    Contact contact;

    private final int CAM_REQST_ID = 222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        picFile = new File(MainActivity.folder, "tmp.jpg");

        db = FirebaseDatabase.getInstance();
        dbRef = db.getReference("contact_book");

        imageView = (ImageView) findViewById(R.id.imageView);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextBirthDate = (EditText) findViewById(R.id.editTextBirthDate);
        buttonSave = (Button) findViewById(R.id.buttonSave);

        editTextBirthDate.setOnClickListener(new EditTextDatePickerClickListener(this, R.id.editTextBirthDate));

        contact = getIntent().getParcelableExtra(EDIT_CONTACT);

        if(contact != null) {
            if(contact.getImageURL() != null) {
                Glide.with(this).load(contact.getImageURL()).into(imageView);
                picLocallyLoaded = true;
            }
            editTextName.setText(contact.getName());
            editTextBirthDate.setText(contact.getBirthDate());
        }

        TextWatcher textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                formChaged();
            }
        };

        editTextName.addTextChangedListener(textChangedListener);
        editTextBirthDate.addTextChangedListener(textChangedListener);
    }

    private void formChaged() {
        if(picLocallyLoaded
                && editTextName.getText().toString().trim().length() > 0
                && editTextBirthDate.getText().toString().trim().length() > 0 ) {
            buttonSave.setEnabled(true);
        } else {
            buttonSave.setEnabled(false);
        }
    }

    public void saveContactCL(View view) {
        if(contact == null) {
            contact = new Contact(null, null, null, null, null);

            contact.setUid(dbRef.push().getKey());
        }
        contact.setName(editTextName.getText().toString());
        contact.setBirthDate(editTextBirthDate.getText().toString());

        StorageReference picReference = FirebaseStorage.getInstance().getReference("contact_book").child(contact.getUid());

        picReference.putFile(Uri.fromFile(picFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadURL = taskSnapshot.getDownloadUrl();
                contact.setImageURL(downloadURL.toString());

                dbRef.child(contact.getUid()).setValue(contact);

                finish();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.editTextName), "Couldn't save the pic to firebase... " + e.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    public void takeContactPictureCL(View view) {
        if(picFile.exists()) {
            picFile.delete();
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));

        startActivityForResult(cameraIntent, CAM_REQST_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAM_REQST_ID:
                picLocallyLoaded = true;
                imageView.setImageURI(null);
                imageView.setImageURI(Uri.parse(picFile.toString()));
                formChaged();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }
}
