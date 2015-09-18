package com.github.jhaucke.smarthome.washeragent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.jhaucke.smarthome.washeragent.service.MqttService;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private EditText etBrokerHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etBrokerHost = (EditText) findViewById(R.id.et_broker_host);

        Button btnStartService = (Button) findViewById(R.id.btn_start_service);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String brokerHost = etBrokerHost.getText().toString().trim();
                if (brokerHost.equals("")) {
                    Toast.makeText(context, "Insert broker host name!", Toast.LENGTH_SHORT).show();
                } else {

                    Intent intent = new Intent(context, MqttService.class);
                    intent.putExtra("BrokerHost", brokerHost);
                    startService(intent);
                }
            }
        });

        Button btnStopService = (Button) findViewById(R.id.btn_stop_service);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(context, MqttService.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
