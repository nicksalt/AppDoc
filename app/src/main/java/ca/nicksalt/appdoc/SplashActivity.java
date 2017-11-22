package ca.nicksalt.appdoc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Start home activity
        if(auth.getCurrentUser() != null) {
            Log.d("Test", auth.getCurrentUser().getEmail());
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        } else{
            //TOBE replaced
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        // close splash activity
        finish();
    }
}