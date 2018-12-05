package marieanthonette.tan.com;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class AddShelter extends AppCompatActivity {

//    FirebaseDatabase db;
    DatabaseReference shelter;

    EditText eName, eAddress, eCapacity, eDays;

    private ImageView mSelectImage;
    private StorageReference mStorage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog mProgressDialog;

    StorageReference imagePath;
    Uri imageUri;
    String imageExtension;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shelter);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mProgressDialog = new ProgressDialog(this);

        // FIREBASE
        shelter = FirebaseDatabase.getInstance().getReference("shelter");
        mStorage = FirebaseStorage.getInstance().getReference("images");

        // FIELDS
        eName = findViewById(R.id.etName);
        eAddress = findViewById(R.id.etAddress);
        mSelectImage = findViewById(R.id.selectImage);
        eCapacity = findViewById(R.id.etCapacity);
        eDays = findViewById(R.id.etDays);

        // ONCLICK | IMAGE
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
    }

    // ACTIVITY RESULT
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            imageUri = data.getData();
            File file = new File(imageUri.getPath());
            imageExtension = getFileExtension(file);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mSelectImage.setImageBitmap(bitmap);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }


    // GET FILE EXTENSION
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    // ADD RECORD
    public void addRecord(View v) {
        // IF IMAGE IS NOT NULL
        if(imageExtension != null) {
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.show();

            // SET ITEM
            String key = shelter.push().getKey();
            String name = beautifyTextField(eName);
            String address = beautifyTextField(eAddress);
            String days = beautifyTextField(eDays);
            String capacity = beautifyTextField(eCapacity);

            imagePath = mStorage.child(key + imageExtension);

            // UPLOAD IMAGE
            imagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast("Error in uploading...");
                    mProgressDialog.dismiss();
                }
            });


            shelter.child(key).setValue(new Shelter(name, address, imagePath.getPath(), days, capacity));
        }
    }


    /* HELPER METHODS */
    protected String beautifyTextField(EditText et) {
        return et.getText().toString().trim();
    }

    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
