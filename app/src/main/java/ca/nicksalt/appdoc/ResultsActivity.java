package ca.nicksalt.appdoc;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResultsActivity extends BaseNavDrawerActivity {

    private TextView title, tableTitle2, tableTitle3;
    private DatabaseReference reference;
    TableLayout colourTable;
    TableLayout eyeTable;
    TableLayout hearingTable;
    TableLayout currentTable;

    private ProgressDialog progressDialog;
    NavigationView navigationView;

    float dp_scale;

    final int WRITE_EXTERNAL_STORAGE_CODE = 24;
    final int READ_EXTERNAL_STORAGE_CODE = 25;

    ArrayList<Long> times = new ArrayList<>();

    File pdfFile;

    long tests = 0;

    boolean pdfGenerating = false;
    private int pdfStep;
    ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Database stuff takes longer to load so show dialog

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
        TextView headerName = header.findViewById(R.id.nav_email);
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
                        colourTest();
                        return true;
                    case R.id.eye:
                        currentTable = eyeTable;
                        eyeTest();
                        return true;
                    case R.id.hearing:
                        currentTable = hearingTable;
                        hearingTest();
                        return true;
                }
                return false;
            }
        });
        findViewById(R.id.results_pdf_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForPermissions(true);
            }
        });
        // Navbar info
        try {
            headerName.setText(auth.getCurrentUser().getDisplayName());
            reference = FirebaseDatabase.getInstance().getReference().child("users").child
                    (auth.getCurrentUser().getUid()).child("number-of-test");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() == null || (Long) dataSnapshot.getValue() == 0){
                        headerTest.setText("Complete your first test!");
                        tests = 0;
                    } else if((Long) dataSnapshot.getValue() == 1) {
                        tests = (long)dataSnapshot.getValue();
                        headerTest.setText(tests + " Test Completed");
                    } else{
                        tests = (long)dataSnapshot.getValue();
                        headerTest.setText(tests + " Tests Completed");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });

        } catch (NullPointerException e) {
            e.printStackTrace();
            headerName.setText("");
        }

        // Scaled to convert pixels to dp
        dp_scale =  getResources().getDisplayMetrics().density;
        //Call colour test to start
        colourTest();
        currentTable = colourTable;
    }

    private void colourTest() {
        colourTable.setVisibility(View.VISIBLE);
        hearingTable.setVisibility(View.GONE);
        eyeTable.setVisibility(View.GONE);
        tableTitle2.setText("Red Green Correct");
        tableTitle3.setText("Total Correct");
        title.setText("Colour Test Past Results");
        if(colourTable.getChildCount() == 0) {
            try {
                times = new ArrayList<>();
                // Get info from past colour tests
                reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid()).child("color-test");
                // Read from the database
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                        for (DataSnapshot dS : source) {
                            createRow(dS.getChildren(), colourTable, "RedGreen", "TotalGreen");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else if(pdfGenerating){
            Log.d("AppDoc", "Colour Test");
            pdfStep++;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {getBitmaps();}}, 1000);
        }
    }
    private void hearingTest() {
        // Pretty much same as colourTest() with exception of string, will optimize later with universal strings
        colourTable.setVisibility(View.GONE);
        hearingTable.setVisibility(View.VISIBLE);
        eyeTable.setVisibility(View.GONE);
        tableTitle2.setText("Estimated Age");
        tableTitle3.setText("Highest Frequency");
        title.setText("Hearing Test Past Results");
        if(hearingTable.getChildCount() == 0) {
            times = new ArrayList<>();
            try {
                reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser()
                        .getUid()).child("hearing-test");
                // Read from the database
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                        for (DataSnapshot dS : source) {
                            createRow(dS.getChildren(), hearingTable, "EstAge", "HighestFreq");
                        }if(pdfGenerating){
                            Log.d("AppDoc", "Hearing Test");
                            pdfStep++;
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {getBitmaps();}}, 1000);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else if(pdfGenerating){
            Log.d("AppDoc", "Hearing Test");
            pdfStep++;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {getBitmaps();}}, 1000);

        }

    }

    private void eyeTest() {
        // Pretty much same as colourTest() with exception of string, will optimize later with universal strings
        title.setText("Eye Test Past Results");
        colourTable.setVisibility(View.GONE);
        hearingTable.setVisibility(View.GONE);
        eyeTable.setVisibility(View.VISIBLE);
        tableTitle2.setText("Score");
        tableTitle3.setText("Description");
        if(eyeTable.getChildCount() == 0) {
            times = new ArrayList<>();
            try {
                reference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser()
                        .getUid()).child("eye-test");
                // Read from the database
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> source = dataSnapshot.getChildren();
                        for (DataSnapshot dS : source) {
                            createRow(dS.getChildren(), eyeTable, "Score", "Description");

                        }
                        if(pdfGenerating){
                            Log.d("AppDoc", "Eye Test");
                            pdfStep++;
                            (new Handler()).postDelayed(new Runnable() {
                                public void run() {getBitmaps();}}, 1000);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        } else if(pdfGenerating){
            Log.d("AppDoc", "Eye Test");
            pdfStep++;
            (new Handler()).postDelayed(new Runnable() {
                public void run() {getBitmaps();}}, 1000);
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
        int index = 0;
        if (table.getChildCount() > 0) {
            try {
                long timeTable = times.get(0);
                while (timeTable < time && times.size() > index){
                    timeTable = times.get(++index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        times.add(index, time);
        table.addView(tableRow, index);

        Log.d("AppDoc", "createRows");
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Generating PDF... ");
            progressDialog.setMessage("Please be patient, this may take up to 30 seconds.");
            progressDialog.setCancelable(false);
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
    @Override
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
                    getBitmaps();
                else
                    Toast.makeText(this, "Permission must be granted",
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkForPermissions(boolean write) {
        if (write) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE_CODE);
            } else
                getBitmaps();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        READ_EXTERNAL_STORAGE_CODE);
            } else
                getBitmaps();
        }
    }

    public void getBitmaps() {
        if(tests > 0) {
            switch (pdfStep) {
                case 0:
                    showProgressDialog();
                    Log.d("AppDoc", "Colour loop");
                    pdfGenerating = true;
                    colourTest();
                    break;
                case 1:
                    if (colourTable.getChildCount() > 0)
                        bitmaps.add(getBitmapFromView());
                    pdfStep++;
                    getBitmaps();
                    break;
                case 2:
                    eyeTest();
                    Log.d("AppDoc", "Eye loop");
                    break;
                case 3:
                    if (eyeTable.getChildCount() > 0)
                        bitmaps.add(getBitmapFromView());
                    pdfStep++;
                    getBitmaps();
                    break;
                case 4:
                    hearingTest();
                    Log.d("AppDoc", "Hearing loop");
                    break;
                case 5:
                    if (hearingTable.getChildCount() > 0)
                        bitmaps.add(getBitmapFromView());
                    pdfStep++;
                    getBitmaps();
                    break;
                case 6:
                    Log.d("AppDoc", "Finished loop");
                    generatePDF();
                    break;
                case 7:
                    hideProgressDialog();
                    pdfGenerating = false;
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_pdf, null);
                    Button cancel = view.findViewById(R.id.pdf_dialog_cancel);
                    cancel.getBackground().setColorFilter(ContextCompat.getColor(
                            this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });
                    Button ok = view.findViewById(R.id.pdf_dialog_ok);
                    ok.getBackground().setColorFilter(ContextCompat.getColor(
                            this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent pdfIntent = new Intent();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Log.d("AppDoc", pdfFile.getAbsolutePath());
                            pdfIntent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
                            if (pdfIntent.resolveActivity(getPackageManager()) != null)
                                startActivity(Intent.createChooser(pdfIntent, "Open with:"));
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setView(view);
                    alertDialog.show();
                    if (currentTable == colourTable)
                        colourTest();
                    else if (currentTable == eyeTable)
                        eyeTest();
                    break;
            }
        } else {
            Toast.makeText(this, "Please complete your first test.", Toast.LENGTH_LONG).show();
        }

    }
    public void generatePDF() {
        Log.d("AppDoc", hearingTable.getChildCount() + " "+ " " + hearingTable.getHeight());

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Create Directory
                Log.d("AppDoc", "Running");
                String state = Environment.getExternalStorageState();
                if (!Environment.MEDIA_MOUNTED.equals(state))
                    Toast.makeText(getApplicationContext(), getString(R.string.storage), Toast.LENGTH_SHORT).show
                            ();
                //Create directory for pdf
                File pdfDir = new File(Environment.getExternalStorageDirectory() +
                        "/AppDoc");
                if (!pdfDir.exists())
                    pdfDir.mkdir();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                String fileName = "Results";
                pdfFile = new File(pdfDir, fileName + ".pdf");
                try {
                    ChangeMargins event = new ChangeMargins();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
                    ByteArrayOutputStream logoStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, logoStream);
                    Image logo = Image.getInstance(logoStream.toByteArray());
                    logo.scaleAbsolute(550, 155);
                    logo.setAbsolutePosition(PageSize.A4.getWidth()/2 - logo.getScaledWidth()/2,
                            PageSize.A4.getHeight() - logo.getScaledHeight() - 36f);

                    Document document = new Document(PageSize.A4, 36, 36, 155+36,
                            36);
                    PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                    pdfWriter.setPageEvent(event);
                    document.open();
                    document.add(logo);
                    pdfWriter.setStrictImageSequence(true);
                    document.add(new Paragraph("\n\n"));
                    Font font = new Font(Font.FontFamily.HELVETICA, 64, Font.BOLD,
                            new BaseColor(42, 88, 115));
                    Paragraph title = new Paragraph("AppDoc Results", font);
                    title.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(title);
                    font.setSize(36);
                    if (auth.getCurrentUser() != null) {
                        title = new Paragraph(auth.getCurrentUser().getDisplayName(), font);
                        title.setAlignment(Paragraph.ALIGN_CENTER);
                        document.add(title);
                    }
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    title = new Paragraph(new SimpleDateFormat("dd/MM/yy").format(calendar.getTime()), font);
                    title.setAlignment(Paragraph.ALIGN_CENTER);
                    document.add(title);
                    document.newPage();
                    while (bitmaps.size() > 0) {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmaps.remove(0).compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytes = stream.toByteArray();

                        Log.d("AppDoc", "Sending from generate added");
                        addImage(document, bytes);
                        document.add(Chunk.NEWLINE);
                    }

                    document.close();
                    pdfStep++;
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
        thread.start();
        try {
            thread.join();
            Log.d("AppDoc", pdfStep+"");
            getBitmaps();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromView() {
        ScrollView view = findViewById(R.id.result_scroll_view);
        int totalHeight = view.getChildAt(0).getHeight();
        int totalWidth = view.getChildAt(0).getWidth();
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

    private void addImage(Document d, byte[] b) {
        //add Image to document

        Log.d("AppDoc", "Bitmap adding...");
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
            if (image.getScaledHeight() > d.getPageSize().getHeight() - d.topMargin() - d.bottomMargin()){
                float ratio = (d.getPageSize().getHeight() - d.topMargin() - d.bottomMargin()) / image
                        .getScaledHeight();
                spiltBitmap(ratio, d, b);
            }
            else {
                d.add(image);
                Log.d("AppDoc", "Bitmap added");
            }



        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void spiltBitmap(Float ratio, Document d, byte[] b) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i = 1;
        while (ratio*i < 1) {
            Log.d("AppDoc", "Running ratio: " + ratio);
            Bitmap subBitmap = Bitmap.createBitmap(bitmap, 0, Math.round(bitmap
                    .getHeight() * ratio * (i-1)), bitmap.getWidth() , Math.round(bitmap
                    .getHeight() * ratio));
            subBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            addImage(d, stream.toByteArray());
            i++;
            stream = new ByteArrayOutputStream();
            subBitmap.recycle();
        } if (ratio*i > 1){
            Bitmap subBitmap;
            float remaining = 1 - ratio*(i-1);
            Log.d("AppDoc", "Bitmap created ");
            subBitmap = Bitmap.createBitmap(bitmap, 0, (int)Math.floor(bitmap
                    .getHeight() * ratio * (i - 1)), bitmap.getWidth(), (int)Math.floor(bitmap
                    .getHeight() * remaining));
            Log.d("AppDoc", "Running remaining: " + remaining + " " + subBitmap.getByteCount());
            subBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            addImage(d, stream.toByteArray());


        }
    }
    public class ChangeMargins extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            document.setMargins(36, 36, 36, 36);
        }
    }
}
