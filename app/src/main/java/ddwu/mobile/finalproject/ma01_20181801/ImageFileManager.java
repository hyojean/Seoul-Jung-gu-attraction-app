package ddwu.mobile.finalproject.ma01_20181801;

import android.content.Context;

import java.io.File;

public class ImageFileManager {

    private Context context;

    public ImageFileManager(Context context) {
        this.context = context;
    }

    /* 내부저장소에 저장한 모든 임시 파일을 제거 */
    public void clearSaveFilesOnInternal() {
        File internalStoragefiles = context.getFilesDir();
        File[] files = internalStoragefiles.listFiles();
        for (File target : files) {
            target.delete();
        }
    }

}
