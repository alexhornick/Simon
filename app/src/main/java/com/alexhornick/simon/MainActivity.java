package com.alexhornick.simon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.play_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //start activity based on button clicked
        if(v.getId()==R.id.play_button){
            Intent intent=new Intent(this,PlayActivity.class);
            startActivity(intent);
        }

    }
}
