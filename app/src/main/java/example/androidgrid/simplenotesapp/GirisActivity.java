package example.androidgrid.simplenotesapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import example.androidgrid.simplenotesapp.user_sign.LoginActivity;
import example.androidgrid.simplenotesapp.user_sign.RegisterActivity;

public class GirisActivity extends AppCompatActivity {

    private Button btnReg, btnLog;

    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnReg = (Button) findViewById(R.id.start_reg_btn);
        btnLog = (Button) findViewById(R.id.start_log_btn);

        fAuth = FirebaseAuth.getInstance();

        updateUI();

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    private void register(){
        Intent regIntent = new Intent(GirisActivity.this, RegisterActivity.class);
        startActivity(regIntent);
    }

    private void login(){
        Intent logIntent = new Intent(GirisActivity.this, LoginActivity.class);
        startActivity(logIntent);
    }

    private void updateUI(){
        if (fAuth.getCurrentUser() != null){
            Log.i("GirisActivity", "fAuth != null");
            Intent startIntent = new Intent(GirisActivity.this, MainActivity.class);
            startActivity(startIntent);
            finish();
        } else {

            Log.i("GirisActivity", "fAuth == null");
        }
    }

}
