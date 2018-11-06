package com.goodocom.wms.bluetooth

class BluetoothCallbackImpl(private val activity: MainActivity) : IBluetoothCallback.Stub() {
    var settings: LocalSettings? = null
    var devs: DeviceList? = null

    fun connectedDevices(dev: Array<BluetoothDeviceImpl>) {
       devs?.onConnectedDevice(dev)
    }
    override fun onDMAdd(bdaddr: String) = activity.dmAdd(bdaddr)
    override fun onDMRemove(bdaddr: String) = activity.dmRemove(bdaddr)
    override fun onInitComplete() {}
    override fun onInquiryComplete() = activity.broadcast("search") { it.putExtra("complete", "complete") }
    override fun onInquiryResult(bdaddr: String, name: String) = activity.broadcast("search") { it.putExtra("result", arrayOf(bdaddr, name)) }
    override fun onVersion(version: String) { settings?.onVersion(version) }
    override fun onLocalBdaddr(bdaddr: String) { settings?.onLocalBdaddr(bdaddr) }
    override fun onLocalName(name: String)  { settings?.onName(name) }
    override fun onPoweron(on: Boolean) { settings?.onPoweron(on)}
    override fun onPin(pin: String) { settings?.onPin(pin)}
    override fun onAutoConnect(auto: Boolean) { settings?.onAutoConnect(auto)}
    override fun onAutoAnswer(auto: Boolean) { settings?.onAutoAnswer(auto)}
    override fun onPairlist(index: Int, bdaddr: String, name: String) { devs?.onPairDevice(index, bdaddr, name) }

    override fun onPairMode(pairmode: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}