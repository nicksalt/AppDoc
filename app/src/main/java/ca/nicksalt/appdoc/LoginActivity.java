package ca.nicksalt.appdoc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;


public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {


    private static final int RC_SIGN_IN_GOOGLE = 9001;

    //Keep Track of Layouts (Buttons, email, password)
    private View currentView;
    private Button emailContinue, forgotPassword;
    private TextView emailError, passwordError;
    private ProgressDialog progressDialog;
    private TextView mainText;

    private String email, password;
    private boolean accountExists;

    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Set Views and onClicks
        mainText = findViewById(R.id.login_text);
        emailContinue = findViewById(R.id.email_continue_button);
        forgotPassword = findViewById(R.id.forgot_password);
        emailError = findViewById(R.id.email_error_text);
        passwordError = findViewById(R.id.password_error_text);
        emailContinue.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        findViewById(R.id.email_button).setOnClickListener(this);
        findViewById(R.id.google_button).setOnClickListener(this);
        currentView = findViewById(R.id.layout_login_buttons);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_ID))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Setup Firebase
        auth = FirebaseAuth.getInstance();
    }




    @Override
    public void onClick(View view) {
        //All Button On Clicks, works because of setting them in onCreate and
        // implementing onClick
        int i = view.getId();
        // Setup via email
        if (i == R.id.email_button) {
            currentView.setVisibility(View.GONE);
            currentView = findViewById(R.id.layout_email_address);
            currentView.setVisibility(View.VISIBLE);
            emailContinue.setVisibility(View.VISIBLE);
            mainText.setText(getString(R.string.enter_email_request));
        }
        // Email Button Continued tapped: either when entering email, or password
        else if (view == emailContinue) {
            //User is entering email address
            if (currentView == findViewById(R.id.layout_email_address)) {
                EditText editTextEmail = findViewById(R.id.email_text);
                if (isValidEmail(email = editTextEmail.getText().toString()))
                    checkEmail();
                else {
                    emailError.setText(getString(R.string.invalid_email));
                    editTextEmail.setText("");
                }
            }
            // User is entering in password
            else if (currentView == findViewById(R.id.layout_password)) {
                EditText editTextPassword = findViewById(R.id.password_login);
                //Firebase requires password to be > 6 character
                if (isValidPassword(password = editTextPassword.getText().toString())) {
                    if (accountExists)
                        loginWithEmail();
                    else
                        signUpWithEmail();
                }
                else {
                    passwordError.setText(getString(R.string.password_longer));
                    editTextPassword.setText("");
                }
            }
        }
        else if (view == forgotPassword){
            showProgressDialog();
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this,
                                        getString(R.string.forgot_email_sent), Toast.LENGTH_LONG).show();
                            } else{
                                Toast.makeText(LoginActivity.this,
                                        getString(R.string.forgot_email_failed), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            hideProgressDialog();
        }
        // Sign in via Google
        else if (view.getId() == R.id.google_button){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
        }
    }

    @Override
    public void onBackPressed(){
        //Change View Visibility

        //Currently entering email address
        if (currentView == findViewById(R.id.layout_email_address)){
                findViewById(R.id.layout_email_address).setVisibility(View.GONE);
                findViewById(R.id.email_continue_button).setVisibility(View.GONE);
                currentView = findViewById(R.id.layout_login_buttons);
                currentView.setVisibility(View.VISIBLE);
                mainText.setText(getString(R.string.AppDoc_login_greeting));
        }
        //Currently entering password
        else if (currentView == findViewById(R.id.layout_password)) {
            findViewById(R.id.layout_password).setVisibility(View.GONE);
            currentView = findViewById(R.id.layout_email_address);
            currentView.setVisibility(View.VISIBLE);
            emailContinue.setText(getString(R.string.next));
            forgotPassword.setVisibility(View.GONE);
            mainText.setText(getString(R.string.enter_email_request));
        }
    }

    private void checkEmail(){
        showProgressDialog();
        auth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                if (task.isSuccessful()) {
                    try {
                        currentView.setVisibility(View.GONE);
                        //Already a user?
                        if (task.getResult().getProviders().size() > 0) {
                            //Login, no need to sign up
                            currentView = findViewById(R.id.layout_password);
                            currentView.setVisibility(View.VISIBLE);
                            forgotPassword.setVisibility(View.VISIBLE);
                            emailContinue.setText(R.string.login);
                            mainText.setText(getString(R.string.enter_password_request_login));
                            accountExists = true;
                        }
                        //Not a user, sign up
                        else {
                            currentView = findViewById(R.id.layout_password);
                            currentView.setVisibility(View.VISIBLE);
                            emailContinue.setText(getString(R.string.sign_up));
                            mainText.setText(getString(R.string.enter_password_request_sign_up));
                            accountExists = false;

                        }
                    } catch (NullPointerException e){
                        passwordError.setText(getString(R.string.server_error));
                    }
                    hideProgressDialog();
                }
            }
        });
    }
    private void loginWithEmail(){
        showProgressDialog();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            hideProgressDialog();
                            checkDisplayName();

                        }else {
                            // If sign in fails, display a message to the user.
                            passwordError.setText(getString(R.string.password_does_not_match));
                            hideProgressDialog();
                        }
                    }
                });

    }

    private void signUpWithEmail(){
        showProgressDialog();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
                            hideProgressDialog();
                            checkDisplayName();
                        } else {
                            // If sign up fails, display a message to the user.
                            passwordError.setText(getString(R.string.server_error));
                            hideProgressDialog();

                        }
                    }
                });
    }

    private void signInWithGoogle(GoogleSignInAccount acct){
        showProgressDialog();
        //Opens Google dialog
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Successfully linked
                        if (task.isSuccessful()) {
                            // Sign in success
                            hideProgressDialog();
                            checkDisplayName();
                        }
                        // Failed, likely connection issue
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.google_sign_in_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void checkDisplayName(){
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null && (user.getDisplayName() == null || user.getDisplayName().equals(""))){
            currentView.setVisibility(View.GONE);
            currentView = findViewById(R.id.layout_login_name);
            currentView.setVisibility(View.VISIBLE);
            emailContinue.setVisibility(View.VISIBLE);
            forgotPassword.setVisibility(View.GONE);
            emailContinue.setText("Alight, Let's Go!");
            mainText.setText("Please enter your first and last name.");
            emailContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showProgressDialog();
                    String name = ((TextView)findViewById(R.id.name_text)).getText().toString();
                    if (!name.equals("") && !name.equals(" ")) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    hideProgressDialog();
                                    startActivity(new Intent(LoginActivity.this, InitialTutorial.class));
                                }
                            }
                        });
                    } else {
                        hideProgressDialog();
                        ((TextView)findViewById(R.id.name_error_text)).setText("Invalid name.");
                    }
                }
            });
        } else {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }


    }

    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isValidPassword(CharSequence target){
        return (target.length()>=6);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                signInWithGoogle(account);
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }


}