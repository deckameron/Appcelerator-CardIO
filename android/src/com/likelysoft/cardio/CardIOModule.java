/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.likelysoft.cardio;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.util.TiConvert;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.titanium.util.TiIntentWrapper;

import android.app.Activity;
import android.content.Intent;

@Kroll.module(name="CardIO", id="com.likelysoft.cardio")
public class CardIOModule extends KrollModule
{
    private int MY_SCAN_REQUEST_CODE = 100;

	// Standard Debugging variables
	private static final String LCAT = "CardIOModule";
	private static final boolean DBG = TiConfig.LOGD;
    private boolean useCardioIcon = false;
    private boolean usePaypalIcon = true;

	public CardIOModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(LCAT, "inside onAppCreate");
	}

	// Methods
	// Scan a card
	@Kroll.method
	public void scanCard(KrollFunction callback) throws Exception {
        Log.d(LCAT, "inside CardIO scanCard");

		final Activity activity = TiApplication.getAppCurrentActivity();
		final TiActivitySupport activitySupport = (TiActivitySupport) activity;

		final TiIntentWrapper scanIntent = new TiIntentWrapper(new Intent(activity, CardIOActivity.class));

		scanIntent.setWindowId(TiIntentWrapper.createActivityName("CARDIOMODULE"));

		// Customize these values to suit your needs.
        scanIntent.getIntent().putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, !useCardioIcon); 
        scanIntent.getIntent().putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, usePaypalIcon); // default: true
		scanIntent.getIntent().putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: true
		scanIntent.getIntent().putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true); // default: false

		// Hides the manual entry button
		scanIntent.getIntent().putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true); // default: false

		CardIOResultHandler resultHandler = new CardIOResultHandler();

        resultHandler.callback = callback;
		resultHandler.activitySupport = activitySupport;
		resultHandler.scanIntent = scanIntent.getIntent();
		
		activity.runOnUiThread(resultHandler);
	}
	
	protected class CardIOResultHandler implements TiActivityResultHandler, Runnable {

		protected int code;
		protected KrollFunction callback;
		protected TiActivitySupport activitySupport;
		protected Intent scanIntent;

		public void run() {
			Log.d(LCAT, "inside CardIOResultHandler run");
			code = activitySupport.getUniqueResultCode();
			activitySupport.launchActivityForResult(scanIntent, code, this);
		}

		public void onError(Activity activity, int requestCode, Exception e) {
			String msg = "Problem with scanner; " + e.getMessage();
			Log.d(LCAT, "inside CardIOResultHandler onError " + msg);
			
			HashMap<String, String> callbackDict = new HashMap<String, String>();
			
			callbackDict.put("success", "false");
			
            callback.callAsync((KrollObject)callback, callbackDict);
		}

		public void onResult(Activity activity, int requestCode, int resultCode, Intent data) {
			String resultStr;
			HashMap<String, String> callbackDict = new HashMap<String, String>();

            Log.d(LCAT, "inside onResult");

			// process the results
			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
			    Log.d(LCAT, "got result");
			
				CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				// Never log a raw card number. Avoid displaying it, but if
				// necessary use getFormattedCardNumber()
				resultStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

				if (scanResult.isExpiryValid()) {
					resultStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
				}

				if (scanResult.cvv != null) {
					// Never log or display a CVV
					resultStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
				}

				// get all of the data in a hash for returning
				callbackDict.put("success", "true");
				callbackDict.put("cvv", TiConvert.toString(scanResult.cvv));
				callbackDict.put("expiryMonth", TiConvert.toString(scanResult.expiryMonth));
				callbackDict.put("expiryYear", TiConvert.toString(scanResult.expiryYear));
				callbackDict.put("cardNumber", scanResult.getFormattedCardNumber());

				callback.callAsync((KrollObject)callback, callbackDict);
			} 
			else {
				resultStr = "Scan was canceled.";

				callbackDict.put("success", "false");
				callbackDict.put("cancelled", resultStr);

				callback.callAsync((KrollObject)callback, callbackDict);
			}
			
			Log.d(LCAT, "Scan results: " + resultStr);
		}
	}
    
    @Kroll.setProperty
    @Kroll.method
    public void setCardIOLogo(boolean val) {
        useCardioIcon = !val;
    }
    
    @Kroll.setProperty
    @Kroll.method
    public void setPayPalLogo(boolean val) {
        usePaypalIcon = !val;
    }
    
    @Kroll.getProperty
    @Kroll.method
    public boolean getCardIOLogo() {
        return useCardioIcon;
    }
    
    @Kroll.getProperty
    @Kroll.method
    public boolean getPayPalLogo() {
        return usePaypalIcon;
    }

}
