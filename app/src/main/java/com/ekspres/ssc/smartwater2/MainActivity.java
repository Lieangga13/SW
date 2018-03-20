package com.ekspres.ssc.smartwater2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    final Context context = this;
    Button saldo, record, readcard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saldo = (Button) findViewById(R.id.saldo);
        saldo.setOnClickListener(new klik());

        record = (Button) findViewById(R.id.record);
        record.setOnClickListener(new klik2());

        readcard = (Button) findViewById(R.id.readcard);
        readcard.setOnClickListener(new klik3());
    }

    class klik implements View.OnClickListener {
        public void onClick (View v)
        {
            Intent i = new Intent(MainActivity.this, Saldo.class);
            startActivity(i);
        }
    }
    class klik2 implements View.OnClickListener {
        public  void onClick (View v)
        {
            Intent i = new Intent(MainActivity.this, Record.class);
            startActivity(i);
        }
    }
    class  klik3 implements View.OnClickListener {
        public void onClick (View v)
        {
            Intent i = new Intent(MainActivity.this, Read_Card.class);
            startActivity(i);
        }
    }
}
