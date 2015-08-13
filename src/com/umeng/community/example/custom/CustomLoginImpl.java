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

package com.umeng.community.example.custom;

import android.content.Context;
import android.content.Intent;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.login.Loginable;
import com.umeng.comm.core.utils.Log;

/**
 * 自定义实现登录系统，实现Loginable中的三个方法。SDK内部在登录时会调用login函数，如果你的登录界面是Activity，
 * 那么需要自己将LoginListener传递给你的登录Activity并且最后将结果回调给微社区SDK，登录成功那么返回码为200，否则视为登录失败。
 * </p> 另外需要注意的是是否登录的标识应该存储到本地，而不应该存储在内存中。本例中的isLogin变量是就是错误的示例，
 * 你应该就是否登录的标识信息存储到SharedPreferences等地方。如果存储在内存中，那么下次重新进入应用是
 * isLogin被置为false，造成重复登录。
 * 
 */
public class CustomLoginImpl implements Loginable {
    // !不要使用成员变量存储是否登录的状态，应该保存在本地
    private boolean isLogin = false;

    @Override
    public void login(Context context, final LoginListener listener) {
        // 包装一下Listener
        CustonLoginActivity.sLoginListener = new LoginListener() {

            @Override
            public void onStart() {
                listener.onStart();
            }

            @Override
            public void onComplete(int stCode, CommUser userInfo) {
                if (stCode == 200) {
                    isLogin = true;
                }
                listener.onComplete(stCode, userInfo);
            }
        };
        // 跳转到你的Activity
        Intent intent = new Intent(context, CustonLoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void logout(Context context, LoginListener listener) {
        Log.d("", "### 注销登录 ");
        isLogin = false;
        listener.onComplete(200, null);
    }

    @Override
    public boolean isLogined(Context context) {
        Log.d("", "### 这里需要将是否已经登录的状态存到本地,避免下次重新进入应用是 isLogin被置为false");
        // 错误示例,可以将该值存储在SharedPreferences中
        return isLogin;
    }

}
