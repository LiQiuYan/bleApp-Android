# NAME: bleApp-android
# SYSTEM: Support Android 4.4.2 +
# VERSION: 1.0.0.0
# USE: Used for transmission between Bluetooth devices and Android phones, and save the file to phone
# DESC:
  一款通用型蓝牙接收软件，该软件界面接单，易于使用。该软件运行在安卓系统高于4.4的手机中，通过ble4.0协议，接收蓝牙设备的数据，并将接收到的原始数据存放在   手机相应目录下。
  遵循LGPL开源许可。
  此软件已在Android 4.4 系统的开发板上进行过测试。
  此项目基于google code项目开发。
# TRANSFER PROTOCOL: BLE（Bluetooth Low Energy）GATT
  Service
  一个低功耗蓝牙设备可以定义许多 Service, Service 可以理解为一个功能的集合。设备中每一个不同的 Service 都有一个 128 bit 的 UUID 作为这个 Service 的   独立标志
 
  Characteristic
  在 Service 下面，又包括了许多的独立数据项，我们把这些独立的数据项称作 Characteristic。同样的，每一个 Characteristic 也有一个唯一的 UUID 作为标识   符。在 Android 开发中，建立蓝牙连接后，我们说的通过蓝牙发送数据给外围设备就是往这些 Characteristic 中的 Value 字段写入数据；外围设备发送数据给手机   就是监听这些 Charateristic 中的 Value 字段有没有变化，如果发生了变化，手机的 BLE API 就会收到一个监听的回调。
# SUPPORT SERVICES:
  1.Battery
  2.Device Firmware Update (DFU)
  3.Device Information
  4.Eddystone Configuration Service
  5.Health Thermometer
  6.Heart Rate
  7.Link Loss
  8.UART
  9.UriBeacon
  10.iBeacon
  11.file transfer
# DOWNLOAD LINK
# AUTHOR: LiQiuYan
