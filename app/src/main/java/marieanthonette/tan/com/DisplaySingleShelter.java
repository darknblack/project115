package marieanthonette.tan.com;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
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

    DatabaseReference shelter;
    StorageReference mStorage;

    TextView eName, eAddress, eCapacity, eDays;
    ImageView eHeader;


    ValueEventListener mListener;

    String address;

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

        shelter = FirebaseDatabase.getInstance().getReference("shelter");
        mStorage = FirebaseStorage.getInstance().getReference();

        eName = findViewById(R.id.vName);
        eAddress = findViewById(R.id.vAddress);
        eHeader = findViewById(R.id.vHeader);
        eCapacity = findViewById(R.id.vCapacity);
        eDays = findViewById(R.id.vDays);

        Intent intent = getIntent();
        final String key = intent.getStringExtra("key");


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Shelter sh = dataSnapshot.child(key).getValue(Shelter.class);

                String _name, _address, _capacity, _days;

                _name = sh.getName();
                _address = sh.getAddress();
                _capacity = sh.getCapacity();
                _days = sh.getDays();

                // view map
                address = _address;

                eName.setText(_name);
                eAddress.setText("Address: " + _address);
                eCapacity.setText("Max Capacity Allowed: " + _capacity);
                eDays.setText("Max Days Allowed: " + _days);

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
    }

    public void visitMap(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("geo:0, 0?q=" + Uri.encode(address)));
        Intent chooser = Intent.createChooser(i, "Choose an app");
        startActivity(chooser);
    }


}
