/*
 * Copyright (c) 2017 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pepperonas.showcase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.pepperonas.aesprefs.AesPrefs;
import com.pepperonas.aesprefs.AesPrefs.LogMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String dy = String.valueOf(System.currentTimeMillis());

        AesPrefs.init(this, "password", LogMode.ALL);
        //        AesPrefs.put("some_key", "some_value");
        //        AesPrefs.putInt("key_001", 123);
        Log.i(TAG, "some_value? " + AesPrefs.get("some_key", null));
        Log.i(TAG, "123? " + AesPrefs.getInt("key_001", null));

        List<String> strings = new ArrayList<>();
        strings.add("hello");
        strings.add("world");
        AesPrefs.storeArray("STR_ARRAY", strings);
        Log.i(TAG, "" + AesPrefs.restoreArray("STR_ARRAY"));

        boolean isNew = AesPrefs.initInt("ix", 2);
        Log.i(TAG, "2? " + AesPrefs.getInt("ix", -1) + " new=" + isNew);
        isNew = AesPrefs.initInt("ix", 3);
        Log.i(TAG, "2? " + AesPrefs.getInt("ix", -1) + " new=" + isNew);

        boolean isNewBool = AesPrefs.initBool("bx" + dy, true);
        Log.i(TAG, "true? " + AesPrefs.getBool("bx" + dy, false) + " new=" + isNewBool);
        isNewBool = AesPrefs.initBool("bx" + dy, false);
        Log.i(TAG, "true? " + AesPrefs.getBool("bx" + dy, false) + " new=" + isNewBool);

        boolean isNewDouble = AesPrefs.initDouble("dx" + dy, 123.456D);
        Log.i(TAG, "123.456? " + AesPrefs.getDouble("dx" + dy, -1D) + " new=" + isNewDouble);
        isNewDouble = AesPrefs.initDouble("dx" + dy, 654.321D);
        Log.i(TAG, "123.456? " + AesPrefs.getDouble("dx" + dy, -1D) + " new=" + isNewDouble);

        boolean isNewFloat = AesPrefs.initFloat("fx" + dy, 123.456F);
        Log.i(TAG, "123.456? " + AesPrefs.getFloat("fx" + dy, -1F) + " new=" + isNewFloat);
        isNewFloat = AesPrefs.initFloat("fx" + dy, 654.321F);
        Log.i(TAG, "123.456? " + AesPrefs.getFloat("fx" + dy, -1F) + " new=" + isNewFloat);

        boolean isNewLong = AesPrefs.initLong("lx" + dy, 123456L);
        Log.i(TAG, "123456? " + AesPrefs.getLong("lx" + dy, -1L) + " new=" + isNewLong);
        isNewLong = AesPrefs.initLong("lx" + dy, 654321L);
        Log.i(TAG, "123456? " + AesPrefs.getLong("lx" + dy, -1L) + " new=" + isNewLong);

        AesPrefs.putInt("t-null-int", 1);
        Log.i(TAG, "1? " + AesPrefs.getInt("t-null-int", -1));
        AesPrefs.putInt("t-null-int", null);
        Log.i(TAG, "null? " + AesPrefs.getInt("t-null-int", -1));
        Log.i(TAG, "null-test-passed? " + (AesPrefs.getInt("t-null-int", -1) == null));

        loadPreferences();
    }

    public void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(".aesconfig", Context.MODE_PRIVATE);

        Map<String, ?> prefs = sp.getAll();
        for (String key : prefs.keySet()) {
            Object pref = prefs.get(key);
            String printVal = "";
            if (pref instanceof Boolean) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Float) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Integer) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Long) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof String) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Set<?>) {
                printVal = key + " : " + pref;
            }
            Log.d(TAG, "loadPreferences: " + printVal);
        }
    }

}
