package marieanthonette.tan.com;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DisplaySingleShelter extends AppCompatActivity {

    private FirebaseAuth mAuth;

    DatabaseReference shelter, shelter_request;
    StorageReference mStorage;
    Button bRequest, bViewMap;

    TextView eName,
            eAddress,
            eCapacity,
            eDays,
            eOwner,
            eAvailable;

    ImageView eHeader;

    ValueEventListener mListener;

    String LoginUserID = "";
    String item_key, address;
    String shelter_key,
            shelter_userID,
            shelter_name,
            shelter_address,
            shelter_capacity,
            shelter_days,
            shelter_user_id,
            shelter_image_link;
    Boolean shelter_available;

    Boolean isViewMapClicked;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        if (mListener != null && shelter != null) {
            shelter.removeEventListener(mListener);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_single_shelter);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        shelter = FirebaseDatabase.getInstance().getReference("shelter");
        shelter_request = FirebaseDatabase.getInstance().getReference("shelter_request");

        mAuth = FirebaseAuth.getInstance();
        // REDIRECT IF NOT AUTHENTICATED
        if(mAuth.getCurrentUser() == null) {
            Intent i = new Intent(getApplicationContext(), LogIn.class);
            startActivity(i);
            finish();
        }
        LoginUserID = mAuth.getCurrentUser().getDisplayName();

        mStorage = FirebaseStorage.getInstance().getReference();

        eName = findViewById(R.id.vName);
        eAddress = findViewById(R.id.vAddress);
        eHeader = findViewById(R.id.vHeader);
        eCapacity = findViewById(R.id.vCapacity);
        eDays = findViewById(R.id.vDays);
        eOwner = findViewById(R.id.vOwner);
        eAvailable = findViewById(R.id.vAvailable);

        bViewMap = findViewById(R.id.viewMap);
        bRequest = findViewById(R.id.addRequest);

    } // END ONCREATE

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        item_key = intent.getStringExtra("key");
//        item_key = "-LSlttM6jCfmPgm3jt5B";
        shelter_userID = "";

        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Shelter sh = dataSnapshot.child(item_key).getValue(Shelter.class);
                shelter_key = dataSnapshot.child(item_key).getKey();

                shelter_name = sh.getName();
                shelter_address = sh.getAddress();
                shelter_capacity = sh.getCapacity();
                shelter_days = sh.getDays();
                shelter_user_id = sh.getUserID();
                shelter_image_link = sh.getLink();
                shelter_available = sh.getAvailable();

                // view map
                address = shelter_address;
                shelter_userID = shelter_user_id;

                eName.setText(shelter_name);

                String field_address = "Address: " + shelter_address;
                String field_max_capacity_allowed = "Max Capacity Allowed: " + shelter_capacity;
                String field_max_days_allowed = "Max Days Allowed: " + shelter_days;
                String field_owner = "Owner: " + shelter_user_id;

                eAddress.setText(field_address);
                eCapacity.setText(field_max_capacity_allowed);
                eDays.setText(field_max_days_allowed);
                eOwner.setText(field_owner);
                eAvailable.setText("Availability: " + (shelter_available ? "available" : "not-available"));

                StorageReference storageReference = mStorage.child(shelter_image_link);

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(uri.toString())
                                .into(eHeader);
                    }
                });


                // SET DEFAULTS
                isViewMapClicked = false;

                if(isOwnerOfPost()) {
                    setRequestBtnToEditBtn();
                }
                else if(!shelter_available) {
                    setRequestBtnToUnavailable();
                }
                else {
                    setViewMapButtonStatus();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        shelter.addValueEventListener(mListener);

        setButtonViewMapDefaults();
        setButtonRequestDefaults();
    }

    public void setButtonViewMapDefaults() {
        bViewMap.setBackground(getResources().getDrawable(R.drawable.btn_radius_green));
        bViewMap.setTextColor(getResources().getColor(R.color.white));
    }

    public void setButtonRequestDefaults() {
        bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius));
        bRequest.setTextColor(getResources().getColor(R.color.white));
    }

    public void setRequestBtnToUnavailable() {
        bRequest.setText("Unavailable");
        bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius_grey));
    }

    public void setRequestBtnToEditBtn() {
        bRequest.setText("Edit");
        bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius));
    }

    public boolean isOwnerOfPost() {
        return LoginUserID.equals(shelter_userID);
    }

    public void setViewMapButtonStatus() {

        shelter_request.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child(item_key).child(LoginUserID).exists())
                    return;

                ShelterRequest sr = dataSnapshot.child(item_key).child(LoginUserID).getValue(ShelterRequest.class);
                Boolean isClicked = sr.getRequest();

                if (isClicked) {
                    isViewMapClicked = true;
                    bRequest.setText("INQUIRY SENT");
                    bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius_active));
                } else {
                    isViewMapClicked = false;
                    bRequest.setText("SEND INQUIRY");
                    setButtonRequestDefaults();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void visitMap(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("geo:0, 0?q=" + Uri.encode(address)));
        Intent chooser = Intent.createChooser(i, "Choose an app");
        startActivity(chooser);
    }

    public void addRequest(View v) {
        // SHOULD DISPLAY EDIT SHELTER
        if(isOwnerOfPost()) {
            Intent intent = new Intent(DisplaySingleShelter.this, AddShelter.class);
            intent.putExtra("action", "edit");
            intent.putExtra("shelter_key", shelter_key);
            intent.putExtra("shelter_name", shelter_name);
            intent.putExtra("shelter_address", shelter_address);
            intent.putExtra("shelter_capacity", shelter_capacity);
            intent.putExtra("shelter_days", shelter_days);
            intent.putExtra("shelter_image_link", shelter_image_link);
            intent.putExtra("shelter_isAvailable", shelter_available + "");
            startActivity(intent);
        }

        else if(!shelter_available) {
            Toast("Unavailable...");
        }

        else {
            if(isViewMapClicked)
                Toast("Inquiry succesfully removed...");
            else
                Toast("Inquiry successfully sent...");
            shelter_request.child(item_key).child(LoginUserID).setValue(new ShelterRequest(!isViewMapClicked));
        }
    }

    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
