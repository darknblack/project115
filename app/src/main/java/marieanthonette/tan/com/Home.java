package marieanthonette.tan.com;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    private FirebaseAuth mAuth;

    GoogleSignInClient mGoogleSignInClient;
    int bAddShelter, bViewShelters, bLogout, bViewRequests;
    TextView mUsername;

    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bAddShelter = R.id.vAddShelter;
        bViewShelters = R.id.vViewShelters;
        bLogout = R.id.vLogout;
        bViewRequests = R.id.vViewRequests;
        mUsername = findViewById(R.id.username);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // GREET THE USER INITIAL LOGIN

        userName = user.getDisplayName();

        mUsername.setText("Username: " + userName);

        Intent intent = getIntent();
        if(intent.getExtras() != null && intent.getStringExtra("intent_from").equals("login"))
            Toast("Hi! " + userName);
    }



    public void action(View v) {

        int option = v.getId();
        Intent intent;

        if(option == bAddShelter) {
            intent = new Intent(Home.this, AddShelter.class);
            startActivity(intent);
        }
        else if(option == bViewShelters) {
            intent = new Intent(Home.this, DisplayShelters.class);
            startActivity(intent);
        }
        else if(option == bViewRequests) {
            intent = new Intent(Home.this, DisplayRequests.class);
            startActivity(intent);
        } else {

            mAuth.signOut();

            // IGNORE THIS, NEEDED FOR SIGNOUT
            mGoogleSignInClient = GoogleSignIn.getClient(this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build());

            mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(Home.this, LogIn.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });

            Toast("You have successfully sign out!");
            finish();
        }
    }
    protected void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
