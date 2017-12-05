package ca.nicksalt.appdoc;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeActivity extends BaseNavDrawerActivity implements View.OnClickListener {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //NavBar Stuff
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Setting Header Info
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set Home to current selected in nav bar
        navigationView.getMenu().getItem(0).setChecked(true);
        View header = navigationView.getHeaderView(0);
        final TextView headerTest = header.findViewById(R.id.nav_completed_test);
        TextView headerEmail = header.findViewById(R.id.nav_email);
        try {
            //noinspection ConstantConditions
            headerEmail.setText(auth.getCurrentUser().getEmail());
            //Get Number of test for database
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child
                    (auth.getCurrentUser().getUid()).child("number-of-test");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null || (Long) dataSnapshot.getValue() == 0){
                        headerTest.setText("Complete your first test!");
                    } else if((Long) dataSnapshot.getValue() == 1) {
                        headerTest.setText((Long) dataSnapshot.getValue() + " Test Completed");
                    } else{
                        headerTest.setText((Long) dataSnapshot.getValue() + " Tests Completed");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
            headerEmail.setText("");
        }
        //Button onClicks
        findViewById(R.id.colour_test).setOnClickListener(this);
        findViewById(R.id.hearing_button).setOnClickListener(this);
        findViewById(R.id.eye_button).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.colour_test) {
            startActivity(new Intent(HomeActivity.this, ColourTestActivity.class));
        } else if (id == R.id.hearing_button) {
            startActivity(new Intent(HomeActivity.this, HearingTestActivity.class));
        } else if (id == R.id.eye_button) {
            startActivity(new Intent(HomeActivity.this, EyeTestActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Set Home to current selected in nav bar
        navigationView.getMenu().getItem(0).setChecked(true);
    }
}
