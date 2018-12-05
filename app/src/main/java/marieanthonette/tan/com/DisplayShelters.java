package marieanthonette.tan.com;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.List;


public class DisplayShelters extends AppCompatActivity {

    DatabaseReference mShelter;

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


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mShelter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shelterListID.removeAllViews();
                for(DataSnapshot ss : dataSnapshot.getChildren()) {
                    displayShelter(ss.getValue(Shelter.class), ss.getKey());
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
                Picasso.with(DisplayShelters.this).load(uri.toString()).into(imageView);
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
