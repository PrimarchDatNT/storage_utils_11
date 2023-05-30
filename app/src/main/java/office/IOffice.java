//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package office;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import office.common.IOfficeToPicture;
import office.res.ResConstant;
import office.system.IMainFrame;
import office.system.MainControl;

public abstract class IOffice implements IMainFrame {
    private MainControl control;
    private String tempFilePath;
    private boolean writeLog = true;

    public IOffice() {
        this.initControl();
    }

    private void initControl() {
        MainControl var1 = new MainControl(this);
        this.control = var1;
        var1.setOffictToPicture(new IOfficeToPicture() {
            private Bitmap bitmap;

            public void callBack(Bitmap var1) {
                IOffice.this.saveBitmapToFile(var1);
            }

            public void dispose() {
            }

            public Bitmap getBitmap(int var1, int var2) {
                if (var1 != 0 && var2 != 0) {
                    Bitmap var3 = this.bitmap;
                    if (var3 == null || var3.getWidth() != var1 || this.bitmap.getHeight() != var2) {
                        var3 = this.bitmap;
                        if (var3 != null) {
                            var3.recycle();
                        }

                        this.bitmap = Bitmap.createBitmap(var1, var2, Config.ARGB_8888);
                    }

                    return this.bitmap;
                } else {
                    return null;
                }
            }

            public byte getModeType() {
                return 1;
            }

            public boolean isZoom() {
                return false;
            }

            public void setModeType(byte var1) {
            }
        });
    }

    private void saveBitmapToFile(Bitmap var1) {
        if (var1 != null) {
            StringBuilder var2;
            if (this.tempFilePath == null) {
                if ("mounted".equals(Environment.getExternalStorageState())) {
                    this.tempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                }

                var2 = new StringBuilder();
                var2.append(this.tempFilePath);
                var2.append(File.separatorChar);
                var2.append("tempPic");
                File var8 = new File(var2.toString());
                if (!var8.exists()) {
                    var8.mkdir();
                }

                this.tempFilePath = var8.getAbsolutePath();
            }

            var2 = new StringBuilder();
            var2.append(this.tempFilePath);
            var2.append(File.separatorChar);
            var2.append("export_image.jpg");
            File var3 = new File(var2.toString());

            try {
                try {
                    if (var3.exists()) {
                        var3.delete();
                    }

                    var3.createNewFile();
                    FileOutputStream var9 = new FileOutputStream(var3);
                    var1.compress(CompressFormat.JPEG, 100, var9);
                    var9.flush();
                    var9.close();
                } catch (IOException var6) {
                }

            } finally {
                ;
            }
        }
    }

    public void changePage() {
    }

    public void changeZoom() {
    }

    public void completeLayout() {
    }

    public void destroyEngine() {
    }

    public void dispose() {
        MainControl var1 = this.control;
        if (var1 != null) {
            var1.dispose();
            this.control = null;
        }

    }

    public boolean doActionEvent(int var1, Object var2) {
        Exception var10000;
        boolean var10001;
        if (var1 != 20) {
            if (var1 != 788529152) {
                if (var1 != 1073741828) {
                    return false;
                }

                return true;
            }

            try {
                String var5 = ((String) var2).trim();
                if (var5.length() > 0 && this.control.getFind().find(var5)) {
                    this.setFindBackForwardState(true);
                }

                return true;
            } catch (Exception var3) {
                var10000 = var3;
                var10001 = false;
            }
        } else {
            try {
                this.updateToolsbarStatus();
                return true;
            } catch (Exception var4) {
                var10000 = var4;
                var10001 = false;
            }
        }

        Exception var6 = var10000;
        this.control.getSysKit().getErrorKit().writerLog(var6);
        return true;
    }

    public void error(int var1) {
    }

    public abstract Activity getActivity();

    public abstract String getAppName();

    public int getBottomBarHeight() {
        return 0;
    }

    public MainControl getControl() {
        return this.control;
    }

    public String getLocalString(String var1) {
        return ResConstant.getString(var1);
    }

    public byte getPageListViewMovingPosition() {
        return 0;
    }

    public String getTXTDefaultEncode() {
        return "UTF-8";
    }

    public abstract File getTemporaryDirectory();

    public int getTopBarHeight() {
        return 0;
    }

    public View getView() {
        return this.control.getView();
    }

    public Object getViewBackground() {
        return Color.parseColor("#e0e0e0");
    }

    public byte getWordDefaultView() {
        return 0;
    }

    public boolean isChangePage() {
        return true;
    }

    public boolean isDrawPageNumber() {
        return true;
    }

    public boolean isIgnoreOriginalSize() {
        return false;
    }

    public boolean isPopUpErrorDlg() {
        return true;
    }

    public boolean isShowFindDlg() {
        return true;
    }

    public boolean isShowPasswordDlg() {
        return true;
    }

    public boolean isShowProgressBar() {
        return true;
    }

    public boolean isShowTXTEncodeDlg() {
        return true;
    }

    public boolean isShowZoomingMsg() {
        return true;
    }

    public boolean isThumbnail() {
        return false;
    }

    public boolean isTouchZoom() {
        return true;
    }

    public boolean isWriteLog() {
        return this.writeLog;
    }

    public boolean isZoomAfterLayoutForWord() {
        return true;
    }

    public boolean onEventMethod(View var1, MotionEvent var2, MotionEvent var3, float var4, float var5, byte var6) {
        return false;
    }

    public void openFile(String var1) {
        this.getControl().openFile(var1);
    }

    public void setFindBackForwardState(boolean var1) {
    }

    public void setIgnoreOriginalSize(boolean var1) {
    }

    public void setThumbnail(boolean var1) {
    }

    public void setWriteLog(boolean var1) {
        this.writeLog = var1;
    }

    public void showProgressBar(boolean var1) {
    }

    public void updateToolsbarStatus() {
    }

    public void updateViewImages(List<Integer> var1) {
    }
}
