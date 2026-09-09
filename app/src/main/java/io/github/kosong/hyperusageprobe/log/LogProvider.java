package io.github.kosong.hyperusageprobe.log;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;

import io.github.kosong.hyperusageprobe.hook.HookTargets;

public final class LogProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            LogFileManager.cleanup(
                    getContext()
            );
        }

        return true;
    }

    @Override
    public Bundle call(
            String method,
            String arg,
            Bundle extras
    ) {
        if (!isAllowedCaller()) {
            Bundle denied =
                    new Bundle();

            denied.putString(
                    LogContract.KEY_RESULT,
                    LogContract.RESULT_DENIED
            );

            return denied;
        }

        if (getContext() == null) {
            return null;
        }

        if (LogContract.METHOD_WRITE.equals(method)) {
            String line =
                    extras == null
                            ? null
                            : extras.getString(
                                    LogContract.KEY_LINE
                            );

            LogFileManager.append(
                    getContext(),
                    line
            );

            Bundle result =
                    new Bundle();

            result.putString(
                    LogContract.KEY_RESULT,
                    LogContract.RESULT_OK
            );

            return result;
        }

        if (LogContract.METHOD_CLEAR.equals(method)) {
            LogFileManager.clear(
                    getContext()
            );

            Bundle result =
                    new Bundle();

            result.putString(
                    LogContract.KEY_RESULT,
                    LogContract.RESULT_OK
            );

            return result;
        }

        if (LogContract.METHOD_READ.equals(method)) {
            Bundle result =
                    new Bundle();

            result.putString(
                    LogContract.KEY_RESULT,
                    LogContract.RESULT_OK
            );

            result.putString(
                    LogContract.KEY_TEXT,
                    LogFileManager.readAll(
                            getContext()
                    )
            );

            return result;
        }

        return super.call(
                method,
                arg,
                extras
        );
    }

    private boolean isAllowedCaller() {
        if (getContext() == null) {
            return false;
        }

        String packageName =
                getCallingPackage();

        if (packageName == null) {
            return false;
        }

        if (!HookTargets.TARGET_PACKAGE.equals(
                packageName
        )) {
            return false;
        }

        int callingUid =
                Binder.getCallingUid();

        return callingUid != Binder.getUid();
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder
    ) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(
            Uri uri,
            ContentValues values
    ) {
        return null;
    }

    @Override
    public int delete(
            Uri uri,
            String selection,
            String[] selectionArgs
    ) {
        return 0;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs
    ) {
        return 0;
    }
}
