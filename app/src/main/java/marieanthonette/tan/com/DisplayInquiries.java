package marieanthonette.tan.com;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DisplayInquiries extends AppCompatActivity {

    private static final String TAG = "DisplayInquiries";
    //vars
    private final ArrayList<String> mNames = new ArrayList<>();
    private final ArrayList<String> mImageUrls = new ArrayList<>();
    private final ArrayList<String> mInquirerName = new ArrayList<>();
    private final ArrayList<String> mAddress = new ArrayList<>();


    DatabaseReference mShelterInquiries;
    private FirebaseAuth mAuth;
    LinearLayout shelterListID;
    RecyclerView recyclerView;
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
        setContentView(R.layout.activity_display_inquiries);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mAuth = FirebaseAuth.getInstance();
        // REDIRECT IF NOT AUTHENTICATED
        if(mAuth.getCurrentUser() == null) {
            Intent i = new Intent(getApplicationContext(), LogIn.class);
            startActivity(i);
            finish();
        }
        LoginUserID = mAuth.getCurrentUser().getDisplayName().toLowerCase();


        mShelterInquiries = FirebaseDatabase.getInstance().getReference("shelter_inquiries").child(LoginUserID);

        recyclerView = findViewById(R.id.recyclerv_view);

        mShelterInquiries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                recyclerView.removeAllViews();

                for (DataSnapshot ss : dataSnapshot.getChildren()) {

                    final String shelterID = ss.getKey();

                    mShelterInquiries.child(shelterID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot_shelterID) {
                            //TODO DO THIS
//                            dataSnapshot_shelterID.getRef().setValue(null);

                            mNames.clear();
                            mInquirerName.clear();
                            mImageUrls.clear();
                            mAddress.clear();
                            for (DataSnapshot s2 : ss.getChildren()) {
                                final String shelterInquirer = s2.getKey();

                                ShelterRequest shelterRequest = s2.getValue(ShelterRequest.class);

                                if(!shelterRequest.getRequest()) {
                                    continue;
                                }


                                DatabaseReference sh = FirebaseDatabase.getInstance().getReference("shelter");
                                sh.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.hasChild(shelterID)) {
                                            return;
                                        }

                                        Shelter shelter_info = dataSnapshot.child(shelterID).getValue(Shelter.class);

                                        mNames.add(shelter_info.getName());
                                        mImageUrls.add(shelter_info.getLink());
                                        mInquirerName.add(shelterInquirer);
                                        mAddress.add(shelter_info.getAddress());

                                        initRecyclerView();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("DisplayInquiries", "error " + databaseError.toException());
            }
        });
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerViewAdapter_Requests adapter = new RecyclerViewAdapter_Requests(getApplicationContext(),
                mNames, mImageUrls, mInquirerName, mAddress);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
