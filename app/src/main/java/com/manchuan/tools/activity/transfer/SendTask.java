package com.manchuan.tools.activity.transfer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.manchuan.tools.bean.FileBean;
import com.manchuan.tools.socket.SendSocket;

import java.io.File;

import timber.log.Timber;

public class SendTask extends AsyncTask<String, Integer, Void> implements SendSocket.ProgressSendListener {

    private static final String TAG = "SendTask";

    private FileBean mFileBean;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private SendSocket mSendSocket;
    private ProgressDialog mProgressDialog;


    public SendTask(Context ctx, FileBean fileBean) {
        mFileBean = fileBean;
        mContext = ctx;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //创建进度条
    }

    @Override
    protected Void doInBackground(String... strings) {
        mSendSocket = new SendSocket(mFileBean, strings[0], this);
        mSendSocket.createSendSocket();
        return null;
    }


    @Override
    public void onProgressChanged(File file, int progress) {
        Timber.tag(TAG).e("发送进度：%s", progress);
    }

    @Override
    public void onFinished(File file) {
        Timber.tag(TAG).e("发送完成");
        assert file != null;
        Toast.makeText(mContext, file.getName() + "发送完毕！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFaliure(File file) {
        Timber.tag(TAG).e("发送失败");
        Toast.makeText(mContext, "发送失败，请重试！", Toast.LENGTH_SHORT).show();
    }
}
