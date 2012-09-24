/**
*	NotifyService.java
*
*	@author Johan
*/

package se.chalmers.watchme.notifications;

import se.chalmers.watchme.activity.MainActivity;
import android.R;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;

public class NotifyService extends Service {
	
	public class ServiceBinder extends Binder {
		NotifyService getService() {
			return NotifyService.this;
		}
	}
	
	// ID to identify the notification
	private static final int NOTIFICATION = 666;
	
	/** Notification name */
	public static final String INTENT_NOTIFY = "watchme";
	
	private NotificationManager manager;
	private IBinder binder = new ServiceBinder();
	

	@Override
	public void onCreate() {
		this.manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID) {
		// If this service was started by AlarmTask, show a notification
		if(intent.getBooleanExtra(INTENT_NOTIFY, false)) {
			showNotification();
		}
		
		// If the service is killed, it's no big deal as we've delivered our notification
		return START_NOT_STICKY;
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return this.binder;
	}
	
	@TargetApi(16)
	private void showNotification() {
		CharSequence title = "Movie released";
		CharSequence text = "The movie is released!";
		int icon = R.drawable.ic_dialog_alert;
		
		// The intent to launch an activity if the user presses this notification
		PendingIntent pending = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

		// Build a notification

		Notification notification = new Notification.Builder(this)
			.setSmallIcon(icon)
			.setContentIntent(pending)
			.setContentTitle(title)
			.setContentText(text)
			.setAutoCancel(true)
			.build();
		
		// Send the notification to the system along with our id
		this.manager.notify(NOTIFICATION, notification);
		
		// Stop and finish
		this.stopSelf();
	}

}
