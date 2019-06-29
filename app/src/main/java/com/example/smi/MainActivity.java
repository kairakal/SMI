package com.example.smi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button fbButton,twitterButton,gmailButton;

    public void facebookButton(View view){
        Intent intent1= new Intent(getApplicationContext(),FacebookLogin.class);
startActivity(intent1);

    }
    public void twitterButton(View view){
        Intent intent2= new Intent(getApplicationContext(),TwitterLogin.class);
        startActivity(intent2);

    }
    public void gmailButton(View view){
        Intent intent= new Intent(getApplicationContext(),GmailLogin.class);
        startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbButton=findViewById(R.id.button1);
        twitterButton=findViewById(R.id.button2);
        gmailButton=findViewById(R.id.button);

    }
}
