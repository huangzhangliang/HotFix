package com.webapps.hotfix;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by leon on 16/12/19.
 */

public class MyTestClass {
    public void testFix(Context context){
        int i = 10;
        int x = 0;
        Toast.makeText(context,"shit:"+i/x,Toast.LENGTH_SHORT).show();
    }
}
