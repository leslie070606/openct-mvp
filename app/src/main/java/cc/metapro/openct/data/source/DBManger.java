package cc.metapro.openct.data.source;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.BorrowInfo;
import cc.metapro.openct.data.ClassInfo;
import cc.metapro.openct.data.GradeInfo;
import cc.metapro.openct.university.UniversityInfo;

public class DBManger {

    private static SQLiteDatabase mDatabase;

    private static DBManger manger;

    private DBManger(Context context) {
        DBHelper DBHelper = new DBHelper(context);
        mDatabase = DBHelper.getWritableDatabase();
    }

    public static DBManger getInstance(Context context) {
        synchronized (DBManger.class) {
            if (manger == null) {
                synchronized (DBManger.class) {
                    manger = new DBManger(context);
                }
            }
        }
        return manger;
    }

    public static void closeDB() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    public UniversityInfo getUniversity(String abbr) {
        Cursor cursor = mDatabase.query(
                DBHelper.SCHOOL_TABLE, null,
                DBHelper.ABBR + "=? COLLATE NOCASE", new String[]{abbr},
                null, null, null);
        int n = cursor.getColumnCount();
        String[] content = cursor.getColumnNames();
        Map<String, String> strKvs = new HashMap<>(n);
        Map<String, Boolean> boolKvs = new HashMap<>(n);
        cursor.moveToFirst();
        for (int i = 0; i < n; i++) {
            switch (cursor.getType(i)) {
                case Cursor.FIELD_TYPE_STRING:
                    strKvs.put(content[i], cursor.getString(i));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    boolean s = cursor.getInt(i) == 1;
                    boolKvs.put(content[i], s);
                    break;
            }
        }
        cursor.close();

        UniversityInfo.SchoolInfo schoolInfo = new UniversityInfo.SchoolInfo(strKvs, boolKvs);
        UniversityInfo universityInfo = new UniversityInfo();
        String cmsJson = null;
        String libJson = null;
        Cursor cmsCursor = mDatabase.query(
                DBHelper.CMS_TABLE, null,
                DBHelper.SYS_NAME + "=? COLLATE NOCASE",
                new String[]{schoolInfo.cmsSys}, null, null, null
        );
        Cursor libCursor = mDatabase.query(
                DBHelper.LIB_TABLE, null,
                DBHelper.SYS_NAME + "=? COLLATE NOCASE",
                new String[]{schoolInfo.libSys}, null, null, null
        );
        cmsCursor.moveToFirst();
        libCursor.moveToFirst();

        String[] titles = libCursor.getColumnNames();
        int i = 0;
        for (String s : titles) {
            if (s.equalsIgnoreCase(DBHelper.JSON)) {
                cmsJson = cmsCursor.getString(i);
                libJson = libCursor.getString(i);
                break;
            }
            i++;
        }
        cmsCursor.close();
        libCursor.close();

        Gson gson = new Gson();
        UniversityInfo.CMSInfo cmsInfo = gson.fromJson(cmsJson, UniversityInfo.CMSInfo.class);
        cmsInfo.mCmsURL = schoolInfo.cmsURL;
        cmsInfo.mDynLoginURL = schoolInfo.cmsDynURL;
        cmsInfo.mNeedCAPTCHA = schoolInfo.cmsCaptcha;

        UniversityInfo.LibraryInfo libraryInfo = gson.fromJson(libJson, UniversityInfo.LibraryInfo.class);
        libraryInfo.mLibURL = schoolInfo.libURL;
        libraryInfo.mNeedCAPTCHA = schoolInfo.libCaptcha;

        universityInfo.mCMSInfo = cmsInfo;
        universityInfo.mLibraryInfo = libraryInfo;

        return universityInfo;
    }

    public void updateClassInfos(List<ClassInfo> classInfos) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.CLASS_TABLE, null, null);
            for (ClassInfo c : classInfos) {
                mDatabase.execSQL(
                        "INSERT INTO " + DBHelper.CLASS_TABLE + " VALUES(null, ?)",
                        new Object[]{c.toString()}
                );
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public List<ClassInfo> getClassInfos() {
        Cursor cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<ClassInfo> classInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            classInfos.add(gson.fromJson(cursor.getString(1), ClassInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return classInfos;
    }

    public void updateGradeInfos(List<GradeInfo> gradeInfos) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.GRADE_TABLE, null, null);
            for (GradeInfo g : gradeInfos) {
                mDatabase.execSQL(
                        "INSERT INTO " + DBHelper.GRADE_TABLE + " VALUES(null, ?)",
                        new Object[]{g.toString()}
                );
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public List<GradeInfo> getGradeInfos() {
        Cursor cursor = mDatabase.query(DBHelper.GRADE_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<GradeInfo> gradeInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            gradeInfos.add(gson.fromJson(cursor.getString(1), GradeInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return gradeInfos;
    }

    public void updateBorrowInfos(List<BorrowInfo> borrowInfos) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.BORROW_TABLE, null, null);
            for (BorrowInfo b : borrowInfos) {
                mDatabase.execSQL(
                        "INSERT INTO " + DBHelper.BORROW_TABLE + " VALUES(null, ?)",
                        new Object[]{b.toString()}
                );
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    public List<BorrowInfo> getBorrowInfos() {
        Cursor cursor = mDatabase.query(DBHelper.BORROW_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<BorrowInfo> borrowInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            borrowInfos.add(gson.fromJson(cursor.getString(1), BorrowInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return borrowInfos;
    }
}
