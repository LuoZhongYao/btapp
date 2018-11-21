package com.goodocom.wms.bluetooth;

import com.goodocom.wms.bluetooth.IBluetoothCallback;
import com.goodocom.wms.bluetooth.IBluetoothDeviceCallback;

interface IBluetoothService {
    void devRegister(String bdaddr, IBluetoothDeviceCallback cbk);
    void devUnregister(String bdaddr, IBluetoothDeviceCallback cbk);
    void register(IBluetoothCallback callback);
    void unregister(IBluetoothCallback callback);

    void ReadPairList();
    void Delete(String bdaddr);
	String LocalName();
	void SetLocalName(String name);
	String PinCode();
	void SetPinCode(String pincode);
	String LocalAddress();
	String Version();
	void Search();
	void CancelSearch();
	void Connect(String bdaddr);
	void EnableAutoConnect();
	void DisableAutoConnect();
	void EnableAutoAnswer();
	void DisableAutoAnswer();
	boolean IsAutoConnecte();
	boolean IsAutoAnswer();

    String DeviceNumber(String bdaddr);
	String DeviceName(String bdaddr);
	int DeviceSignal(String bdaddr);
	int DeviceBattchg(String bdaddr);
	boolean DeviceMicMuted(String bdaddr);
    String DevicePbapStatus(String bdaddr);
    String DeviceAudioDirection(String bdaddr);
    String DeviceHfpStatus(String bdaddr);
    String DeviceA2dpStatus(String bdaddr);
    String DeviceAvrcpStatus(String bdaddr);
    long DeviceAvrcpPlaybackPos(String bdaddr);
    List<String> DeviceAvrcpAttribute(String bdaddr);

    void DeviceDTMF(String bdaddr,String dtfm);
    void DeviceDial(String bdaddr,String number);
    void DeviceAudioToggle(String bdaddr);
    void DeviceHangup(String bdaddr);
    void DeviceAnswer(String bdaddr);
    void DeviceRedial(String bdaddr);
    void DeviceForward(String bdaddr);
    void DeviceBackward(String bdaddr);
    void DevicePause(String bdaddr);
    void DevicePlay(String bdaddr);
    void DeviceStop(String bdaddr);
    void DeviceMicToggle(String bdaddr);
    void DeviceVoiceToggle(String bdaddr);
    void DeviceDisconnect(String bdaddr);
    void DeviceSyncPhonebook(String bdaddr);
    void DeviceSyncHistory(String bdaddr);
    void DeviceCancelSync(String bdaddr);
    void DeviceAudioSource(String bdaddr);
}
