package com.tanhd.library.smartpen;

import com.tqltech.tqlpencomm.Dot;
import com.tqltech.tqlpencomm.PenStatus;
import com.tqltech.tqlpencomm.listener.TQLPenSignal;

public class SmartPenCallback implements TQLPenSignal {
    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onReceiveDot(Dot dot) {

    }

    @Override
    public void onUpDown(boolean b) {

    }

    @Override
    public void onPenNameSetupResponse(boolean b) {

    }

    @Override
    public void onPenTimetickSetupResponse(boolean b) {

    }

    @Override
    public void onPenAutoShutdownSetUpResponse(boolean b) {

    }

    @Override
    public void onPenFactoryResetSetUpResponse(boolean b) {

    }

    @Override
    public void onPenAutoPowerOnSetUpResponse(boolean b) {

    }

    @Override
    public void onPenBeepSetUpResponse(boolean b) {

    }

    @Override
    public void onPenSensitivitySetUpResponse(boolean b) {

    }

    @Override
    public void onPenLedConfigResponse(boolean b) {

    }

    @Override
    public void onPenDotTypeResponse(boolean b) {

    }

    @Override
    public void onPenChangeLedColorResponse(boolean b) {

    }

    @Override
    public void onPenOTAMode(boolean b) {

    }

    @Override
    public void onReceivePenAllStatus(PenStatus penStatus) {

    }

    @Override
    public void onReceivePenMac(String s) {

    }

    @Override
    public void onReceivePenName(String s) {

    }

    @Override
    public void onReceivePenBtFirmware(String s) {

    }

    @Override
    public void onReceivePenTime(long l) {

    }

    @Override
    public void onReceivePenBattery(byte b, Boolean aBoolean) {

    }

    @Override
    public void onReceivePenMemory(byte b) {

    }

    @Override
    public void onReceivePenAutoPowerOnModel(Boolean aBoolean) {

    }

    @Override
    public void onReceivePenBeepModel(Boolean aBoolean) {

    }

    @Override
    public void onReceivePenAutoOffTime(byte b) {

    }

    @Override
    public void onReceivePenMcuVersion(String s) {

    }

    @Override
    public void onReceivePenCustomer(String s) {

    }

    @Override
    public void onReceivePenSensitivity(byte b) {

    }

    @Override
    public void onReceivePenType(byte b) {

    }

    @Override
    public void onReceivePenDotType(byte b) {

    }

    @Override
    public void onReceivePenDataType(byte b) {

    }

    @Override
    public void onReceivePenLedConfig(byte b) {

    }

    @Override
    public void onReceivePenEnableLed(Boolean aBoolean) {

    }

    @Override
    public void onReceivePenHandwritingColor(byte b) {

    }

    @Override
    public void onReceiveOIDFormat(long l) {

    }

    @Override
    public void onWriteCmdResult(int i) {

    }

    @Override
    public void onDownOfflineDataCmdResult(boolean b) {

    }

    @Override
    public void onOfflineDataListCmdResult(boolean b) {

    }

    @Override
    public void onOfflineDataList(int i) {

    }

    @Override
    public void onStartOfflineDownload(boolean b) {

    }

    @Override
    public void onFinishedOfflineDownload(boolean b) {

    }

    @Override
    public void onReceiveOfflineStrokes(Dot dot) {

    }

    @Override
    public void onDownloadOfflineProgress(int i) {

    }

    @Override
    public void onReceiveOfflineProgress(int i) {

    }

    @Override
    public void onPenConfirmRecOfflineDataResponse(boolean b) {

    }

    @Override
    public void onPenDeleteOfflineDataResponse(boolean b) {

    }

    @Override
    public void onReceivePenMcuUniqueCode(String s) {

    }
}
