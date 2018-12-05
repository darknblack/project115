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
        Intent i = null;

        if(v.getId()==R.id.logIn)
        {
            i = new Intent(this,LogIn.class);
            startActivity(i);
        }

        else  if(v.getId()==R.id.signUp)
        {
            i = new Intent(this,SignUp.class);
            startActivity(i);
        }

    }
}
