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

        AesPrefs.init(this, "password", LogMode.ALL);
        AesPrefs.put("some_key", "some_value");
        AesPrefs.putInt("key_001", 123);
        System.out.println(AesPrefs.get("some_key", null));
        System.out.println(AesPrefs.getInt("key_001", null));

        List<String> strings = new ArrayList<>();
        strings.add("hello");
        strings.add("world");
        AesPrefs.storeArray("STR_ARRAY", strings);
        System.out.println(AesPrefs.restoreArray("STR_ARRAY"));

        loadPreferences();
    }


    public void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(".aesconfig", Context.MODE_PRIVATE);

        Map<String, ?> prefs = sp.getAll();
        for (String key : prefs.keySet()) {
            Object pref = prefs.get(key);
            String printVal = "";
            if (pref instanceof Boolean) {
                printVal = key + " : " + (Boolean) pref;
            }
            if (pref instanceof Float) {
                printVal = key + " : " + (Float) pref;
            }
            if (pref instanceof Integer) {
                printVal = key + " : " + (Integer) pref;
            }
            if (pref instanceof Long) {
                printVal = key + " : " + (Long) pref;
            }
            if (pref instanceof String) {
                printVal = key + " : " + (String) pref;
            }
            if (pref instanceof Set<?>) {
                printVal = key + " : " + (Set<String>) pref;
            }
            Log.d(TAG, "loadPreferences: " + printVal);
        }
    }

}
