package com.crysis.bookshelf;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// 序列化操作
public class ListOperator {
    //private String file="LocalCache.dat";
    public ArrayList<Bookitem> load(Context context, String file) {
        /*if ( !isExistDataCache(context, file) )
            return null;*/
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (ArrayList<Bookitem>) ois.readObject();
        } catch ( FileNotFoundException e ) {
        } catch ( Exception e ) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if ( e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch ( Exception e ) {
            }
            try {
                fis.close();
            } catch ( Exception e ) {
            }
        }
        return null;
    }

    public boolean save(Context context, Serializable ser, String file)
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(file, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch ( Exception e ) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> loadBookshelf(Context context) {
        String file = "Bookshelves.dat";
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (ArrayList<String>) ois.readObject();
        } catch ( FileNotFoundException e ) {
        } catch ( Exception e ) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if ( e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch ( Exception e ) {
            }
            try {
                fis.close();
            } catch ( Exception e ) {
            }
        }
        return null;
    }
    public ArrayList<String> loadLabel(Context context) {
        String file = "Labels.dat";
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (ArrayList<String>) ois.readObject();
        } catch ( FileNotFoundException e ) {
        } catch ( Exception e ) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if ( e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch ( Exception e ) {
            }
            try {
                fis.close();
            } catch ( Exception e ) {
            }
        }
        return null;
    }
}
