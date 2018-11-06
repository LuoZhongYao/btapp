// IBluetoothCallback.aidl
package com.goodocom.wms.bluetooth;

// Declare any non-default types here with import statements

interface IBluetoothCallback {

    // device management
    void onDMAdd(String bdaddr);
    void onDMRemove(String bdaddr);

    void onInitComplete();
    void onInquiryComplete();
    void onAutoConnect(boolean auto);
    void onAutoAnswer(boolean auto);
    void onPairMode(boolean pairmode);
    void onVersion(String version);
    void onLocalBdaddr(String bdaddr);
    void onLocalName(String name);
    void onPoweron(boolean on);
    void onPin(String pin);
    void onPairlist(int index, String bdaddr, String name);
    void onInquiryResult(String bdaddr, String name);
}
