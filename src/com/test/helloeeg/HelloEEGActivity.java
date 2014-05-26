package com.test.helloeeg;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.*;

public class HelloEEGActivity extends Activity implements OnClickListener {
	BluetoothAdapter bluetoothAdapter;
	
	TextView tv;
	ScrollView sv;
	Button b;
	
	TGDevice tgDevice;
	final boolean rawEnabled = false;
	private int poor_signal = 25;
	private WebView mWebView = null;
	private Button lock_button = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //webview preload----------------------------------
        mWebView = (WebView)findViewById(R.id.webview);
		
		mWebView.setWebViewClient(mWebViewClient);
		mWebView.setWebChromeClient(mWebChromeClient); 
		mWebView.setBackgroundColor(Color.BLACK);
        //-------------------------------------------------
		sv = (ScrollView)findViewById(R.id.scrollView1);
		tv = (TextView)findViewById(R.id.textView1);
        tv.setText("");
        
        sv.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    sv.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
        lock_button = (Button)findViewById(R.id.button2);
        lock_button.setOnClickListener(this);
        

        tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n" );
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
        	// Alert user that Bluetooth is not available
        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }else {
        	/* create the TGDevice */
        	tgDevice = new TGDevice(bluetoothAdapter, handler);
        }  
    }
    
    @Override
    public void onDestroy() {
    	tgDevice.close();
        super.onDestroy();
    }
    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
            case TGDevice.MSG_STATE_CHANGE:

                switch (msg.arg1) {
	                case TGDevice.STATE_IDLE:
	                    break;
	                case TGDevice.STATE_CONNECTING:		                	
	                	tv.append("Connecting...\n");
	                	break;		                    
	                case TGDevice.STATE_CONNECTED:
	                	tv.append("Connected.\n");
	                	tgDevice.start();
	                    break;
	                case TGDevice.STATE_NOT_FOUND:
	                	tv.append("Can't find\n");
	                	break;
	                case TGDevice.STATE_NOT_PAIRED:
	                	tv.append("not paired\n");
	                	break;
	                case TGDevice.STATE_DISCONNECTED:
	                	tv.append("Disconnected mang\n");
                }

                break;
            case TGDevice.MSG_POOR_SIGNAL:
            	poor_signal = msg.arg1;
            		tv.append("PoorSignal: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_RAW_DATA:	  
            		//raw1 = msg.arg1;
            		//tv.append("Got raw: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_HEART_RATE:
        		tv.append("Heart rate: " + msg.arg1 + "\n");
                break;
            case TGDevice.MSG_ATTENTION:
            	String hold = "Attention: " + msg.arg1 + "\n";
            	if((poor_signal == 0) && (msg.arg1 >= 67)){
            		//unlock the door
            		mWebView.loadUrl("http://<ip-address>/ledon");
            		tv.setTextColor(Color.RED);
                	tv.append("Attention: " + msg.arg1 + "\n"); 
                	try {
                	    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                	    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                	    r.play();
                	} catch (Exception e) {
                	    e.printStackTrace();
                	}
            	}else{
                	tv.setTextColor(Color.WHITE);
                	tv.append("Attention: " + msg.arg1 + "\n");
            	}

            	break;
            case TGDevice.MSG_MEDITATION:

            	break;
            case TGDevice.MSG_BLINK:
            		tv.append("Blink: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_RAW_COUNT:
            		//tv.append("Raw Count: " + msg.arg1 + "\n");
            	break;
            case TGDevice.MSG_LOW_BATTERY:
            	Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
            	break;
            case TGDevice.MSG_RAW_MULTI:
            	//TGRawMulti rawM = (TGRawMulti)msg.obj;
            	//tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
            default:
            	break;
        }
        }
    };
    
    public void doStuff(View view) {
    	if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
    		tgDevice.connect(rawEnabled);   
    	//tgDevice.ena
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		mWebView.loadUrl("http://192.168.15.234:1337/ledoff");
		Toast.makeText(this, "The door locked", Toast.LENGTH_LONG).show();
	}

  	//Webview inner class-----------------------------------------------------

  	WebViewClient mWebViewClient = new WebViewClient() {
  		@Override
  		public boolean shouldOverrideUrlLoading(WebView view, String url) {
  			view.loadUrl(url);
  			return true;
  		}
  	};
  	

  	WebChromeClient mWebChromeClient = new WebChromeClient() {

  		@Override
  		public void onReceivedTitle(WebView view, String title) {
  			if ((title != null) && (title.trim().length() != 0)) {
  				setTitle(title);
  			}
  		}
  	};
  	//------------------------------------------------------------------------


}