/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.comm.ui.notifycation;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.activities.FeedsActivity;
import com.umeng.comm.ui.activities.PostFeedActivity;

import java.util.Random;

/**
 * 发布Feed时的消息提示
 */
public class PostNotifycation {

    private static NotificationManager mNotificationManager;
    /**
     * notify id
     */
    private static int mNotifyID = 0;

    /**
     * @param context
     */
    public static void showPostNotifycation(final Context context, String title, String content) {
        if (context == null) {
            return;
        }
        int iconResId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_camera");
        String sendFailString = ResFinder.getString("umeng_comm_send_failed");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(iconResId).setContentTitle(title)
                        .setContentText(content)
                        .setTicker(title);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, PostFeedActivity.class);
        boolean postResult = title.equals(sendFailString);
        resultIntent.putExtra(Constants.POST_FAILED, postResult);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(FeedsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(mNotifyID);

        if (!postResult) {
            mNotifyID = new Random().nextInt(10000);
        }
        if (mNotificationManager != null) {
            // mId allows you to update the notification later on.
            mNotificationManager.notify(mNotifyID, mBuilder.build());
        }
    }

    /**
     * 消除提醒
     */
    public static void clearPostNotifycation(Context context) {
        if (mNotificationManager == null && context != null) {
            mNotificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        } else if (mNotificationManager != null) {
            mNotificationManager.cancel(mNotifyID);
        }
    }
}
