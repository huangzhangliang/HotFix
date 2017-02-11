package com.webapps.hotfix;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * Created by leon on 16/12/19.
 */

public class FixDexUtils {

    private static HashSet<File> loadedDex = new HashSet<>();

    static {
        loadedDex.clear();
    }

    public static void loadFixedDex (Context context){
        if (context == null){
            return;
        }
        File fileDir = context.getDir(MainActivity.ODEX, Context.MODE_PRIVATE);
        Log.v("FixDexUtils----------",fileDir.getAbsolutePath());
        File[] listFiles = fileDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("classes")&&file.getName().endsWith(".dex")){
                loadedDex.add(file);
            }
        }
        // 和之前apk里面的dex合并
        doDexInject(context,fileDir,loadedDex);
    }


    private static void doDexInject(Context context,File fileDir,HashSet<File> loadedDex){
        String optimizeDir = fileDir.getAbsolutePath() + File.separator + "opt_dex";
        File fot = new File(optimizeDir);
        if (!fot.exists()){
            fot.mkdirs();
        }

        Log.v("FixDexUtils----------",fot.getAbsolutePath());

        // 加载应用程序的dex
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();

        // 拿到自己的dex
        for (File dex : loadedDex) {
            DexClassLoader dexClassLoader = new DexClassLoader(
                    dex.getAbsolutePath(),//String dexPath
                    fot.getAbsolutePath(),//String optimizedDirectory
                    null,//String librarySearchPath
                    pathClassLoader//ClassLoader parent
            );

            try {
                Object dexObj = getPathList(dexClassLoader);
                Object pathObj = getPathList(pathClassLoader);

                Object mDexElementsList = getDexElements(dexObj);
                Object mPathElementsList = getDexElements(pathObj);

                // 合并
                Object dexElementsList = combineArray(mDexElementsList,mPathElementsList);
                Object pathList = getPathList(pathClassLoader);
                setField(pathList,pathList.getClass(),"dexElements",dexElementsList);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj,obj.getClass(),"dexElements");
    }

    private static void setField(Object object,Class<?> cl,String field,Object value) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(object,value);
    }

    private static Object getField(Object object,Class<?> cl,String field) throws NoSuchFieldException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(object);
    }

    private static Object getPathList(Object pathClassLoader) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return getField(pathClassLoader,Class.forName("dalvik.system.BaseDexClassLoader"),"pathList");
    }

    private static Object combineArray(Object arrayLhs,Object arrayRhs){
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass,j);
        for (int k = 0;k<j;k++){
            if (k<i){
                Array.set(result,k,Array.get(arrayLhs,k));
            }else{
                Array.set(result,k,Array.get(arrayRhs,k - 1));
            }
        }
        return  result;
    }

}
