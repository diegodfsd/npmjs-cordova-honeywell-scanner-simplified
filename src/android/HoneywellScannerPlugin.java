package com.icsfl.rfsmart.honeywell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.ScannerNotClaimedException;

public class HoneywellScannerPlugin extends CordovaPlugin implements BarcodeReader.BarcodeListener {
    private static final String TAG = "HoneywellScanner";
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {

        super.initialize(cordova, webView);

        Context context = cordova.getActivity().getApplicationContext();
        AidcManager.create(context, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                if (barcodeReader != null) {
                    barcodeReader.addBarcodeListener(HoneywellScannerPlugin.this);

                    Map<String, Object> properties = new HashMap<String, Object>();
                    try {
                        // Set Symbologies On/Off
                        properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
                        properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);
                        // Set Max Code 39 barcode length
                        properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
                        // Turn on center decoding
                        properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        // Enable bad read response
                        properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
                        // Apply the settings
                        barcodeReader.setProperties(properties);
                        barcodeReader.claim();
                    } catch (ScannerUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext)
    throws JSONException {
        if (action.equals("softwareTriggerStart")) {
            if (barcodeReader != null) {
                try {
                    barcodeReader.softwareTrigger(true);
                } catch (ScannerNotClaimedException e) {
                    e.printStackTrace();
                    NotifyError("ScannerNotClaimedException");
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                 NotifyError("ScannerUnavailableException");
                }
            }
        } else if (action.equals("softwareTriggerStop")) {
            if (barcodeReader != null) {
                try {
                    barcodeReader.softwareTrigger(false);
                } catch (ScannerNotClaimedException e) {
                    e.printStackTrace();
                    NotifyError("ScannerNotClaimedException");
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                 NotifyError("ScannerUnavailableException");
                }
            }
        } else if (action.equals("listen") ) {
            this.callbackContext = callbackContext;
            PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
            if (barcodeReader != null) {
                try {
                   barcodeReader.softwareTrigger(false);
                } catch (ScannerNotClaimedException e) {
                    e.printStackTrace();
                    NotifyError("ScannerNotClaimedException2");
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                     NotifyError("ScannerUnavailableException2");
                }
            }
        } else if (action.equals("claim")) {
            if (barcodeReader != null) {
                try {
                    barcodeReader.claim();
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                    NotifyError("Scanner unavailable");
                }
            }
            if (barcodeReader != null) {
                try {
                   barcodeReader.softwareTrigger(false);
                } catch (ScannerNotClaimedException e) {
                    e.printStackTrace();
                    NotifyError("ScannerNotClaimedException2");
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                     NotifyError("ScannerUnavailableException2");
                }
            }
        } else if (action.equals("release")) {
            if (barcodeReader != null) {
                barcodeReader.release();
            }
            if (barcodeReader != null) {
                try {
                   barcodeReader.softwareTrigger(false);
                } catch (ScannerNotClaimedException e) {
                    e.printStackTrace();
                    NotifyError("ScannerNotClaimedException2");
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                     NotifyError("ScannerUnavailableException2");
                }
            }
         }
        return true;
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, barcodeReadEvent.getBarcodeData());
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
        if (barcodeReader != null) {
            try {
                barcodeReader.softwareTrigger(false);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                NotifyError("ScannerNotClaimedException2");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                    NotifyError("ScannerUnavailableException2");
            }
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        NotifyError("Scan has failed");
        if (barcodeReader != null) {
            try {
                barcodeReader.softwareTrigger(false);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                NotifyError("ScannerNotClaimedException2");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                    NotifyError("ScannerUnavailableException2");
            }
        }
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                NotifyError("The scanner is unavailable");
            }
        }
        if (barcodeReader != null) {
            try {
                barcodeReader.softwareTrigger(false);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                NotifyError("ScannerNotClaimedException2");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                    NotifyError("ScannerUnavailableException2");
            }
        }
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        if (barcodeReader != null) {
            barcodeReader.release();
        }
        if (barcodeReader != null) {
            try {
                barcodeReader.softwareTrigger(false);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                NotifyError("ScannerNotClaimedException2");
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                    NotifyError("ScannerUnavailableException2");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (barcodeReader != null) {
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            manager.close();
        }
    }

    private void NotifyError(String error) {
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
            result.setKeepCallback(true);
            this.callbackContext.sendPluginResult(result);
        }
    }
}