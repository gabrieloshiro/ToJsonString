package com.gabrieloshiro.tojsonstring;

import android.util.Log;

@ToJsonString
public class Dog {

    public String name;
    public int age;
    public double weight;

    public static void main(String[] args) {
        Dog dog = new Dog();

        dog.age = 11;
        dog.name = "Yogi";
        dog.weight = 23.7;

        Log.d("Dog", com.gabrieloshiro.tojsonstring.sample.ToJsonString.toJsonString(dog));
    }

}
