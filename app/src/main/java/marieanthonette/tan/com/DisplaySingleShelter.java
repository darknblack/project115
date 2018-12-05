package marieanthonette.tan.com;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class DisplaySingleShelter extends AppCompatActivity {

    DatabaseReference shelter, shelter_request;
    StorageReference mStorage;
    Button bRequest, bViewMap;

    TextView eName, eAddress, eCapacity, eDays;
    ImageView eHeader;

    ValueEventListener mListener;

    String user_id, item_key, address;

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

        mStorage = FirebaseStorage.getInstance().getReference();

        eName = findViewById(R.id.vName);
        eAddress = findViewById(R.id.vAddress);
        eHeader = findViewById(R.id.vHeader);
        eCapacity = findViewById(R.id.vCapacity);
        eDays = findViewById(R.id.vDays);

        bViewMap = findViewById(R.id.viewMap);
        bRequest = findViewById(R.id.addRequest);

        Intent intent = getIntent();
        item_key = intent.getStringExtra("key");
//        item_key = "-LSlttM6jCfmPgm3jt5B";
        user_id = "IANODERON";

        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Shelter sh = dataSnapshot.child(item_key).getValue(Shelter.class);

                String shelter_name, shelter_address, shelter_capacity, shelter_days;

                shelter_name = sh.getName();
                shelter_address = sh.getAddress();
                shelter_capacity = sh.getCapacity();
                shelter_days = sh.getDays();

                // view map
                address = shelter_address;

                eName.setText(shelter_name);

                String field_address = "Address: " + shelter_address;
                String field_max_capacity_allowed = "Max Capacity Allowed: " + shelter_capacity;
                String field_max_days_allowed = "Max Days Allowed: " + shelter_days;

                eAddress.setText(field_address);
                eCapacity.setText(field_max_capacity_allowed);
                eDays.setText(field_max_days_allowed);

                StorageReference storageReference = mStorage.child(sh.getLink());

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.with(DisplaySingleShelter.this).load(uri.toString()).into(eHeader);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        shelter.addValueEventListener(mListener);


        // SET DEFAULTS
        isViewMapClicked = false;

        bViewMap.setBackground(getResources().getDrawable(R.drawable.btn_radius));
        bViewMap.setTextColor(getResources().getColor(R.color.white));

        setViewMapButtonDefaults();
        setViewMapButtonStatus();

    } // END ONCREATE

    public void setViewMapButtonDefaults() {
        bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius));
        bRequest.setTextColor(getResources().getColor(R.color.white));
    }

    public void setViewMapButtonStatus() {

        shelter_request.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child(user_id).child(item_key).exists())
                    return;

                ShelterRequest sr = dataSnapshot.child(user_id).child(item_key).getValue(ShelterRequest.class);
                Boolean isClicked = sr.getRequest();

                if(isClicked) {
                    isViewMapClicked = true;
                    bRequest.setText("REQUESTED");
                    bRequest.setBackground(getResources().getDrawable(R.drawable.btn_radius_active));
                } else {
                    isViewMapClicked = false;
                    bRequest.setText("REQUEST");
                    setViewMapButtonDefaults();
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
        shelter_request.child(user_id).child(item_key).setValue(new ShelterRequest(!isViewMapClicked));
    }

    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
