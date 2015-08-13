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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.Source;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.utils.Log;
import com.umeng.community.example.R;

import java.util.Random;

/**
 * 由于登录时会调用login函数,而该函数中有一个LoginListener参数，如果使用Activity做登录界面,
 * 那么LoginListener需要传递到Activity。因此用户可以使用Dialog来自定义登录界面,这样避免静态的LoginListener字段。
 */
public class CustonLoginActivity extends Activity {

    public static LoginListener sLoginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.login_btn).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                mockLoginData();
            }
        });
    }

    /**
     * 模拟用户登录操作,自定义过程中用户可以构造用户名、密码输入界面
     */
    private void mockLoginData() {
        Log.d("", "### 使用自己的账户系统登录,然后将标识用户唯一性的id和source传递给社区SDK ");
        Random random = new Random();
        CommUser loginedUser = new CommUser();
        String userId = "id" + random.nextInt(Integer.MAX_VALUE);
        loginedUser.id = userId; // 用户id
        loginedUser.name = "name" + random.nextInt(Integer.MAX_VALUE); // 用户名
        loginedUser.source = Source.SELF_ACCOUNT;// 登录系统来源
        loginedUser.gender = Gender.FEMALE;// 用户性别
        loginedUser.level = random.nextInt(100); // 用户等级
        loginedUser.score = random.nextInt(100);// 积分
        
        if (sLoginListener != null) {
            // 登录完成回调给社区SDK，200代表登录成功
            sLoginListener.onComplete(200, loginedUser);
        }
    }
}
