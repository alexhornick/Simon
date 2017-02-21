package com.alexhornick.simon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    int itemSelected=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.play_button).setOnClickListener(this);

        Spinner sp = (Spinner) findViewById(R.id.spinner);
        sp.setOnItemSelectedListener(this);
    }


    @Override
    public void onClick(View v) {
        //start activity based on button clicked
        if(v.getId()==R.id.play_button){
            Intent intent=new Intent(this,PlayActivity.class);
            intent.putExtra("version",itemSelected);
            startActivity(intent);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        itemSelected=position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
