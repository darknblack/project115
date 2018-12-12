package marieanthonette.tan.com;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void process(View v){
        if(v.getId()==R.id.logIn) {
            Intent i = null;
            i = new Intent(this,LogIn.class);
            startActivity(i);
        }

    }
}
