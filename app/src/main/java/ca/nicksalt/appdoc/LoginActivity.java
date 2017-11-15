package ca.nicksalt.appdoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    private View currentView;
    private String email, password;
    private Button emailContinue;
    private FirebaseAuth auth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        //Set Button and onClicks
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
        if (view.getId() == R.id.go_to_home_button)
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            //Change View Visibility (See layout folder)
        else if (view.getId() == R.id.email_button) {
            findViewById(R.id.layout_login_buttons).setVisibility(View.GONE);
            currentView = findViewById(R.id.layout_email_address);
            currentView.setVisibility(View.VISIBLE);
            emailContinue.setVisibility(View.VISIBLE);

        } else if (view == emailContinue) {
            if (currentView == findViewById(R.id.layout_email_address)) {
                EditText editTextEmail = findViewById(R.id.email_text);
                if (isValidEmail(email = editTextEmail.getText().toString()))
                    checkEmail();
                else
                    Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_LONG).show();
            } else if (currentView == findViewById(R.id.layout_login_buttons)); {
                EditText editTextPassword = findViewById(R.id.password_login);
                if (isValidPassword(password = editTextPassword.getText().toString()))
                    loginWithEmail();
                else
                    Toast.makeText(this, "Password is too short.", Toast.LENGTH_SHORT).show();

            }

        }
    }

    @Override
    public void onBackPressed(){
        //Change View Visibility
        if (currentView == findViewById(R.id.layout_email_address)){
                findViewById(R.id.layout_email_address).setVisibility(View.GONE);
                currentView = findViewById(R.id.layout_login_buttons);
                currentView.setVisibility(View.VISIBLE);
                findViewById(R.id.email_continue_button).setVisibility(View.GONE);
        } else if (currentView == findViewById(R.id.layout_password_sign_up) ||
                currentView == findViewById(R.id.layout_password_login)) {
            findViewById(R.id.layout_password_sign_up).setVisibility(View.GONE);
            findViewById(R.id.layout_password_sign_up).setVisibility(View.GONE);
            findViewById(R.id.layout_password_login).setVisibility(View.GONE);
            currentView = findViewById(R.id.layout_email_address);
            currentView.setVisibility(View.VISIBLE);
            emailContinue.setText("Next");
        }
    }

    private void checkEmail(){
        auth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if (task.isSuccessful()) {
                    try {
                        if (task.getResult().getProviders().size() > 0) {
                            findViewById(R.id.layout_email_address).setVisibility(View.GONE);
                            currentView = findViewById(R.id.layout_password_login);
                            currentView.setVisibility(View.VISIBLE);
                            emailContinue.setText("Login");
                        } else {
                            findViewById(R.id.layout_email_address).setVisibility(View.GONE);
                            currentView = findViewById(R.id.layout_password_sign_up);
                            currentView.setVisibility(View.VISIBLE);
                            emailContinue.setText("Sign Up");
                        }
                    } catch (NullPointerException e){
                        Toast.makeText(LoginActivity.this,
                                "Unable to reach server. Please check intrenet connection and try again.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
    private void loginWithEmail(){
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                            // Sign in success, update UI with the signed-in user's information
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                         else
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();


                    }
                });
    }
    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isValidPassword(CharSequence target){
        return (target.length()>6);
    }
}