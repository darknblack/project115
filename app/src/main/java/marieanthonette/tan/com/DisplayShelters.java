package marieanthonette.tan.com;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DisplayShelters extends AppCompatActivity {

    DatabaseReference mShelter;


    EditText mSearchField;

    List<Shelter> sheltersArray;
    LinearLayout shelterListID;
    float scale;

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
        setContentView(R.layout.activity_display_shelter);

        mShelter = FirebaseDatabase.getInstance().getReference("shelter");
        sheltersArray = new ArrayList<Shelter>();

        scale = getResources().getDisplayMetrics().density;

        shelterListID = findViewById(R.id.shelterLists);
        mSearchField = findViewById(R.id.searchField);


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                searchShelter();
            }
        });
    }

    public void searchShelter() {
        String querySearch = mSearchField.getText().toString().trim().toLowerCase();
        mShelter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shelterListID.removeAllViews();

                ArrayList<Shelter> shelters_list = new ArrayList<Shelter>();
                ArrayList<String> keys = new ArrayList<String>();

                for(DataSnapshot ss : dataSnapshot.getChildren()) {
                    Shelter shelter = ss.getValue(Shelter.class);
                    String key = ss.getKey();

                    String shelter_name = shelter.getName().toLowerCase();
                    String shelter_address = shelter.getAddress().toLowerCase();

                    if(!(shelter_name.contains(querySearch) || shelter_address.contains(querySearch))) {
                        continue;
                    }


                    shelters_list.add(0, shelter);
                    keys.add(0, key);
                }

//                Collections.reverse(shelters_list);
//                Collections.reverse(keys);

                for(int i = 0; i < shelters_list.size(); i++){
                    displayShelter(shelters_list.get(i),keys.get(i));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("hackdog", "error " + databaseError.toException());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mShelter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shelterListID.removeAllViews();

                ArrayList<Shelter> shelters_list = new ArrayList<Shelter>();
                ArrayList<String> keys = new ArrayList<String>();

                for(DataSnapshot ss : dataSnapshot.getChildren()) {
                    Shelter shelter = ss.getValue(Shelter.class);
                    String key = ss.getKey();

                    shelters_list.add(0, shelter);
                    keys.add(0, key);
                }

//                Collections.reverse(shelters_list);
//                Collections.reverse(keys);

                for(int i = 0; i < shelters_list.size(); i++){
                    displayShelter(shelters_list.get(i),keys.get(i));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("hackdog", "error " + databaseError.toException());
            }
        });
    }

    protected void displayShelter(Shelter shelter, String key) {
        final String shelter_name = shelter.getName();
        final String shelter_address = shelter.getAddress();
        final String shelter_image_link = shelter.getLink();

        final ImageView imageView = new ImageView(
                new ContextThemeWrapper(DisplayShelters.this, R.style.CardImage), null, 0);

        LinearLayout linearOuter = new LinearLayout(
                new ContextThemeWrapper(DisplayShelters.this, R.style.CardLinearOuter), null, 0);
        LinearLayout linearInner = new LinearLayout(
                new ContextThemeWrapper(DisplayShelters.this, R.style.CardLinearInner), null, 0);

        TextView name = new TextView(
                new ContextThemeWrapper(DisplayShelters.this, R.style.Card_Name), null, 0);
        TextView address = new TextView(
                new ContextThemeWrapper(DisplayShelters.this, R.style.Card_Address), null, 0);

        CardView cardView = new CardView(DisplayShelters.this);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int marginBottom = 10 * Math.round(scale);

        ViewGroup.MarginLayoutParams cardViewMarginParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        cardViewMarginParams.setMargins(0, 0, 0, marginBottom);
        cardView.requestLayout();


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(DisplayShelters.this, DisplaySingleShelter.class);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(shelter_image_link);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(uri.toString())
                        .into(imageView);
            }
        });

        int imageSize = Math.round(scale) * 200;
        imageView.setMaxHeight(imageSize);
        imageView.setMinimumHeight(imageSize);

        name.setText(shelter_name);
        address.setText(shelter_address);

        linearInner.addView(name);
        linearInner.addView(address);

        linearOuter.addView(imageView);
        linearOuter.addView(linearInner);

        cardView.addView(linearOuter);

        shelterListID.addView(cardView);
    }

    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
