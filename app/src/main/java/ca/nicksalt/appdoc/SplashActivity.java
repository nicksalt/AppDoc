package ca.nicksalt.appdoc;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // Cancel notifications
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        // Start home activity
        if(auth.getCurrentUser() != null) {
            if (auth.getCurrentUser().getDisplayName() == null ||
                    auth.getCurrentUser().getDisplayName().equals("")) {
                auth.signOut();
                startActivity(new Intent(SplashActivity.this, LoginActivity
                        .class));
            } else {
                Log.d("AppDoc", auth.getCurrentUser().getDisplayName());
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            }
        } else{
            //TOBE replaced
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        // close splash activity
        finish();
    }
}