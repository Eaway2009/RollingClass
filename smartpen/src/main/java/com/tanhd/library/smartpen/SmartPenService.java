package com.tanhd.library.smartpen;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.tqltech.tqlpencomm.Dot;
import com.tqltech.tqlpencomm.PenCommAgent;
import com.tqltech.tqlpencomm.PenStatus;
import com.tqltech.tqlpencomm.listener.TQLPenSignal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class SmartPenService {
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private class SmartPenEvent implements TQLPenSignal {
        private Handler mHandler;
        private ArrayList<SmartPenCallback> smartPenCallbacks = new ArrayList<>();

        public SmartPenEvent() {
            mHandler = new Handler();
        }

        public void register(SmartPenCallback eventListener) {
            if (!smartPenCallbacks.contains(eventListener)) {
                smartPenCallbacks.add(eventListener);
            }
        }

        public void unregister(SmartPenCallback eventListener) {
            if (smartPenCallbacks.contains(eventListener)) {
                smartPenCallbacks.remove(eventListener);
            }
        }

        public void unregisterAll() {
            smartPenCallbacks.clear();
        }

        private void onCallback(final Object... objects) {
            final String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();

            Log.i("SmartPenEvent", "API Callback " + methodName);
            Method[] methods = SmartPenCallback.class.getMethods();
            for (final Method method: methods) {
                if (method.getName().equals(methodName)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                for (SmartPenCallback callback: smartPenCallbacks) {
                                    method.invoke(callback, objects);
                                }

                            } catch (ConcurrentModificationException e) {
                                e.printStackTrace();
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                }
            }
        }

        @Override
        public void onConnected() {
            onCallback();
        }

        @Override
        public void onDisconnected() {
            onCallback();
        }

        @Override
        public void onReceiveDot(Dot dot) {
            onCallback(dot);
        }

        @Override
        public void onUpDown(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenNameSetupResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenTimetickSetupResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenAutoShutdownSetUpResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenFactoryResetSetUpResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenAutoPowerOnSetUpResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenBeepSetUpResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenSensitivitySetUpResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenLedConfigResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenDotTypeResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenChangeLedColorResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenOTAMode(boolean b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenAllStatus(PenStatus penStatus) {
            onCallback(penStatus);
        }

        @Override
        public void onReceivePenMac(String s) {
            onCallback(s);
        }

        @Override
        public void onReceivePenName(String s) {
            onCallback(s);
        }

        @Override
        public void onReceivePenBtFirmware(String s) {
            onCallback(s);
        }

        @Override
        public void onReceivePenTime(long l) {
            onCallback(l);
        }

        @Override
        public void onReceivePenBattery(byte b, Boolean aBoolean) {
            onCallback(b, aBoolean);
        }

        @Override
        public void onReceivePenMemory(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenAutoPowerOnModel(Boolean aBoolean) {
            onCallback(aBoolean);
        }

        @Override
        public void onReceivePenBeepModel(Boolean aBoolean) {
            onCallback(aBoolean);
        }

        @Override
        public void onReceivePenAutoOffTime(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenMcuVersion(String s) {
            onCallback(s);
        }

        @Override
        public void onReceivePenCustomer(String s) {
            onCallback(s);
        }

        @Override
        public void onReceivePenSensitivity(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenType(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenDotType(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenDataType(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenLedConfig(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenEnableLed(Boolean aBoolean) {
            onCallback(aBoolean);
        }

        @Override
        public void onReceivePenHandwritingColor(byte b) {
            onCallback(b);
        }

        @Override
        public void onReceiveOIDFormat(long l) {
            onCallback(l);
        }

        @Override
        public void onWriteCmdResult(int i) {
            onCallback(i);
        }

        @Override
        public void onDownOfflineDataCmdResult(boolean b) {
            onCallback(b);
        }

        @Override
        public void onOfflineDataListCmdResult(boolean b) {
            onCallback(b);
        }

        @Override
        public void onOfflineDataList(int i) {
            onCallback(i);
        }

        @Override
        public void onStartOfflineDownload(boolean b) {
            onCallback(b);
        }

        @Override
        public void onFinishedOfflineDownload(boolean b) {
            onCallback(b);
        }

        @Override
        public void onReceiveOfflineStrokes(Dot dot) {
            onCallback(dot);
        }

        @Override
        public void onDownloadOfflineProgress(int i) {
            onCallback(i);
        }

        @Override
        public void onReceiveOfflineProgress(int i) {
            onCallback(i);
        }

        @Override
        public void onPenConfirmRecOfflineDataResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onPenDeleteOfflineDataResponse(boolean b) {
            onCallback(b);
        }

        @Override
        public void onReceivePenMcuUniqueCode(String s) {
            onCallback(s);
        }
    }

    private PenCommAgent penCommAgent;
    private SmartPenEvent smartPenEvent = new SmartPenEvent();;
    private boolean mIsConnected = false;
    private final int vendorId = 3141;
    private final int productId = 28825;
    private UsbManager mUsbManager;
    private Context mContext;
    PendingIntent mPermissionIntent;

    private static SmartPenService mInstance = null;
    public static SmartPenService getInstance() {
        if (mInstance == null) {
            mInstance = new SmartPenService();
        }

        return mInstance;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mContext.registerReceiver(mUsbReceiver, makeUsbFilter());
        mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void tryToConnect() {
        if (!mIsConnected)
            searchDevicesAndConnect(vendorId, productId);
        else
            smartPenEvent.onConnected();
    }

    private boolean initUSB() {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        penCommAgent = PenCommAgent.GetInstance(mContext);
        penCommAgent.setTQLPenSignalListener(smartPenEvent);
        register(new SmartPenCallback() {
            @Override
            public void onConnected() {
                mIsConnected = true;
            }

            @Override
            public void onDisconnected() {
                mIsConnected = false;
            }
        });
        penCommAgent.setXYDataFormat(1);
        penCommAgent.initUSB();

        return false;
    }

    private void searchDevicesAndConnect(int vendorId, int productId) {
        initUSB();
        UsbDevice usbDevice = penCommAgent.findUSBDevice(vendorId, productId);
        if (usbDevice != null) {
            if (mUsbManager.hasPermission(usbDevice)) {
                penCommAgent.getUsbData(usbDevice);
                smartPenEvent.onConnected();
            } else {
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        } else {
            smartPenEvent.onDisconnected();
        }
    }

    private void close() {
        penCommAgent.close();
    }

    public void register(SmartPenCallback callback) {
        smartPenEvent.register(callback);
    }

    public void unregister(SmartPenCallback callback) {
        smartPenEvent.unregister(callback);
    }

    private IntentFilter makeUsbFilter() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(ACTION_USB_PERMISSION);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        return usbFilter;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (usbDevice != null) {
                        searchDevicesAndConnect(vendorId, productId);
                    }
                } else {
                    Toast.makeText(mContext.getApplicationContext(), "未获取USB权限", Toast.LENGTH_SHORT).show();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {//usb连接
                searchDevicesAndConnect(vendorId, productId);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {//usb断开
                penCommAgent.close();
                smartPenEvent.onDisconnected();
            }
        }
    };

    public void active(SmartPenCallback callback) {
        smartPenEvent.unregisterAll();
        smartPenEvent.register(callback);
    }

}
