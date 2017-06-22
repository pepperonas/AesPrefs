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

    /**
     * The enum Aes setup.
     */
    enum AesSetup {
        /**
         * Inst date aes setup.
         */
        INST_DATE(".inst_date"),
        /**
         * Executions aes setup.
         */
        EXECUTIONS(".executions");

        private final String s;

        AesSetup(final String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
    }

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

    private static long mExecutionTime = 0;
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
     * @param context  the context
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
     * @param context  the context
     * @param password the password
     * @param logMode  the log mode
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
     * @param context  the context
     * @param filename the filename
     * @param password the password
     * @param logMode  the log mode
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
     * @param context  the context
     * @param filename the filename
     * @param password the password
     */
    public static void init(@NonNull Context context, @Nullable String filename, @NonNull final String password) {
        if (mLog != LogMode.NONE) {
            Log.i(TAG, "Initializing AesPrefs...");
        }

        mCtx = context.getApplicationContext();
        if (filename != null) {
            mFilename = filename;
        }
        //        String mPassword = password;

        SharedPreferences sp = mCtx
                .getSharedPreferences(mFilename, Context.MODE_PRIVATE);
        String mSecRand = sp.getString("sr", null);
        KeySet mKs;
        byte[] mSrBytes;
        if (mSecRand == null) {
            mKs = Crypt.getSecretKey(password, null);
            mKey = mKs.getSecretKey();
            mSrBytes = mKs.getSalt();
            mSecRand = Base64.encodeToString(mSrBytes, Base64.DEFAULT);
            sp.edit().putString("sr", mSecRand).apply();
        } else {
            mSrBytes = Base64.decode(sp.getString("sr", null), Base64.DEFAULT);
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
     * @param key   the key
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

        mExecutionTime += System.currentTimeMillis() - start;
    }

    /**
     * Get string.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string
     */
    @Nullable
    public static String get(@NonNull String key, @Nullable String defaultValue) {
        long start = System.currentTimeMillis();

        SharedPreferences sp = mCtx.getSharedPreferences(mFilename, Context.MODE_PRIVATE);

        byte[] e = toByte(sp.getString(key, ""));
        byte[] i = toByte(sp.getString(key + TAIL, ""));

        try {

            String de = Crypt.dec(mKey, i, e);
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, "get  " + key + " -> " + de);
            }
            mExecutionTime += System.currentTimeMillis() - start;
            return de;
        } catch (Exception ex) {
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                Log.d(TAG, ex.getMessage());
            }
            mExecutionTime += System.currentTimeMillis() - start;
            return defaultValue;
        }
    }

    /**
     * Init int boolean.
     *
     * @param key   the key
     * @param value the value
     * @return the boolean
     */
    public static boolean initInt(@NonNull String key, @Nullable Integer value) {
        Integer i;
        try {
            i = getInt(key, Integer.MIN_VALUE + 1);
            if (i == Integer.MIN_VALUE + 1) {
                putInt(key, value);
            }
        } catch (Exception e) {
            putInt(key, value);
            return true;
        }

        return i == Integer.MIN_VALUE + 1;
    }

    /**
     * Put int.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putInt(@NonNull String key, @Nullable Integer value) {
        put(key, String.valueOf(value));
    }

    /**
     * Gets int.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the int
     */
    @Nullable
    public static Integer getInt(@NonNull String key, @Nullable Integer defaultValue) {
        long start = System.currentTimeMillis();
        try {
            return Integer.valueOf(get(key, String.valueOf(defaultValue)));

        } catch (NumberFormatException nfe) {
            mExecutionTime += (System.currentTimeMillis() - start);
            return null;
        }
    }

    public static boolean initBool(@NonNull String key, @Nullable Boolean value) {
        Boolean b;
        try {
            b = getBool(key, null);
            if (b == null) {
                Log.i(TAG, "initBool: " + b);
                putBool(key, value);
                return true;
            }
        } catch (Exception e) {
            putBool(key, value);
            return true;
        }

        return false;
    }

    public static void putBool(@NonNull String key, @Nullable Boolean value) {
        put(key, String.valueOf(value));
    }

    @Nullable
    public static Boolean getBool(@NonNull String key, @Nullable Boolean defaultValue) {
        long start = System.currentTimeMillis();
        try {

            //noinspection ConstantConditions
            if (get(key, String.valueOf(defaultValue)).equals("null")) {
                return null;
            }

            return Boolean.valueOf(get(key, String.valueOf(defaultValue)));

        } catch (NumberFormatException nfe) {
            mExecutionTime += (System.currentTimeMillis() - start);
            return null;
        }
    }

    public static boolean initFloat(@NonNull String key, @Nullable Float value) {
        Float f;
        try {
            f = getFloat(key, Float.MIN_VALUE + 1F);
            if (f == Float.MIN_VALUE + 1F) {
                putFloat(key, value);
            }
        } catch (Exception e) {
            putFloat(key, value);
            return true;
        }

        return f == Float.MIN_VALUE + 1F;
    }

    /**
     * Put float.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putFloat(@NonNull String key, @Nullable Float value) {
        put(key, String.valueOf(value));
    }

    /**
     * Gets float.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the float
     */
    @Nullable
    public static Float getFloat(@NonNull String key, @Nullable Float defaultValue) {
        return Float.valueOf(get(key, String.valueOf(defaultValue)));
    }

    public static boolean initDouble(@NonNull String key, @Nullable Double value) {
        Double d;
        try {
            d = getDouble(key, Double.MIN_VALUE + 1D);
            if (d == Float.MIN_VALUE + 1D) {
                putDouble(key, value);
            }
        } catch (Exception e) {
            putDouble(key, value);
            return true;
        }

        return d == Double.MIN_VALUE + 1D;
    }

    /**
     * Put double.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putDouble(@NonNull String key, @Nullable Double value) {
        put(key, String.valueOf(value));
    }

    /**
     * Gets double.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the double
     */
    @Nullable
    public static Double getDouble(@NonNull String key, @Nullable Double defaultValue) {
        return Double.valueOf(get(key, String.valueOf(defaultValue)));
    }

    public static boolean initLong(@NonNull String key, @Nullable Long value) {
        Long l;
        try {
            l = getLong(key, Long.MIN_VALUE + 1L);
            if (l == Long.MIN_VALUE + 1L) {
                putLong(key, value);
            }
        } catch (Exception e) {
            putLong(key, value);
            return true;
        }

        return l == Long.MIN_VALUE + 1L;
    }

    /**
     * Put long.
     *
     * @param key   the key
     * @param value the value
     */
    public static void putLong(@NonNull String key, @Nullable Long value) {
        put(key, String.valueOf(value));
    }

    /**
     * Gets long.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the long
     */
    @Nullable
    public static Long getLong(@NonNull String key, @Nullable Long defaultValue) {
        return Long.valueOf(get(key, String.valueOf(defaultValue)));
    }

    /**
     * Store array.
     *
     * @param key    the key
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
        int size = getInt(key + "_size", null);
        try {
            List<String> strings = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                strings.add(get(key + "_" + i, ""));
            }
            return strings;
        } catch (Exception e) {
            if (mLog == LogMode.ALL || mLog == LogMode.GET) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }
    }

    /**
     * Gets execution time.
     *
     * @return the execution time
     */
    public static long getExecutionTime() {
        return mExecutionTime;
    }

    /**
     * Gets installed.
     *
     * @return the installed
     */
    public static Long getInstalled() {
        return getLong(AesSetup.INST_DATE.toString(), -1L);
    }

    /**
     * Gets executions.
     *
     * @return the executions
     */
    public static Long getExecutions() {
        return getLong(AesSetup.INST_DATE.toString(), -1L);
    }

}