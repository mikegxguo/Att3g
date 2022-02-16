package com.mitac.att3g;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SunsetReceiver extends BroadcastReceiver {
	private static String TAG = "SunsetReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.d(TAG, "Receive : action = " + action);
      if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
//          Intent service = new Intent(context, ATService.class);
//          service.putExtra(ATService.EXTRA_EVENT, ATService.EVENT_BOOT_COMPLETED);
//          context.startService(service);
          Intent service = new Intent(context, SunsetService.class);
          service.putExtra(SunsetService.EXTRA_EVENT, SunsetService.EVENT_BOOT_COMPLETED);
          context.startService(service);
      }
  }

}
