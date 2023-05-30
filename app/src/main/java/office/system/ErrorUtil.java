package office.system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import office.common.ICustomDialog;
import office.constant.EventConstant;
import office.fc.OldFileFormatException;
import office.fc.poifs.filesystem.OfficeXmlFileException;

/* loaded from: classes2.dex */
public class ErrorUtil {
    public static final int BAD_FILE = 2;
    public static final int INSUFFICIENT_MEMORY = 0;
    public static final int OLD_DOCUMENT = 3;
    public static final int PARSE_ERROR = 4;
    public static final int PASSWORD_DOCUMENT = 6;
    public static final int PASSWORD_INCORRECT = 7;
    public static final int RTF_DOCUMENT = 5;
    public static final int SD_CARD_ERROR = 8;
    public static final int SD_CARD_NOSPACELEFT = 10;
    public static final int SD_CARD_WRITEDENIED = 9;
    public static final int SYSTEM_CRASH = 1;
    private static final String VERSION = "2.0.0.4";
    private static final SimpleDateFormat sdf_24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private File logFile;
    private AlertDialog message;
    private SysKit sysKit;

    public ErrorUtil(SysKit sysKit) {
        this.sysKit = sysKit;
        if (sysKit.getControl().getMainFrame().isWriteLog()) {
            File temporaryDirectory = sysKit.getControl().getMainFrame().getTemporaryDirectory();
            this.logFile = temporaryDirectory;
            if (temporaryDirectory != null && temporaryDirectory.exists() && this.logFile.canWrite()) {
                File file = new File(this.logFile.getAbsolutePath() + File.separatorChar + "ASReader");
                this.logFile = file;
                if (!file.exists()) {
                    this.logFile.mkdirs();
                }
                this.logFile = new File(this.logFile.getAbsolutePath() + File.separatorChar + "errorLog.txt");
            }
        }
    }

    public void writerLog(Throwable th) {
        writerLog(th, false);
    }

    public void writerLog(Throwable th, boolean z) {
        writerLog(th, z, true);
    }

    public void writerLog(Throwable th, boolean z, boolean z2) {
        try {
            if (!(th instanceof AbortReaderError)) {
                File file = this.logFile;
                if (file == null) {
                    th = new Throwable("SD CARD ERROR");
                } else if (file != null && file.exists() && !this.logFile.canWrite()) {
                    th = new Throwable("Write Permission denied");
                } else if (this.sysKit.getControl().getMainFrame().isWriteLog() && !(th instanceof OutOfMemoryError)) {
                    FileWriter fileWriter = new FileWriter(this.logFile, true);
                    PrintWriter printWriter = new PrintWriter((Writer) fileWriter, true);
                    printWriter.println();
                    printWriter.println("--------------------------------------------------------------------------");
                    printWriter.println("Exception occurs: " + sdf_24.format(Calendar.getInstance().getTime()) + "  " + VERSION);
                    th.printStackTrace(printWriter);
                    fileWriter.close();
                }
                if (z2) {
                    processThrowable(th, z);
                }
            }
        } catch (Exception unused) {
        } catch (OutOfMemoryError unused2) {
            this.sysKit.getControl().getMainFrame().getActivity().onBackPressed();
        }
    }

    private void processThrowable(final Throwable th, final boolean z) {
        final IControl control = this.sysKit.getControl();
        final Activity activity = control.getMainFrame().getActivity();
        if (control != null && activity != null) {
            if (control.isAutoTest()) {
                System.exit(0);
            } else if (this.message == null) {
                control.getActivity().getWindow().getDecorView().post(new Runnable() { // from class: com.adoc.office.system.ErrorUtil.1
                    @Override // java.lang.Runnable
                    public void run() {
                        int i;
                        try {
                            String str = "";
                            String th2 = th.toString();
                            if (th2.contains("SD")) {
                                str = control.getMainFrame().getLocalString("SD_CARD");
                                i = 8;
                            } else if (th2.contains("Write Permission denied")) {
                                str = control.getMainFrame().getLocalString("SD_CARD_WRITEDENIED");
                                i = 9;
                            } else if (th2.contains("No space left on device")) {
                                str = control.getMainFrame().getLocalString("SD_CARD_NOSPACELEFT");
                                i = 10;
                            } else {
                                if (!(th instanceof OutOfMemoryError) && !th2.contains("OutOfMemoryError")) {
                                    if (!th2.contains("no such entry") && !th2.contains("Format error") && !th2.contains("Unable to read entire header") && !(th instanceof OfficeXmlFileException) && !th2.contains("The text piece table is corrupted") && !th2.contains("Invalid header signature")) {
                                        if (th2.contains("The document is really a RTF file")) {
                                            str = control.getMainFrame().getLocalString("DIALOG_RTF_FILE");
                                            i = 5;
                                        } else if (th instanceof OldFileFormatException) {
                                            str = control.getMainFrame().getLocalString("DIALOG_OLD_DOCUMENT");
                                            i = 3;
                                        } else if (th2.contains("Cannot process encrypted office file")) {
                                            str = control.getMainFrame().getLocalString("DIALOG_CANNOT_ENCRYPTED_FILE");
                                            i = 6;
                                        } else if (th2.contains("Password is incorrect")) {
                                            str = control.getMainFrame().getLocalString("DIALOG_PASSWORD_INCORRECT");
                                            i = 7;
                                        } else if (z) {
                                            str = control.getMainFrame().getLocalString("DIALOG_PARSE_ERROR");
                                            i = 4;
                                        } else {
                                            Throwable th3 = th;
                                            if (!(th3 instanceof NullPointerException) && !(th3 instanceof IllegalArgumentException) && !(th3 instanceof ClassCastException)) {
                                                if (ErrorUtil.this.sysKit.isDebug()) {
                                                    str = control.getMainFrame().getLocalString("DIALOG_SYSTEM_CRASH");
                                                }
                                                i = 1;
                                            }
                                            str = control.getMainFrame().getLocalString("DIALOG_SYSTEM_CRASH");
                                            i = 1;
                                        }
                                    }
                                    str = control.getMainFrame().getLocalString("DIALOG_FORMAT_ERROR");
                                    i = 2;
                                }
                                str = control.getMainFrame().getLocalString("DIALOG_INSUFFICIENT_MEMORY");
                                i = 0;
                            }
                            if (str.length() > 0) {
                                control.getMainFrame().error(i);
                                control.actionEvent(EventConstant.APP_ABORTREADING, true);
                                if (!control.getMainFrame().isPopUpErrorDlg() || ErrorUtil.this.message != null) {
                                    ICustomDialog customDialog = control.getCustomDialog();
                                    if (customDialog != null) {
                                        customDialog.showDialog((byte) 3);
                                        return;
                                    }
                                    return;
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(str);
                                builder.setCancelable(false);
                                builder.setTitle(control.getMainFrame().getAppName());
                                builder.setPositiveButton(control.getMainFrame().getLocalString("BUTTON_OK"), new DialogInterface.OnClickListener() { // from class: com.adoc.office.system.ErrorUtil.1.1
                                    @Override // android.content.DialogInterface.OnClickListener
                                    public void onClick(DialogInterface dialogInterface, int i2) {
                                        ErrorUtil.this.message = null;
                                        activity.onBackPressed();
                                    }
                                });
                                ErrorUtil.this.message = builder.create();
                                ErrorUtil.this.message.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    public void dispose() {
        this.sysKit = null;
    }
}