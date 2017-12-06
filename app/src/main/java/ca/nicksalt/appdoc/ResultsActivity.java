package ca.nicksalt.appdoc;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ResultsActivity extends BaseNavDrawerActivity {

    private TextView title, tableTitle2, tableTitle3;
    private DatabaseReference reference;
    TableLayout colourTable;
    TableLayout eyeTable;
    TableLayout hearingTable;

    private ProgressDialog progressDialog;
    NavigationView navigationView;

    float dp_scale;

    final int WRITE_EXTERNAL_STORAGE_CODE = 24;
    final int READ_EXTERNAL_STORAGE_CODE = 25;


    File pdfFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Database stuff takes longer to load so show dialog
        showProgressDialog();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Navbar stuff
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
         navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Set Home to current selected in nav bar
        navigationView.getMenu().getItem(1).setChecked(true);
        // Initiate Views
        View header = navigationView.getHeaderView(0);
        final TextView headerTest = header.findViewById(R.id.nav_completed_test);
        TextView headerEmail = header.findViewById(R.id.nav_email);
        colourTable = findViewById(R.id.result_colour);
        eyeTable = findViewById(R.id.result_eye);
        hearingTable = findViewById(R.id.result_hearing);
        title = findViewById(R.id.result_title);
        tableTitle2 = findViewById(R.id.result_text_2);
        tableTitle3 = findViewById(R.id.result_text_3);
        //Set Bottom Nav Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.result_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // Set visibilities based on test and get data from database if it isn't
                    // already loaded
                    case R.id.color:
                        colourTable.setVisibility(View.VISIBLE);
                        hearingTable.setVisibility(View.GONE);
                        eyeTable.setVisibility(View.GONE);
                        tableTitle2.setText("Red Green Correct");
                        tableTitle3.setText("Total Correct");
                        title.setText("Colour Test Past Results");
                        if(colourTable.getChildCount() == 0)
                            colourTest();
                        return true;
                    case R.id.eye:
                        title.setText("Eye Test Past Results");
                        colourTable.setVisibility(View.GONE);
                        hearingTable.setVisibility(View.GONE);
                        eyeTable.setVisibility(View.VISIBLE);
                        tableTitle2.setText("Score");
                        tableTitle3.setText("Description");
                        if(eyeTable.getChildCount() == 0) {
                            showProgressDialog();
                            eyeTest();
                            hideProgressDialog();
                        }
                        return true;
                    case R.id.hearing:
                        colourTable.setVisibility(View.GONE);
                        hearingTable.setVisibility(View.VISIBLE);
                        eyeTable.setVisibility(View.GONE);
                        tableTitle2.setText("Estimated Age");
                        tableTitle3.setText("Highest Frequency");
                        title.setText("Hearing Test Past Results");
                        if(hearingTable.getChildCount() == 0) {
                            showProgressDialog();
                            hearingTest();
                            hideProgressDialog();
                        }
                        return true;
                }
                return false;
            }
        });
        // Navbar info
        try {
            headerEmail.setText(auth.getCurrentUser().getEmail());
            reference = FirebaseDatabase.getInstance().getReference().child("users").child
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

        // Scaled to convert pixels to dp
        dp_scale =  getResources().getDisplayMetrics().density;
        //Call colour test to start
        colourTest();
    }

    private void colourTest() {
        try {
            // Get info from past colour tests
            reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser()
                    .getUid()).child("color-test");
            // Read from the database
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                    for(DataSnapshot dS: source){
                        createRow(dS.getChildren(), colourTable, "RedGreen", "TotalGreen");
                    }
                    hideProgressDialog();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void hearingTest() {
        // Pretty much same as colourTest() with exception of string, will optimize later with universal strings
        try {
            reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser()
                    .getUid()).child("hearing-test");
            // Read from the database
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                    for(DataSnapshot dS: source){
                        createRow(dS.getChildren(), hearingTable, "EstAge", "HighestFreq");
                    }
                    hideProgressDialog();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void eyeTest() {
        // Pretty much same as colourTest() with exception of string, will optimize later with universal strings
        try {
            reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser()
                    .getUid()).child("eye-test");
            // Read from the database
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                    for(DataSnapshot dS: source){
                        createRow(dS.getChildren(), eyeTable, "Score", "Description");
                    }
                    hideProgressDialog();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void createRow(Iterable<DataSnapshot> source, TableLayout table, String child1, String child2){
        long time = 0;
        String column2 = "";
        String column3 = "";
        // Go through each value in db
        for(DataSnapshot dS: source){
            if (dS.getKey().equals(child1))
                column2 = dS.getValue().toString();
            else if (dS.getKey().equals(child2))
                column3 = dS.getValue().toString();
            else
                time = (long)dS.getValue();
        }
        //time is always stored in the database, this formats it.
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        String[] array = new String[3];
        array[0] = formatter.format(calendar.getTime());
        //Set strings accordingly
        if(table == colourTable) {
            column3 = (Integer.parseInt(column2) + Integer.parseInt(column3)) + "/10";
            column2 = column2 + "/6";
        } else if (table == hearingTable) {
            column3 = column3.charAt(0) + "," + column3.substring(1) + "Hz";
        } else{
            column2 = column2 + "/12";
        }
        array[1] = column2;
        array[2] = column3;
        //Add one row per test that was three text views, lots of layout constraints.
        TableRow tableRow = new TableRow(this);
        tableRow.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        for (int i=0; i<3; i++){
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams llp = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
            textView.setLayoutParams(llp);
            textView.setBackground(new ColorDrawable(getResources().getColor(R.color.defaultBackground)));
            textView.setPadding((int)(8*dp_scale+0.5f), (int)(4*dp_scale+0.5f), (int)(8*dp_scale+0.5f),
                    (int)(4*dp_scale+0.5f));
            textView.setGravity(Gravity.CENTER);
            if (i<2)
                llp.setMargins(0,0,(int)(3*dp_scale*0.5f),0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setText(array[i]);
            textView.setText(array[i]);
            tableRow.addView(textView);
        }
        table.addView(tableRow, 0);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Set Home to current selected in nav bar
        navigationView.getMenu().getItem(1).setChecked(true);
    }
   /* @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    checkForPermissions(false);
                else
                    Toast.makeText(this, "Permission must be granted",
                            Toast.LENGTH_SHORT).show();
            } case READ_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    generatePDF();
                else
                    Toast.makeText(this, "Permission must be granted",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkForPermissions(boolean write){
        if (write) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_CODE);
            } else
                generatePDF();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_CODE);
            } else
                generatePDF();
        }
    }*/

    private void getBitmaps() {

    }


    private Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
        //method to get bitmap of a view larger than the screen itself(Scroll View)
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        Log.d("AppDoc", totalHeight + " "+ view.toString() +" " + totalWidth );

        return returnedBitmap;
    }

    private void addImage(Document d, byte[] b){
        //add Image to document
        Image image;
        try{
            image = Image.getInstance(b);
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
        try{
            //Make width of image equal to width of page
            float scaler = ((d.getPageSize().getWidth() - d.leftMargin()
                    - d.rightMargin()) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            d.add(image);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
