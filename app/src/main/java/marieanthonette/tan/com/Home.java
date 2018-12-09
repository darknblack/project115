package marieanthonette.tan.com;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {

    int bAddShelter, bViewShelters, bLogout, bViewRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bAddShelter = R.id.vAddShelter;
        bViewShelters = R.id.vViewShelters;
        bLogout = R.id.vLogout;
        bViewRequests = R.id.vViewRequests;
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
        }
//
//        startActivity(intent);
    }
}
