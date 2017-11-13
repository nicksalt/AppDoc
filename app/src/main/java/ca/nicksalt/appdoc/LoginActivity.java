package ca.nicksalt.appdoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private static final int RC_SIGN_IN = 123;

    private Button emailContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailContinue = findViewById(R.id.email_continue_button);
        emailContinue.setOnClickListener(this);
        findViewById(R.id.go_to_home_button).setOnClickListener(this);
        findViewById(R.id.email_button).setOnClickListener(this);
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.go_to_home_button)
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        //Change View Visibility (See layout folder)
        else if (i == R.id.email_button) {
            findViewById(R.id.layout_login_buttons).setVisibility(View.GONE);
            findViewById(R.id.layout_email_address).setVisibility(View.VISIBLE);
            findViewById(R.id.email_continue_button).setVisibility(View.VISIBLE);
        } else if (i == R.id.email_continue_button){
            if (findViewById(R.id.layout_email_address).getVisibility() == View.VISIBLE){
                EditText email = findViewById(R.id.email_text);
                if (isValidEmail(email.getText().toString()))
                    checkEmail();

                else
                    Toast.makeText(this,"Invalid Email Address", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onBackPressed(){
        //Change View Visibility
        if (findViewById(R.id.layout_email_address).getVisibility() == View.VISIBLE) {
            findViewById(R.id.layout_email_address).setVisibility(View.GONE);
            findViewById(R.id.layout_login_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_continue_button).setVisibility(View.GONE);

        } else if (findViewById(R.id.layout_password_sign_up).getVisibility() == View.VISIBLE) {
            findViewById(R.id.layout_password_sign_up).setVisibility(View.GONE);
            findViewById(R.id.layout_email_address).setVisibility(View.VISIBLE);
        }
    }

    private void checkEmail(){
        //FIREBASE AUTH TO BE ADDED
        findViewById(R.id.layout_email_address).setVisibility(View.GONE);
        findViewById(R.id.layout_password_sign_up).setVisibility(View.VISIBLE);
        emailContinue.setText("Sign Up");
        /*
        OR
        findViewById(R.id.layout_email_address).setVisibility(View.GONE);
        findViewById(R.id.layout_password_login).setVisibility(View.VISIBLE);
        emailContinue.setText("Login");
         */
    }
    public boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}