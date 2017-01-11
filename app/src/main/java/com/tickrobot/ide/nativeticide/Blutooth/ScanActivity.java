package com.tickrobot.ide.nativeticide.Blutooth;

import static com.tickrobot.ide.nativeticide.tools.StringUtils.to2String;
import static com.tickrobot.ide.nativeticide.tools.Tools.mRxBle;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tickrobot.ide.nativeticide.MainActivity;
import com.tickrobot.ide.nativeticide.R;
import com.tickrobot.ide.nativeticide.tools.ReadProgram;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * Created by xushun on 2017/1/7.
 */

public class ScanActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    @BindView(R.id.ble_listview)
    ListView bleListview;
    @BindView(R.id.send_btn)
    Button sendBtn;

    private String mMsgSend;

    private static final String TAG = ScanActivity.class.getSimpleName();

//    private RxBle mRxBle;

    private StringBuffer mStringBuffer;


    byte[] program;
    private int responseOk = 0;
    private int sendHexFlag = 0;
    int size = 0;
    int address = 0;
    int programIndex = 0;

    int scanTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);
        program = ReadProgram.readProgram();
        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                requestMultiplePermissions();
            }
        }

        mStringBuffer = new StringBuffer();
        mRxBle = mRxBle.getInstance().setTargetDevice("xushun");
        mRxBle.openBle(this);
        mRxBle.scanBleDevices(true);
        mRxBle.receiveData().subscribe(new Action1<String>() {
            @Override
            public void call(String receiveData) {
                byte[] tmp_byte = receiveData.getBytes();
                if (0 == tmp_byte.length) {
                    return;
                }
                String tmp = "";
                for (int i = 0; i < tmp_byte.length; i++) {
                    String hex = Integer.toHexString(tmp_byte[i] & 0xFF);
                    if (hex.length() == 1) {
                        hex = '0' + hex;
                    }
                    tmp += ' ';
                    tmp = tmp + hex;
                }
                System.out.println("接收到数据：" + tmp);
                if (tmp.endsWith(Integer.toHexString(0x10 & 0xff))) {
                    if (sendHexFlag == 0) {
                        responseOk++;
                    }
                    System.out.println("responseOk:" + responseOk + ";");
                    switch (responseOk){
                        case 5:

                            mRxBle.sendData(mRxBle.getMsg("418120418220", 1));//查询软件主次版本号码
                            break;
                        case 6: //查询设置参数

                            mRxBle.sendData(mRxBle.getMsg("428600000101010103ffffffff008004000000", 1));
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mRxBle.sendData(mRxBle.getMsg("800020", 1));
                            break;
                        case 7:// 并行编程
                            mRxBle.sendData(mRxBle.getMsg("450504d7c20020", 1));
                            break;
                        case 8://进入设置模式。
                            mRxBle.sendData(mRxBle.getMsg("5020", 1));     //进入设置模式。
                            break;
                        case 9://获取设备签名。
                            mRxBle.sendData(mRxBle.getMsg("7520", 1)); //获取设备签名。
                            break;
                        case 10://发送Hex文件
                            sendHexFlag++;

                            if (sendHexFlag % 2 == 1) {   //发送地址
                                int laddress = address % 256;
                                int haddress = address / 256;
                                address += 64;
                                System.out.println("发送地址。" + address);
                                String addressPackage = "";
                                addressPackage = "55" + to2String(laddress) + to2String(haddress) + "20";
                                mRxBle.sendData(mRxBle.getMsg(addressPackage, 1));
                            } else { //发送内容
                                if (program.length - programIndex < 128) {
                                    size = program.length - programIndex;
                                } else {
                                    size = 128;
                                }
                                System.out.println("programming page size: " + size);
                                if (size < 128) {
                                    int tempindex = programIndex;
                                    byte[] lastbytes = new byte[128];
                                    for (int i = 0; i < size - 6; i++) {
                                        lastbytes[i] = program[tempindex++];
                                    }
                                    for (int i = (size - 6); i < 128; i++) {
                                        lastbytes[i] = (byte) 0xff;
                                    }
                                    String size2str = to2String(128);
                                    String oneLine = "";
                                    for (int i = 0; i < 128; i++) {
                                        String hex = Integer.toHexString(lastbytes[i] & 0xff);
                                        if (hex.length() == 1) {
                                            hex = "0" + hex;
                                        }
                                        oneLine += hex;
                                    }
                                    oneLine = "6400" + size2str + "46" + oneLine + "20";
                                    System.out.println("长度：" + oneLine.length());
                                    for (int i = 0; i < ((int) (oneLine.length() / 38)); i++) {
                                        String ss = oneLine.substring(i * 38, i * 38 + 38);
                                        mRxBle.sendData(mRxBle.getMsg(ss, 1));
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    String size2str = to2String(size);
                                    String oneLine = "";
                                    for (int i = 0; i < size; i++) {
                                        String hex = Integer.toHexString(program[programIndex++] & 0xff);
                                        if (hex.length() == 1) {
                                            hex = "0" + hex;
                                        }
                                        oneLine += hex;
                                    }
                                    oneLine = "6400" + size2str + "46" + oneLine + "20";

                                    for (int i = 0; i < ((int) (oneLine.length() / 38)); i++) {
                                        String ss = oneLine.substring(i * 38, i * 38 + 38);
                                        mRxBle.sendData(mRxBle.getMsg(ss, 1));
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if (size != 0x80) {
                                    sendHexFlag = 0;
                                }

                            }
                            //           System.out.println("program.length:"+program.length + "    programindex   "+ programIndex);
                            System.out.println("progress:"+programIndex * 100 / program.length);
                            break;
                        case 11:
                            System.out.println("program index: " + programIndex);
                            System.out.println("leaving programming mode");
                            mRxBle.sendData(mRxBle.getMsg("5120", 1));
//                            downloadVave.setProgress(100);

                            Toast.makeText(ScanActivity.this, "下载成功", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }

                }


            }
        });
        mRxBle.setScanListener(new RxBle.BleScanListener() {
            @Override
            public void onBleScan(BluetoothDevice bleDevice, int rssi, byte[] scanRecord) {
                System.out.println("name:" + bleDevice.getName() + ";");

                int i = 0;
                // 检查是否是搜索过的设备，并且更新
                for (i = 0; i < scan_devices.size(); i++) {
                    if (0 == bleDevice.getAddress().compareTo(
                            scan_devices.get(i).GetDevice().getAddress())) {
                        scan_devices.get(i).ReflashInf(bleDevice, rssi, scanRecord); // 更新信息
                        return;
                    }
                }

                // 增加新设备
                scan_devices.add(new MTBeacon(bleDevice, rssi, scanRecord));

//                if (bleDevice.getName().contains("xushun")) {
//                    System.out.println("conn");
//                    mRxBle.connectDevice(bleDevice);
//                }
            }
        });

        search_timer.sendEmptyMessageDelayed(0, 500);


    }
    // 开始扫描
    private int scan_timer_select = 0;
    private boolean scan_flag = true;
    private Handler search_timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            scanTime++;
            if (scanTime >= 40) {
                search_timer.removeMessages(0);
            }else{
                search_timer.sendEmptyMessageDelayed(0, 500);
            }

            if (!scan_flag){
                return;
            }
            // 扫描时间调度
            switch (scan_timer_select) {
                case 1:
//                    mRxBle.scanBleDevices(true);

                case 3: // 停止扫描(结算)
//                    mRxBle.scanBleDevices(false);

                    for (int i = 0; i < scan_devices.size(); ) { // 防抖
                        if (scan_devices.get(i).CheckSearchcount() > 2) {
                            scan_devices.remove(i);
                        } else {
                            i++;
                        }
                    }
                    scan_devices_dis.clear(); // 显示出来
                    for (MTBeacon device : scan_devices) {
                        scan_devices_dis.add(device);
                    }
                    list_adapter.notifyDataSetChanged();

                    break;

                default:
                    break;
            }
            scan_timer_select = (scan_timer_select + 1) % 4;
        }

    };
    @OnClick(R.id.send_btn)
    public void SendMessage(View view){
        responseOk = 0;
        sendHexFlag = 0;
        size = 0;
        address = 0;
        programIndex = 0;
        new uploadthread().start();

    }

    class uploadthread extends Thread {
        @Override
        public synchronized void run() {
            super.run();
            mRxBle.sendData(mRxBle.getMsg("AT+REAST~*&$@!", 0));
            sleepsec(1000);
            mRxBle.sendData(mRxBle.getMsg("30203020302030203020", 1));
            responseOk = 4;
        }

    }
    // 初始化控件
    private LayoutInflater mInflater;
    private ListView ble_listview;
    private List<MTBeacon> scan_devices = new ArrayList<MTBeacon>();
    private List<MTBeacon> scan_devices_dis = new ArrayList<MTBeacon>();
    private BaseAdapter list_adapter = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.item_device, null);
            }

            TextView device_name_txt = (TextView) convertView
                    .findViewById(R.id.device_name_txt);
            TextView device_rssi_txt = (TextView) convertView
                    .findViewById(R.id.device_rssi_txt);
            TextView device_mac_txt = (TextView) convertView
                    .findViewById(R.id.device_mac_txt);

            device_name_txt.setText(getItem(position).GetDevice().getName());
            device_mac_txt.setText("Mac: "
                    + getItem(position).GetDevice().getAddress());
            device_rssi_txt.setText("Rssi: "
                    + getItem(position).GetAveragerssi());

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public MTBeacon getItem(int position) {
            return scan_devices_dis.get(position);
        }

        @Override
        public int getCount() {
            return scan_devices_dis.size();
        }
    };

    private void initView() {
        mInflater = LayoutInflater.from(this);
        ble_listview = (ListView) findViewById(R.id.ble_listview);

        ble_listview.setAdapter(list_adapter);
        ble_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                BluetoothDevice device = scan_devices.get(position).GetDevice();
                mRxBle.connectDevice(device);

//                Intent intent = new Intent(ScanActivity.this, MainActivity.class);
//                startActivity(intent);

//                Intent intent = new Intent(ScanActivity.this,UploadActivity.class);
//                intent.putExtra("device", scan_devices_dis.get(position).GetDevice());
//                myConnection = connection;
//                setResult(RESULT_OK, intent);
//                ScanActivity.this.finish();

//                MAC = scan_devices_dis.get(position).GetDevice().getAddress();
//                System.out.println(scan_devices_dis.get(position).GetDevice().toString());
//                connect(scan_devices_dis.get(position).GetDevice());

            }
        });
    }

    private void sleepsec(int mils) {
        try {
            uploadthread.sleep(mils);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void requestMultiplePermissions() {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            Log.i("TAG", "onRequestPermissionsResult granted=" + granted);
        }
    }

}
