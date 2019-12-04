package com.yxc.yingyan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.AddEntityRequest;
import com.baidu.trace.api.entity.AddEntityResponse;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.entity.UpdateEntityRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "MainActivity";
    private Button btn_start, btn_stop;
    private Context mContext;

    // 轨迹服务ID
    long serviceId = 0;
    // 设备标识
    String entityName = "张三";
    // 是否需要对象存储服务，默认为：false，关闭对象存储服务。
    // 注：鹰眼 Android SDK v3.0以上版本支持随轨迹上传图像等对象数据，若需使用此功能，该参数需设为 true，且需导入bos-android-sdk-1.0.2.jar。
    boolean isNeedObjectStorage = false;
    // 轨迹服务
    Trace mTrace = null;
    // 轨迹服务客户端
    LBSTraceClient mTraceClient = null;
    // 定位周期(单位:秒)
    int gatherInterval = 20;
    // 打包回传周期(单位:秒)
    int packInterval = 40;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        mContext = MainActivity.this;

        // 初始化轨迹服务
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);
        // 初始化轨迹服务客户端,（Context参数请务必传入getApplicationContext()）
        mTraceClient = new LBSTraceClient(getApplicationContext());
        // 设置定位和打包周期
        mTraceClient.setInterval(gatherInterval, packInterval);
        //上传轨迹点自定义属性
        addTrackAttributeUpload();
        //上传entity自定义属性
        updateEntityAttr();
    }

    private void start(){
        // 开启服务
        mTraceClient.startTrace(mTrace, mTraceListener);
        // 开启采集
        mTraceClient.startGather(mTraceListener);
    }

    private void stop(){
        // 停止服务（此方法将同时停止轨迹服务和轨迹采集，完全结束鹰眼轨迹服务）
        mTraceClient.stopTrace(mTrace, mTraceListener);

        // 停止采集（此方法将停止轨迹采集，但不停止轨迹服务）
//        mTraceClient.stopGather(mTraceListener);
    }

    // 初始化轨迹服务监听器
    OnTraceListener mTraceListener = new OnTraceListener() {
        /**
         * 绑定服务回调接口
         * @param i  状态码
         * @param s 消息 0：成功 1：失败
         */
        @Override
        public void onBindServiceCallback(int i, String s) {
            Log.e(TAG, "绑定服务回调");
            Log.e(TAG, i + "");
            Log.e(TAG, s + "");
            Toast.makeText(mContext, "绑定服务：" + s, Toast.LENGTH_SHORT).show();
        }

        /**
         * 开启服务回调接口
         * @param status 状态码
         * @param message 消息
         *                <p>
         *                <pre>0：成功 </pre>
         *                <pre>10000：请求发送失败</pre>
         *                <pre>10001：服务开启失败</pre>
         *                <pre>10002：参数错误</pre>
         *                <pre>10003：网络连接失败</pre>
         *                <pre>10004：网络未开启</pre>
         *                <pre>10005：服务正在开启</pre>
         *                <pre>10006：服务已开启</pre>
         */
        @Override
        public void onStartTraceCallback(int status, String message) {
            Log.e(TAG, "开启服务回调");
            Log.e(TAG, status + "");
            Log.e(TAG, message + "");
            Toast.makeText(mContext, "开启服务回调：" + message, Toast.LENGTH_SHORT).show();
        }

        /**
         * 停止服务回调接口
         * @param status 状态码
         * @param message 消息
         *                <p>
         *                <pre>0：成功</pre>
         *                <pre>11000：请求发送失败</pre>
         *                <pre>11001：服务停止失败</pre>
         *                <pre>11002：服务未开启</pre>
         *                <pre>11003：服务正在停止</pre>
         */
        @Override
        public void onStopTraceCallback(int status, String message) {
            Log.e(TAG, "停止服务回调");
            Log.e(TAG, status + "");
            Log.e(TAG, message + "");
        }

        /**
         * 开启采集回调接口
         * @param status 状态码
         * @param message 消息
         *                <p>
         *                <pre>0：成功</pre>
         *                <pre>12000：请求发送失败</pre>
         *                <pre>12001：采集开启失败</pre>
         *                <pre>12002：服务未开启</pre>
         */
        @Override
        public void onStartGatherCallback(int status, String message) {
            Log.e(TAG, "开启采集回调");
            Log.e(TAG, status + "");
            Log.e(TAG, message + "");
        }

        /**
         * 停止采集回调接口
         * @param status 状态码
         * @param message 消息
         *                <p>
         *                <pre>0：成功</pre>
         *                <pre>13000：请求发送失败</pre>
         *                <pre>13001：采集停止失败</pre>
         *                <pre>13002：服务未开启</pre>
         */
        @Override
        public void onStopGatherCallback(int status, String message) {
            Log.e(TAG, "停止采集回调");
            Log.e(TAG, status + "");
            Log.e(TAG, message + "");
        }

        /**
         * 推送消息回调接口
         *
         * @param messageNo 状态码
         * @param message 消息
         *                  <p>
         *                  <pre>0x01：配置下发</pre>
         *                  <pre>0x02：语音消息</pre>
         *                  <pre>0x03：服务端围栏报警消息</pre>
         *                  <pre>0x04：本地围栏报警消息</pre>
         *                  <pre>0x05~0x40：系统预留</pre>
         *                  <pre>0x41~0xFF：开发者自定义</pre>
         */
        @Override
        public void onPushCallback(byte messageNo, PushMessage message) {
            Log.e(TAG, "推送消息回调");
            Log.e(TAG, messageNo + "");
            Log.e(TAG, message + "");
        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };

    /**
     * 为轨迹点增加自定义属性数据上传
     */
    private void addTrackAttributeUpload() {
        // 为实现自定义属性数据上传，须重写OnCustomAttributeListener监听器中的onTrackAttributeCallback()接口
        mTraceClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }

            //l - 回调时定位点的时间戳（毫秒）
            @Override
            public Map<String, String> onTrackAttributeCallback(long l) {
                return null;
            }
        });
    }


    /**
     * 自定义entity属性上传（服务端已经自动创建以entityName命名的entity，也就是在初始化轨迹服务传入了entityName）
     */
    private void updateEntityAttr() {
        //自定义entity属性上传
        UpdateEntityRequest updateEntityRequest = new UpdateEntityRequest();
        updateEntityRequest.setServiceId(serviceId);
        updateEntityRequest.setEntityName(entityName);
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("USER_ID", "1001");
        columns.put("USER_SEX", "男");
        columns.put("ACC_NBR", "1234567890");
        updateEntityRequest.setColumns(columns);
        mTraceClient.updateEntity(updateEntityRequest, new OnEntityListener() {
            @Override
            public void onAddEntityCallback(AddEntityResponse addEntityResponse) {
                super.onAddEntityCallback(addEntityResponse);
            }
        });
    }


    /**
     * 主动添加Entity（初始化轨迹服务时不传入entityName）
     */
    private void addEntityAttr() {
        AddEntityRequest request = new AddEntityRequest();
        request.setServiceId(serviceId);
        request.setEntityName(entityName);
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("USER_ID", "1001");
        columns.put("USER_SEX", "男");
        columns.put("ACC_NBR", "1234567890");
        request.setColumns(columns);
        mTraceClient.addEntity(request, new OnEntityListener() {
            @Override
            public void onAddEntityCallback(AddEntityResponse addEntityResponse) {
                super.onAddEntityCallback(addEntityResponse);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                start();
                break;
            case R.id.btn_stop:
                stop();
                break;
            default:
                break;
        }
    }
}
