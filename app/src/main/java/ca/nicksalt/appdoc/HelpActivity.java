package ca.nicksalt.appdoc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        WebView webView = findViewById(R.id.help_web);
        webView.loadUrl("file:///android_res/raw/help.html");
    }
}
