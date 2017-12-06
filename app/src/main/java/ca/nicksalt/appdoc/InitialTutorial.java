package ca.nicksalt.appdoc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class InitialTutorial extends AppCompatActivity {

    LinearLayout pageOne, pageTwo, pageThree, pageFour;
    Button begin, skip, nextOne, nextTwo, finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_tutorial);

        pageOne = (LinearLayout) findViewById(R.id.tutorialPage1);
        pageTwo = (LinearLayout) findViewById(R.id.tutorialPage2);
        pageThree = (LinearLayout) findViewById(R.id.tutorialPage3);
        pageFour = (LinearLayout) findViewById(R.id.tutorialPage4);

        begin = (Button) findViewById(R.id.tutorialbegin);
        skip = (Button) findViewById(R.id.tutorialskip);
        nextOne = (Button) findViewById(R.id.tutorialNext1);
        nextTwo = (Button) findViewById(R.id.tutorialNext2);
        finish = (Button) findViewById(R.id.tutorialNext3);


        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageOne.setVisibility(View.INVISIBLE);
                pageTwo.setVisibility(View.VISIBLE);
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InitialTutorial.this, HomeActivity.class));
            }
        });

        nextOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageTwo.setVisibility(View.INVISIBLE);
                pageThree.setVisibility(View.VISIBLE);
            }
        });

        nextTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageThree.setVisibility(View.INVISIBLE);
                pageFour.setVisibility(View.VISIBLE);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InitialTutorial.this, HomeActivity.class));
            }
        });

    }
}
