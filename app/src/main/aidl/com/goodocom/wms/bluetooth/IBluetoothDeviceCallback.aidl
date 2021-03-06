// IBluetoothDeviceCallback.aidl
package com.goodocom.wms.bluetooth;

// Declare any non-default types here with import statements

interface IBluetoothDeviceCallback {
    void onName(String name);
    void onSignal(int sig);
    void onBattchg(int chg);
    void onPbapStatus(String status);
    void onHfpStatus(String status);
    void onA2dpStatus(String status);
    void onAvrcpStatus(String status);
    void onAvrcpPlaybackPos(long pos);
    void onAvrcpBrowsingChangePathComplete(int status, int num_items);
    void onAvrcpBrowsingFolder(int type, long msb, long lsb, String display);
    void onAvrcpBrowsingMedia(int type, long msb, long lsb, String display, String title, String artist, String album);
    void onAvrcpBrowsingMediaPlayer(int id, int play_status, int major, int subtype, String display);
    void onAudioDirection(String dir);
    void onMicMuted(boolean mute);
    void onTalkingNumber(String number);
    void onIncomingNumber(String number);
    void onOutgoingNumber(String number);
    void onTwcHeldNumber(String number);
    void onTwcWaitNumber(String number);
    void onAvrcpAttribute(in List<String> attr);
    void onPhonebookItem(String name, String number);
    void onHistoryItem(String type, String name, String number, String date);
    void onPhonebookComplete();
    void onHistoryComplete();
}
