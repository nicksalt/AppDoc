package ca.nicksalt.appdoc;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ColourTestActivity extends AppCompatActivity implements View.OnClickListener {


    //Create random indexes
    List<Integer> randomIndexRedGreen;
    List<Integer> randomIndexTotal;
    List<Integer> possibleCorrectButtons= Arrays.asList(0, 1, 2);

    // Access resources
    TypedArray redGreenPlates;
    String[] redGreenCorrect;
    String[] redGreenIncorrect1;
    String[] redGreenIncorrect2;
    TypedArray totalPlates;
    String[] totalCorrect;
    String[] totalIncorrect;

    // Layout items
    TextView description;
    ImageView plate;
    Button nextButton;
    Button optometrist;
    Button share;
    Button button1;
    Button button2;
    Button button3;
    Button[] buttons;

    // Keep track of totals, buttons last selected, etc.
    int correctRedGreen = 0;
    int correctTotal = 0;
    int level = 0;
    int buttonSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colour_test);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 100/(float)255;
        getWindow().setAttributes(lp);

        nextButton = findViewById(R.id.colour_test_next);
        //Make Button Blue
        nextButton.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        //Make countdown button, force user to read instructions
        new CountDownTimer(6000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                nextButton.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                nextButton.setText(getString(R.string.start_test));
                nextButton.setOnClickListener(ColourTestActivity.this);
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        //Click Button during instructions
        if (view == nextButton && nextButton.getText() == getString(R.string.start_test)) {
            findViewById(R.id.colour_test_top_icon).setVisibility(View.GONE);
            findViewById(R.id.colour_test_instructions).setVisibility(View.GONE);
            startTest();
        } // Click next button during test
        else if(view == nextButton && nextButton.getText() == getString(R.string.next)) {
            // Selected the right choice
            if(buttonSelected == possibleCorrectButtons.get(0)){
                if (level%2 == 1 || level==10)
                    correctRedGreen++;
                else
                    correctTotal++;
            }
            level++;
            button1.getBackground().clearColorFilter();
            button2.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            if (level<=10)
                setLevel();
            else
                finishTest();
        } //Button 1, Button 2, or Button 3 selected during test
        else if (view == button1) {
            buttonSelected = 0;
            button1.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button2.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        } else if (view == button2) {
            buttonSelected = 1;
            button2.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button1.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        } else if (view == button3) {
            buttonSelected = 2;
            button3.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button1.getBackground().clearColorFilter();
            button2.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        } //Optometrist button selected
        else if(view == optometrist) {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=optometrist+near+me"));
            startActivity(mapIntent);
        }
        else if (view == share){
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out AppDoc, the new greatest app available on Android! ");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share with:"));
        }
    }

    @Override
    public void onBackPressed(){
        // If the Test is done OR instructions are being viewed, go back to the home screen.
        if (findViewById(R.id.colour_test_top_icon).getVisibility() ==
                View.VISIBLE){
            finish();
            super.onBackPressed();
        } // If doing test, check to make sure they really want to quit
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Would you like to exit this test?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ColourTestActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // Coming back to app from either selecting optometrist or share button.
        if (findViewById(R.id.colour_test_test_completed_buttons).getVisibility() == View.VISIBLE)
            super.onBackPressed();
    }

    public void startTest() {
        //Create random indexes, used to create random plates each test for a total of 10
        randomIndexRedGreen = new ArrayList<>();
         randomIndexTotal = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            randomIndexRedGreen.add(i);
        } for (int i = 0; i < 8; i++) {
            randomIndexTotal.add(i);
        }
        Collections.shuffle(randomIndexRedGreen);
        Collections.shuffle(randomIndexTotal);

        //Set id arrays
        redGreenPlates = getResources().obtainTypedArray(R.array.red_green_color_test_plates);
        totalPlates = getResources().obtainTypedArray(R.array.total_color_test_plates);

        // Get string arrays
        redGreenCorrect = getResources().getStringArray(R.array.red_green_color_test_correct);
        redGreenIncorrect1 = getResources().getStringArray(R.array.red_green_color_test_incorrect_1);
        redGreenIncorrect2 = getResources().getStringArray(R.array.red_green_color_test_incorrect_2);
        totalCorrect = getResources().getStringArray(R.array.total_color_test_correct);
        totalIncorrect = getResources().getStringArray(R.array.total_color_test_incorrect);
        // Set first level
        findViewById(R.id.colour_test_plate_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.colour_test_button_selections).setVisibility(View.VISIBLE);
        nextButton.setText(getString(R.string.next));
        plate = findViewById(R.id.colour_test_plate);
        button1 = findViewById(R.id.colour_test_button_1);
        button2 = findViewById(R.id.colour_test_button_2);
        button3 = findViewById(R.id.colour_test_button_3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        description = findViewById(R.id.colour_test_text);
        buttons = new Button[]{button1, button2, button3};
        level++;
        setLevel();
    }

    public void setLevel(){
        nextButton.setVisibility(View.INVISIBLE);
        int index;
        //  Shuffle to mix up which button is used as the correct one
        Collections.shuffle(possibleCorrectButtons);
        //Use to set text, either a line or a number
        boolean isLine = false;
        // use 6 red green and 4 total plates
        //If level is 1, 3, 5, 7, 9, 10 : set plate to red green
        if (level%2 == 1 || level==10){
            index = randomIndexRedGreen.get(level-1);
            if (index > 7)
                isLine = true;
            plate.setImageDrawable(getResources().getDrawable(redGreenPlates.getResourceId(index, 0)));
            buttons[possibleCorrectButtons.get(0)].setText(redGreenCorrect[index]);
            buttons[possibleCorrectButtons.get(1)].setText(redGreenIncorrect1[index]);
            if (redGreenIncorrect2[index].length() > 0)
                buttons[possibleCorrectButtons.get(2)].setText(redGreenIncorrect2[index]);
            else
                buttons[possibleCorrectButtons.get(2)].setText(getString(R.string.nothing));
        }  //Otherwise set plate to a total color blindness one
        else {
            index = randomIndexTotal.get(level-2);
            if (index > 5)
                isLine = true;
            plate.setImageDrawable(getResources().getDrawable(totalPlates.getResourceId(index, 0)));
            buttons[possibleCorrectButtons.get(0)].setText(totalCorrect[index]);
            buttons[possibleCorrectButtons.get(1)].setText(totalIncorrect[index]);
            buttons[possibleCorrectButtons.get(2)].setText(getString(R.string.nothing));
        }
        if(isLine)
            description.setText(getString(R.string.color_test_line_desc));
        else
            description.setText(getString(R.string.color_test_number_desc));
    }

    @SuppressLint("SetTextI18n")
    public void finishTest(){
        // Hide Views
        findViewById(R.id.colour_test_plate_layout).setVisibility(View.GONE);
        findViewById(R.id.colour_test_button_selections).setVisibility(View.GONE);
        findViewById(R.id.colour_test_next_layout).setVisibility(View.GONE);

        //Show Views
        findViewById(R.id.colour_test_top_icon).setVisibility(View.VISIBLE);
        findViewById(R.id.colour_test_test_completed_text).setVisibility(View.VISIBLE);
        findViewById(R.id.colour_test_test_completed_buttons).setVisibility(View.VISIBLE);
        //Get Buttons
        optometrist = findViewById(R.id.colour_test_optometrist_button);
        share = findViewById(R.id.colour_test_share_button);

        //Make Them Blue
        optometrist.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        share.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        optometrist.setOnClickListener(this);
        share.setOnClickListener(this);

        //Set Text Views depending on results
        ((TextView)findViewById(R.id.colour_test_score)).setText(getString(R.string.colour_test_score)+ " "
                + (correctRedGreen+correctTotal) + "/10");
        if (correctTotal < 4)
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_total);
        else if (correctRedGreen < 6)
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_red_green);
        else
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_perfect);

        // Send notification
        Intent intent = new Intent(ColourTestActivity.this, Receiver.class);
        intent.putExtra("test", "colour");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ColourTestActivity.this, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7), pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7), pendingIntent);
        }

        // Upload results to Database.. if there are issues it may return nullPointerException
            try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //Set to final array because it is being accessed in an inner class
            final DatabaseReference[] reference = {FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("color-test")};
            reference[0].addValueEventListener(new ValueEventListener() {
                boolean ran = false;
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey().equals("color-test") && !ran) {
                        // Just to be sure it doesn't keep iterating over the same node
                        ran=true;
                        reference[0] = reference[0].child("Test" + String.valueOf(dataSnapshot.getChildrenCount() + 1));
                        reference[0].child("RedGreen").setValue(correctRedGreen);
                        reference[0].child("TotalGreen").setValue(correctTotal);
                        reference[0].child("Time").setValue(System.currentTimeMillis());
                    }
                }
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            // Increase Number of test
            final DatabaseReference reference2= FirebaseDatabase.getInstance().getReference().child("users").child
                    (userId).child("number-of-test");
            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() !=null)
                        reference2.setValue((long)dataSnapshot.getValue() + 1);
                    else
                        reference2.setValue((long)1);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

}

