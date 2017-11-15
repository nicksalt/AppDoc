package ca.nicksalt.appdoc;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;
import android.widget.TextView;


public class HomeActivity extends BaseNavDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set Home to current selected in nav bar
        navigationView.getMenu().getItem(0).setChecked(true);
        View header = navigationView.getHeaderView(0);
        TextView headerEmail = header.findViewById(R.id.nav_email);
        try {
            headerEmail.setText(auth.getCurrentUser().getEmail());
        } catch (NullPointerException e) {
            e.printStackTrace();
            headerEmail.setText("");
        }
    }




}
