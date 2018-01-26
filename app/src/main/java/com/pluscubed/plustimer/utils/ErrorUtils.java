package com.pluscubed.plustimer.utils;

import android.content.Context;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import com.pluscubed.plustimer.BuildConfig;

/**
 * Error Utility Methods
 */
public class ErrorUtils {

    public static void logCrashlytics(Exception e) {

    }

    public static void showErrorDialog(final Context context, String userReadableMessage,
                                       Exception e,
                                       boolean sendFileData) {
        try {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                    .title("Error")
                    .positiveText("Dismiss");
            /*if (sendFileData) {
                builder.negativeText(R.string.email_developer_history_data)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                sendFileDataEmail(context);
                            }
                        });
            }*/
            if (!userReadableMessage.equals("")) {
                builder.content(userReadableMessage + "\n" + e.getMessage());
            }
            builder.show();
        } catch (WindowManager.BadTokenException e2) {
            e.printStackTrace();
            e2.printStackTrace();
        }
    }

    public static void showJsonSyntaxError(Context c, Exception e) {
        logCrashlytics(e);
        showErrorDialog(c, "History/current data can't be read from storage.", e, true);
    }

    public static boolean isSolveNonexistent(Context c, String puzzleTypeId, String sessionId,
                                             int solveIndex) {
        //TODO
        /*try {
            PuzzleType.get(puzzleTypeId).getSession(sessionId).getSolveByPosition(solveIndex);
            return false;
        } catch (IndexOutOfBoundsException e) {
            if (BuildConfig.USE_CRASHLYTICS) {
                Crashlytics.log(Log.ERROR,
                        "Solve #" + solveIndex + " nonexistent",
                        PuzzleType.getCurrent()
                                .getSession(sessionId)
                                .toString(c, PuzzleType.getCurrentId(), true, true, true, false)
                );
            }
            showErrorDialog(c, "Solve #" + solveIndex + " doesn't exist", e, false);
            logCrashlytics(e);
            return true;
        }*/
        return false;
    }

    public static void sendFileDataEmail(Context context) {
        /*BufferedReader r = null;
        StringBuilder total = new StringBuilder();
        for (PuzzleType p : PuzzleType.getPuzzleTypes()) {
            for (int i = 0; i < 2; i++) {
                try {
                    String fileName;
                    if (i == 0) {
                        fileName = p.getHistoryFileName();
                    } else {
                        fileName = p.getCurrentSessionFileName();
                    }
                    total.append("\n\n\n").append(fileName).append("\n");
                    InputStream in = context.openFileInput(fileName);
                    r = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = r.readLine()) != null) {
                        total.append(line);
                    }
                } catch (FileNotFoundException e) {
                    // File not found: ignore
                } catch (IOException e) {
                    logCrashlytics(e);
                    showErrorDialog(context, "", e, false);
                } finally {
                    if (r != null) {
                        try {
                            r.close();
                        } catch (IOException e) {
                            logCrashlytics(e);
                            showErrorDialog(context, "", e, false);
                        }
                    }
                }
            }
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", "plusCubed@gmail.com", null));
        intent.putExtra(Intent.EXTRA_TEXT, total.toString());
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)));*/
    }
}
