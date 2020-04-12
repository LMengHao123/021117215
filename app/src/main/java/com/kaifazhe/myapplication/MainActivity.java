package com.kaifazhe.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private boolean isPreview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setFlags(flags, flags);
        //隐藏标题
        setContentView(R.layout.activity_main);

        if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "请安装SD card!", Toast.LENGTH_SHORT).show();
        }
        final SurfaceView surf = (SurfaceView) findViewById(R.id.surfaceView);//用于显示摄像头预览
        final SurfaceHolder surh = surf.getHolder();//获取surfaceHoder
        surh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置surfaceview不维护缓冲
        ImageButton i1 = (ImageButton) findViewById(R.id.imageButton2);
        i1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPreview) {
//                    //添加权限
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        System.out.println("ok");
//                    }else {
//                        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
//                    }
//                    //打开照相机
                    camera = Camera.open();
                    isPreview = true;

                }
                try {
                    camera.setPreviewDisplay(surh);//设置用于显示预览的surfaceview
                    Camera.Parameters par = camera.getParameters();//获取摄像头参数
                    par.setPictureFormat(PixelFormat.JPEG);//设置图片为jpg图片
                    par.set("jpeg-quality", 80);//设置图片质量

                    camera.setParameters(par);//重新设置摄像头参数
                    camera.startPreview();//开始预览
                    camera.autoFocus(null);//设置自动对焦

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        ImageButton i2 = (ImageButton) findViewById(R.id.imageButton);
        i2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //实现拍照功能
                if (camera != null) {
                    camera.takePicture(null, null, jpeg);
                }
            }
        });

    }

    final Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bit = BitmapFactory.decodeByteArray(data, 0, data.length);//根据拍照所得数据创建位图
            camera.stopPreview();
            isPreview = false;
            //保存到sd卡中

            File addp = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera");
            if (!addp.exists()) {
                addp.mkdir();
            }

            String filename = System.currentTimeMillis() + ".jpg";
            File file = new File(addp, filename);

            try {

                FileOutputStream fos = new FileOutputStream(file);
                bit.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.flush();
                fos.close();
            }
            catch(Exception e){}

            try{
                MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(), file.getAbsolutePath(),filename,null);

            }

            catch (Exception e){}

            MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("files://"+"")));
            Toast.makeText(MainActivity.this, "照片保存至："+file, Toast.LENGTH_SHORT).show();
            resetCamera();//重新预览
        }
    };
    private void resetCamera()
    {
        if(!isPreview)
        {
            camera.startPreview();
            isPreview=true;
        }
    }
    protected void onPause()
    {
        super.onPause();
        if(camera!=null)
        {
            camera.stopPreview();
            camera.release();
        }

    }


}
