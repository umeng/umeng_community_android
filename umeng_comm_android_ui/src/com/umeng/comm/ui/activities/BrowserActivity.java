/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.comm.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

public class BrowserActivity extends Activity {
    String mUrl;
    public static final String URL = "url";
    ProgressBar mProgressBar;
    WebView mWebView;
    ImageButton mBackBtn;
    TextView mTitleBtn;
    ImageButton mActionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(ResFinder.getLayout("umeng_comm_browser_url"));

        mUrl = getIntent().getStringExtra(URL);
        checkUrl();

        initWidgets();
    }

    private void checkUrl() {
        if (TextUtils.isEmpty(mUrl)) {
            finish();
        }
        if (mUrl.startsWith("www")) {
            mUrl = "http://" + mUrl;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWidgets() {

        mBackBtn = (ImageButton) findViewById(ResFinder.getId("umeng_comm_title_back_btn"));
        mBackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBtn = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        mTitleBtn.setText(ResFinder.getString("umeng_comm_url_detail"));
        mActionBtn = (ImageButton) findViewById(ResFinder.getId("umeng_comm_title_setting_btn"));
        mActionBtn.setVisibility(View.GONE);

        mProgressBar = (ProgressBar) findViewById(ResFinder.getId("umeng_comm_load_url_bar"));

        // 初始化webview
        mWebView = (WebView) findViewById(ResFinder.getId("umeng_comm_webview"));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);// 设置可以自动加载图片
        mWebView.setHorizontalScrollBarEnabled(false);// 设置水平滚动条
        mWebView.setVerticalScrollBarEnabled(false);// 设置竖直滚动条
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                WebSettings settings = mWebView.getSettings();
                settings.setBuiltInZoomControls(true);
                view.loadUrl(url);
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
        mWebView.loadUrl(mUrl);
    }
}
