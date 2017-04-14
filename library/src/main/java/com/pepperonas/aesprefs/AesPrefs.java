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

package com.pepperonas.aesprefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import com.pepperonas.aesprefs.Crypt.CryptSet;
import com.pepperonas.aesprefs.Crypt.KeySet;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;

/**
 * The type Aes prefs.
 *
 * @author Martin Pfeffer
 * @see <a href="https://celox.io">https://celox.io</a>
 */
public class AesPrefs {

    private static final String TAIL = "=";

    /**
     * Constants
     */
    private static final String TAG = "AesPrefs";

    /**
     * Member
     */
    private static Context mCtx;

    private static String mFilename;

    private static long mDuration = 0;
    private static SecretKey mKey;

    /**
     * The enum Log mode.
     */
    public enum LogMode {
        /**
         * None log mode.
         */
        NONE(-1),
        /**
         * Default log mode.
         */
        DEFAULT(0),
        /**
         * Get log mode.
         */
        GET(1),
        /**
         * Set log mode.
         */
        SET(2),
        /**
         * All log mode.
         */
        ALL(3);

        private final int mode;


        /**
         * Instantiates a new Log mode.
         *
         * @param i the
         */
        LogMode(int i) {
            this.mode = i;
        }
    }


    private static LogMode mLog = LogMode.DEFAULT;


    /**
     * Log mode.
     *
     * @param logMode the log mode
     */
    public static void logMode(@NonNull LogMode logMode) {
        mLog = logMode;
    }


    /**
     * Init (Recommended).
     *
     * @param context the context
     * @param password the password
     */
    public static void init(@NonNull Context context,
        @NonNull String password) {
        mLog = LogMode.NONE;
        init(context, ".aesconfig", password);
    }


    /**
     * Init (Recommended).
     *
     * @param context the context
     * @param password the password
     * @param logMode the log mode
     */
    public static void init(@NonNull Context context,
        @NonNull String password, @Nullable LogMode logMode) {
        if (logMode == null) {
            mLog = LogMode.NONE;
        } else {
            mLog = logMode;
        }
        init(context, ".aesconfig", password);
    }

    /**
     * Init.
     *
     * @param context the context
     * @param filename the filename
     * @param password the password
     * @param logMode the log mode
     */
    public static void init(@NonNull Context context, @NonNull String filename,
        @NonNull String password, @Nullable LogMode logMode) {
        if (logMode == null) {
            mLog = LogMode.NONE;
        } else {
            mLog = logMode;
        }
        init(context, filename, password);
    }

    /**
     * Init.
     *
     * @param context the context
     * @param filename the filename
     * @param password the password
     */
    public static void init(@NonNull Context context, @Nullable String filename, @NonNull
    final String password) {
        if (mLog != LogMode.NONE) {
            Log.i(TAG, "Initializing AesPrefs...");
        }

        mCtx = context.getApplicationContext();
        mFilename = filename;
        String mPassword = password;

        SharedPreferences sp = mCtx
            .getSharedPreferences(mFilename, Context.MODE_PRIVATE);
        String mSecRand = sp.getString("_sr", null);
        KeySet mKs;
        byte[] mSrBytes;
        if (mSecRand == null) {
            mKs = Crypt.getSecretKey(password, null);
            mKey = mKs.getSecretKey();
            mSrBytes = mKs.getSalt();
            mSecRand = Base64.encodeToString(mSrBytes, Base64.DEFAULT);
            sp.edit().putString("sr", mSecRand).apply();
        } else {
            mSrBytes = Base64.decode(sp.getString("_sr", null), Base64.DEFAULT);
            mKs = Crypt.getSecretKey(password, mSrBytes);
            mKey = mKs.getSecretKey();
        }
    }


    private static String toB32(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private static byte[] toByte(String string) {
        return Base64.decode(string, Base64.DEFAULT);
    }


    /**
     * Put.
     *
     * @param key the key
     * @param value the value
     */
    public static void put(@NonNull String key, @Nullable String value) {
        long start = System.currentTimeMillis();

        CryptSet cs = Crypt.enc(mKey, value);
        String e = toB32(cs.getEncrypted());
        String i = toB32(cs.getIv());

        SharedPreferences sp = mCtx.getSharedPreferences(mFilename, Context.MODE_PRIVATE);
        sp.edit().putString(key, e).apply();
        sp.edit().putString(key + TAIL, i).apply();

        if (mLog == LogMode.ALL || mLog == LogMode.SET) {
            Log.d(TAG, "put " + key + " <- " + value);
        }

        mDuration += System.currentTimeMillis() - start;
    }


    /**
     * Get string.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the string
     */
    @Nullable
    public static String get(@NonNull String key, @Nullable String defaultValue) {
        long start = System.currentTimeMillis();

        SharedPreferences sp = mCtx.getSharedPreferences(mFilename, Context.MODE_PRIVATE);

        byte[] e = toByte(sp.getString(key, ""));
        byte[] i = toByte(sp.getString(key + TAIL, ""));

        String de = Crypt.dec(mKey, i, e);
        mDuration += System.currentTimeMillis() - start;
        if (mLog == LogMode.ALL || mLog == LogMode.GET) {
            Log.d(TAG, "get  " + key + " -> " + de);
        }
        return de;
    }


    /**
     * Put int.
     *
     * @param key the key
     * @param value the value
     */
    public static void putInt(@NonNull String key, @Nullable Integer value) {
        long start = System.currentTimeMillis();
        put(key, String.valueOf(value));
        mDuration += System.currentTimeMillis() - start;
    }

    /**
     * Gets int.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the int
     */
    @Nullable
    public static Integer getInt(@NonNull String key, @Nullable Integer defaultValue) {
        long start = System.currentTimeMillis();
        return Integer.valueOf(get(key, String.valueOf(defaultValue)));
    }


    /**
     * Put float.
     *
     * @param key the key
     * @param value the value
     */
    public static void putFloat(@NonNull String key, @Nullable Float value) {
        long start = System.currentTimeMillis();
        put(key, String.valueOf(value));
        mDuration += System.currentTimeMillis() - start;
    }

    /**
     * Gets float.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the float
     */
    @Nullable
    public static Float getFloat(@NonNull String key, @Nullable Float defaultValue) {
        long start = System.currentTimeMillis();
        return Float.valueOf(get(key, String.valueOf(defaultValue)));
    }

    /**
     * Put double.
     *
     * @param key the key
     * @param value the value
     */
    public static void putDouble(@NonNull String key, @Nullable Double value) {
        long start = System.currentTimeMillis();
        put(key, String.valueOf(value));
        mDuration += System.currentTimeMillis() - start;
    }

    /**
     * Gets double.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the double
     */
    @Nullable
    public static Double getDouble(@NonNull String key, @Nullable Double defaultValue) {
        long start = System.currentTimeMillis();
        return Double.valueOf(get(key, String.valueOf(defaultValue)));
    }


    /**
     * Put long.
     *
     * @param key the key
     * @param value the value
     */
    public static void putLong(@NonNull String key, @Nullable Long value) {
        long start = System.currentTimeMillis();
        put(key, String.valueOf(value));
        mDuration += System.currentTimeMillis() - start;
    }

    /**
     * Gets long.
     *
     * @param key the key
     * @param defaultValue the default value
     * @return the long
     */
    @Nullable
    public static Long getLong(@NonNull String key, @Nullable Long defaultValue) {
        long start = System.currentTimeMillis();
        return Long.valueOf(get(key, String.valueOf(defaultValue)));
    }


    /**
     * Store array.
     *
     * @param key the key
     * @param values the values
     */
    public static void storeArray(@NonNull String key, @Nullable List<String> values) {
        putInt(key + "_size", values.size());
        for (int i = 0; i < values.size(); i++) {
            put(key + "_" + i, values.get(i));
        }
    }

    /**
     * Restore array list.
     *
     * @param key the key
     * @return the list
     */
    public static List<String> restoreArray(@NonNull String key) {
        long start = System.currentTimeMillis();
        int size = getInt(key + "_size", null);
        try {
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                strings.add(get(key + "_" + i, ""));
            }
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}