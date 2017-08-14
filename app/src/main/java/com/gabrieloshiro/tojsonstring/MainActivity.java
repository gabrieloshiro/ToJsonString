package com.gabrieloshiro.tojsonstring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dog dog = new Dog();
        dog.age = 11;
        dog.name = "Yogi";
        dog.weight = 23.7;

        ((TextView) findViewById(R.id.to_json_string_text_view)).setText(com.gabrieloshiro.tojsonstring.sample.ToJsonString.toJsonString(dog));
    }
}
