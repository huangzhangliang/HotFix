package com.webapps.hotfix;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String ODEX = "odex";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onTest(View view){
        new MyTestClass().testFix(this);
    }


    public void onFix(View view){
        fixBug();
    }

    private void fixBug() {
        File fileDir = getDir(ODEX, Context.MODE_PRIVATE);
        Log.v("FixDexUtils----------",fileDir.getAbsolutePath());
        String name = "classes2.dex";
        String filePath = fileDir.getAbsolutePath() + File.separator + name;
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
        }
        InputStream is;
        FileOutputStream os;

        try {
            is = getResources().getAssets().open(name);
            os = new FileOutputStream(filePath);

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1){
                os.write(buffer,0,len);
            }

            File f = new File(filePath);
            if (f.exists()){
                Toast.makeText(this,"dex重写成功",Toast.LENGTH_SHORT).show();
            }

            FixDexUtils.loadFixedDex(this);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
