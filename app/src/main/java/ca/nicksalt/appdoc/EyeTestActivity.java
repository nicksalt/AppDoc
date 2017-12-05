package ca.nicksalt.appdoc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

public class EyeTestActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout instructions, test, results;

    Button startTest, left, right, up, down, optometrist, share, alertOk;

    TextView testE, resultScore, resultsText, alertText;

    final float[] fontSizes = new float[]{14, 11.5f, 9.25f, 7.25f, 5.5f, 4};
    LinkedList<String> possibleText = new LinkedList<>(Arrays.asList("3", "E",
            "M", "W"));

    int level = -1;
    int attempts, overallScore, overallAttempts;
    String lastCharacter;

    boolean testingRightEye = true;
    boolean finishedEarly = false;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_test);
        // Set all layouts
        instructions = findViewById(R.id.eye_test_instructions);
        test = findViewById(R.id.eye_test_test);
        results = findViewById(R.id.eye_test_results);

        // Set Buttons and Listeners
        startTest = findViewById(R.id.eye_test_start);
        left = findViewById(R.id.eye_test_left);
        left.setOnClickListener(this);
        right = findViewById(R.id.eye_test_right);
        right.setOnClickListener(this);
        up = findViewById(R.id.eye_test_up);
        up.setOnClickListener(this);
        down = findViewById(R.id.eye_test_down);
        down.setOnClickListener(this);
        optometrist = findViewById(R.id.eye_test_optometrist_button);
        optometrist.setOnClickListener(this);
        share = findViewById(R.id.eye_test_share_button);
        share.setOnClickListener(this);

        // Set text views
        testE = findViewById(R.id.eye_test_E);
        resultsText = findViewById(R.id.eye_test_diagnostic);
        resultScore = findViewById(R.id.eye_test_score);

        //Set Image view
        //TODO: Set the image view in the results

        // Change background colour without affecting style.
        optometrist.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        share.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        //Set up alert dialog
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.eye_switch_dialog, null);
        alertOk = view.findViewById(R.id.eye_test_dialog_button);
        alertOk.setOnClickListener(this);
        alertOk.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        alertText = view.findViewById(R.id.eye_test_dialog_text);
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(view);

        //Make countdown button, force user to read instructions
        new CountDownTimer(6000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                startTest.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                startTest.setText(getString(R.string.start_test));
                startTest.setOnClickListener(EyeTestActivity.this);
            }
        }.start();
    }


    private void runTest(){
        testE.setText(lastCharacter = possibleText.get(attempts));
    };

    private void setNextLevel() {
        overallAttempts = overallAttempts + attempts;
        attempts = 0;
        if(++level > fontSizes.length-1 && testingRightEye || finishedEarly){
            finishedEarly = false;
            overallScore = level;
            Collections.shuffle(possibleText);
            // Set last used character to end, gets rid of repeats.
            possibleText.remove(lastCharacter);
            possibleText.addLast(lastCharacter);
            testingRightEye = false;
            level = 0;
            alertText.setText(getString(R.string.eye_test_alert_next));
            alertDialog.show();
            testE.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSizes[0]);
            runTest();
        } else if (level <= fontSizes.length-1) {
            testE.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSizes[level]);
            Collections.shuffle(possibleText);
            // Set last used character to end, gets rid of repeats.
            if (lastCharacter !=null) {
                possibleText.remove(lastCharacter);
                possibleText.addLast(lastCharacter);
            }
            runTest();
        } else{
            endTest();
        }
    }

    private void endTest(){
        if (testingRightEye) {
            overallScore = level;
            finishedEarly = true;
            alertText.setText(getString(R.string.eye_test_alert_next));
            alertDialog.show();
            setNextLevel();
        } else {
            overallScore = overallScore + level;
            test.setVisibility(View.GONE);
            setResults();
        }
    }

    private void setResults() {
        final String scoreWord;
        if (overallAttempts > 3)
            overallScore--;
        if (overallScore == 12) {
            resultsText.setText(getString(R.string.eye_test_result_excellent));
            scoreWord = "Excellent";
        } else if (overallScore >= 10) {
            resultsText.setText(getString(R.string.eye_test_result_good));
            scoreWord = "Good";
        } else if (overallScore >= 8) {
            resultsText.setText(getString(R.string.eye_test_result_ok));
            scoreWord = "OK";
        } else if (overallScore >= 6) {
            resultsText.setText(getString(R.string.eye_test_result_not_good));
            scoreWord = "Not Good";
        } else {
            resultsText.setText(getString(R.string.eye_test_result_poor));
            scoreWord = "Poor";
        }
        resultScore.setText(scoreWord);
        results.setVisibility(View.VISIBLE);
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //Set to final array because it is being accessed in an inner class
            final DatabaseReference[] reference = {FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("eye-test")};
            reference[0].addValueEventListener(new ValueEventListener() {
                boolean ran = false;
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey().equals("eye-test") && !ran) {
                        // Just to be sure it doesn't keep iterating over the same node
                        ran=true;
                        reference[0] = reference[0].child("Test" + String.valueOf(dataSnapshot.getChildrenCount() + 1));
                        reference[0].child("Score").setValue(overallScore);
                        reference[0].child("Description").setValue(scoreWord);
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
    @Override
    public void onClick(View view) {
        if (view ==startTest){
            instructions.setVisibility(View.GONE);
            test.setVisibility(View.VISIBLE);
            alertDialog.show();
            setNextLevel();
        } else if(view == up && testE.getText().toString().equals("W"))
            setNextLevel();
        else if (view == down && testE.getText().toString().equals("M"))
            setNextLevel();
        else if (view == left && testE.getText().toString().equals("3"))
                setNextLevel();
        else if (view == right && testE.getText().toString().equals("E"))
            setNextLevel();
        else if (view == alertOk)
            alertDialog.dismiss();
        else if (view == optometrist){
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=optometrist+near+me"));
            startActivity(mapIntent);
        } else if (view == share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out AppDoc, the new greatest app available on Android! ");
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share with:"));
        } else if (++attempts == 3)
            endTest();
        else
            runTest();
    }

    @Override
    public void onBackPressed(){
        // If doing test, check to make sure they really want to quit
        if (test.getVisibility() == View.VISIBLE){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Would you like to exit this test?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EyeTestActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
        // If the Test is done OR instructions are being viewed, go back to the home screen.
        else {
            finish();
            super.onBackPressed();
        }
    }
}
