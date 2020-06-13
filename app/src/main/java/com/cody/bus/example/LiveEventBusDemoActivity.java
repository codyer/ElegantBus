package com.cody.bus.example;

import android.os.Bundle;

import com.cody.bus.example.event.Scope$demo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cody.live.event.bus.LiveEventBus;
import com.cody.live.event.bus.core.wrapper.ObserverWrapper;

public class LiveEventBusDemoActivity extends AppCompatActivity {
    private static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_event_bus_demo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                count++;
                Snackbar.make(view, "发送事件监听" + count, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                LiveEventBus.begin().inScope(Scope$demo.class).withEvent$testBean().setValue(new TestBean("count", ("count" + count)));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
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
            Toast.makeText(LiveEventBusDemoActivity.this, "注册事件监听", Toast.LENGTH_SHORT).show();
            LiveEventBus.begin().inScope(Scope$demo.class).withEvent$testBean()
                    .observe(LiveEventBusDemoActivity.this, new ObserverWrapper<TestBean>() {
                        @Override
                        public void onChanged(TestBean testBean) {
                            Toast.makeText(LiveEventBusDemoActivity.this, "事件监听" + testBean.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
