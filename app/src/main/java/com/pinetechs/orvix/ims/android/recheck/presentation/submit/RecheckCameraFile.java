package com.pinetechs.orvix.ims.android.recheck.presentation.submit;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * One temporary full-resolution camera capture stored in app cache.
 */
public final class RecheckCameraFile {

    private final File file;
    private final Uri uri;

    private RecheckCameraFile(File file, Uri uri) {
        this.file = file;
        this.uri = uri;
    }

    public static RecheckCameraFile create(Context context) throws IOException {
        File directory = new File(context.getCacheDir(), "recheck-evidence");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create recheck camera cache");
        }
        File file = File.createTempFile("recheck-", ".jpg", directory);
        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
        );
        return new RecheckCameraFile(file, uri);
    }

    public void attachTo(Intent intent) {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.setClipData(ClipData.newRawUri("recheck-evidence", uri));
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public byte[] readAndDelete() {
        try {
            return read();
        } finally {
            delete();
        }
    }

    public void delete() {
        if (file.exists()) {
            file.delete();
        }
    }

    private byte[] read() {
        if (!file.isFile() || file.length() == 0L) {
            return null;
        }
        try (FileInputStream input = new FileInputStream(file);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            return null;
        }
    }
}
