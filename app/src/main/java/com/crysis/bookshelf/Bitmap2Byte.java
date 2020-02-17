package com.crysis.bookshelf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class Bitmap2Byte {
    // Bitmap转换为Byte数组
    public byte[] bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    // Byte数组转换为Bitmap
    public Bitmap byte2Bitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}
