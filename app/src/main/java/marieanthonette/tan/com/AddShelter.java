package marieanthonette.tan.com;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class AddShelter extends AppCompatActivity {

    private FirebaseAuth mAuth;

    DatabaseReference shelter;
    DatabaseReference mShelterInquiries;
    StorageReference imagePath;
    StorageReference mStorage;

    EditText eName, eAddress, eCapacity, eDays;

    Button mAddEditBtn, mRemoveBtn;
    CheckBox mAvailableCheckBox;

    private ImageView mSelectImage;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog mProgressDialog;

    ScrollView mAddShelterScrollView;


    Uri imageUri;
    String imageExtension;
    String LoginUserID = "";


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
        mShelterInquiries = FirebaseDatabase.getInstance().getReference("shelter_inquiries");

        mAuth = FirebaseAuth.getInstance();
        // REDIRECT IF NOT AUTHENTICATED
        if(mAuth.getCurrentUser() == null) {
            Intent i = new Intent(getApplicationContext(), LogIn.class);
            startActivity(i);
            finish();
        }
        LoginUserID = mAuth.getCurrentUser().getDisplayName().toLowerCase();

        // FIELDS
        eName = findViewById(R.id.etName);
        eAddress = findViewById(R.id.etAddress);
        mSelectImage = findViewById(R.id.selectImage);
        eCapacity = findViewById(R.id.etCapacity);
        eDays = findViewById(R.id.etDays);
        mAddEditBtn = findViewById(R.id.addEditBtn);
        mRemoveBtn = findViewById(R.id.removeBtn);
        mAddShelterScrollView = findViewById(R.id.addShelterScrollView);
        mAvailableCheckBox = findViewById(R.id.availableCheckBox);

        // ONCLICK | IMAGE
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        mRemoveBtn.setVisibility(View.GONE);

        Intent intent = getIntent();

        boolean doEdit = intent.getExtras() != null;

        // IF EXISTING THEN EDIT (COMING FROM DisplayShelters.java)

        if(doEdit) {
            String shelter_name = intent.getStringExtra("shelter_name");
            String shelter_address = intent.getStringExtra("shelter_address");
            String shelter_capacity = intent.getStringExtra("shelter_capacity");
            String shelter_days = intent.getStringExtra("shelter_days");
            String shelter_image_link = intent.getStringExtra("shelter_image_link");
            Boolean shelter_isAvailable = Boolean.parseBoolean(intent.getStringExtra("shelter_isAvailable"));

            this.setTitle("EDIT");
            eName.setText(shelter_name);
            eAddress.setText(shelter_address);
            eCapacity.setText(shelter_capacity);
            eDays.setText(shelter_days);
            mAvailableCheckBox.setChecked(shelter_isAvailable);

            mAddEditBtn.setText("EDIT");

            mRemoveBtn.setVisibility(View.VISIBLE);


            // GET THE APPROPRIATE IMAGE ACCORDING TO IMAGE LINK
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(shelter_image_link);
            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(uri.toString())
                            .into(mSelectImage);
                }
            });
        }



        // DEBUG HERE
    }

    public void log(String message) {
        Log.d("keys", message);
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
    public void addOrEditRecord(Boolean isNewRecord, String existingShelterKey, String existingFileName) {
        // IF IMAGE IS NOT NULL
        if(imageExtension != null || isNewRecord == false) {
            // SET ITEM
            String key = shelter.push().getKey();
            String imageFileName = key + imageExtension;

            if(!isNewRecord) {
                key = existingShelterKey;
                imageFileName = existingFileName;
            }

            String name = beautifyTextField(eName);
            String address = beautifyTextField(eAddress);
            String days = beautifyTextField(eDays);
            String capacity = beautifyTextField(eCapacity);

            String imageFilePath = "";

            // IF USER SELECTED AN IMAGE
            // ELSE USE THE EXISTING (DO NOTHING
            if(imageExtension != null) {
                mProgressDialog.setMessage("Uploading...");
                mProgressDialog.show();

                imagePath = mStorage.child(imageFileName);
                // UPLOAD IMAGE
                imagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressDialog.dismiss();
                        Toast("Upload successful...");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast("Error in uploading...");
                    }
                });

                imageFilePath = imagePath.getPath();

            } else {
                imageFilePath = existingFileName;
            }

            shelter.child(key).setValue(new Shelter(name, address, imageFilePath, days, capacity, LoginUserID, mAvailableCheckBox.isChecked()));

            if(imageExtension == null) {
                Toast("Record updated ...");
            }

        }

    }

    public void deleteShelter(View v) {
        Intent intent = getIntent();
        final String shelter_key = intent.getStringExtra("shelter_key");
        final String shelter_image_link = intent.getStringExtra("shelter_image_link");

        // ADD YES OR NO
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // IF ANSWER IS YES
                if(which == DialogInterface.BUTTON_POSITIVE) {

                    StorageReference storageReference = mStorage.getParent().child(shelter_image_link);
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            shelter.child(shelter_key).setValue(null);

                            mShelterInquiries.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot ds1 : dataSnapshot.getChildren()) {
                                        String shelter_inquirer = ds1.getKey();
                                        for(DataSnapshot ds2 : ds1.getChildren()) {
                                            String shelter_id = ds2.getKey();
                                            if(shelter_id.equals(shelter_key))
                                                mShelterInquiries.child(shelter_inquirer).child(shelter_id).setValue(null);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });


                            Toast("Delete Succesful...");
                            navigateUpTo(new Intent(getBaseContext(), DisplayShelters.class));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast("Error in deleting...");
                        }
                    });
                } // FOR NO ->  DialogInterface.BUTTON_NEGATIVE
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddShelter.this);
        builder.setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void toAddOrEdit(View v) {
        final Intent intent = getIntent();
        final Boolean doAdd = intent.getExtras() == null; // add or edit

        // ADD YES OR NO
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // PERFORM ACTION IF YES
                if(which == DialogInterface.BUTTON_POSITIVE) {

                    // ADD NEW RECORD
                    if(doAdd) {
                        addOrEditRecord(true, null, null);
                    }

                    // UPDATE RECORD
                    else {
                        String shelter_key = intent.getStringExtra("shelter_key");
                        String shelter_image_link = intent.getStringExtra("shelter_image_link");

                        addOrEditRecord(false, shelter_key, shelter_image_link);
                    }
                }

                // DO NOTHING IF NO
            }
        };

        String message = "Are you sure you want to " + (doAdd ? "add" : "edit")  + " this?";

        AlertDialog.Builder builder = new AlertDialog.Builder(AddShelter.this);
        builder.setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    /* HELPER METHODS */
    protected String beautifyTextField(EditText et) {
        return et.getText().toString().trim();
    }

    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
