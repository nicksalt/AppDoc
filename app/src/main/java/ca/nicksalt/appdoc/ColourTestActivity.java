package ca.nicksalt.appdoc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ColourTestActivity extends AppCompatActivity implements View.OnClickListener {

    Button nextButton;
    Button optometrist;
    Button share;
    TextView description;
    int level = 0;
    int buttonSelected;
    //Create random indexes
    List<Integer> randomIndexRedGreen;
    List<Integer> randomIndexTotal;
    // Access resources
    TypedArray redGreenPlates;
    String[] redGreenCorrect;
    String[] redGreenIncorrect1;
    String[] redGreenIncorrect2;
    TypedArray totalPlates;
    String[] totalCorrect;
    String[] totalIncorrect;

    ImageView plate;

    Button button1;
    Button button2;
    Button button3;
    Button[] buttons;

    int correctRedGreen = 0;
    int correctTotal = 0;
    List<Integer> possibleCorrectButtons= Arrays.asList(0, 1, 2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colour_test);
        nextButton = findViewById(R.id.colour_test_next);
        nextButton.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
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
        } else if(view == nextButton && nextButton.getText() == getString(R.string.next)) {
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
        } else if (view == button1) {
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
        } else if(view == optometrist) {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=optometrist+near+me"));
            startActivity(mapIntent);
        }
    }

    @Override
    public void onBackPressed(){
        if (findViewById(R.id.colour_test_top_icon).getVisibility() ==
                View.VISIBLE){
            finish();
            super.onBackPressed();
        } else {
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
        Collections.shuffle(possibleCorrectButtons);
        boolean isLine = false;
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
        } else {
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
        ((TextView)findViewById(R.id.colour_test_score)).setText(getString(R.string.colour_test_score)+ " "
                + (correctRedGreen+correctTotal) + "/10");
        if (correctTotal < 4)
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_total);
        else if (correctRedGreen < 6)
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_red_green);
        else
            ((TextView)findViewById(R.id.colour_test_diagnostic)).setText(R.string.colour_test_diagnostic_perfect);

        DatabaseReference database =  FirebaseDatabase.getInstance().getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("TEST", userId);
        if (userId!=null)
            database.child("users").child(userId).child("s").setValue("TESTING");
    }

}

