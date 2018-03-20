package com.ekspres.ssc.smartwater2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Saldo extends Activity {

    final Context context = this;
    Button isi_saldo;
    String getSaldo, getPembayaran;
    TextView saldo, pembayaran;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saldo);

        saldo = (TextView) findViewById(R.id.txtsaldoanda);
        pembayaran = (TextView) findViewById(R.id.textView1);

        Intent i = getIntent();
        getSaldo = i.getStringExtra("saldo");
        getPembayaran = i.getStringExtra("pembayaran");

        saldo.setText("Saldo : " + getSaldo);
        pembayaran.setText("Pembayaran : " + getPembayaran);

        isi_saldo = (Button) findViewById(R.id.btnisisaldo);
        isi_saldo.setOnClickListener(new klik());
    }

    class klik implements View.OnClickListener {
        public void onClick (View v)
        {
            Intent i = new Intent(Saldo.this, Isi_Saldo.class);
            startActivity(i);
        }
    }
}
