package com.ekspres.ssc.smartwater2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Isi_Saldo extends Activity implements AdapterView.OnItemSelectedListener {

    Button btnsubmit;
    Spinner spin;
    Spinner spin1;
    String[] saldo = {"20.000","50.000","75.000","100.000"};
    String[] pembayaran = {"BRI","BCA","Mandiri","Alfamart","Indomaret"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isi__saldo);


        spin = (Spinner) findViewById(R.id.spinner1);
        spin1 = (Spinner) findViewById(R.id.spinner2);

        spin.setOnItemSelectedListener(this);
        ArrayAdapter<String> sa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, saldo);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin.setAdapter(sa);

        spin1.setOnItemSelectedListener(this);
        ArrayAdapter<String> sb = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, pembayaran);
        sa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spin1.setAdapter(sb);

        btnsubmit = (Button) findViewById(R.id.btnsubmit);
        btnsubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Isi_Saldo.this, Saldo.class);
                intent.putExtra("saldo", spin.getSelectedItem().toString());
                intent.putExtra("pembayaran", spin1.getSelectedItem().toString());
                startActivity(intent);
            }
        });

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub

    }
}