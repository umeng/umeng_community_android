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
package com.umeng.comm.ui.emoji;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.ui.emoji.EmojiBorad.OnEmojiItemClickListener;

/**
 * 每页的Emoji现实View，使用GridView来自展示
 * @author BinGoBinBin
 */
public class EmojiView extends GridView {

    private EmojiBean[] mEmojicons;
    private static final int COLUMN_WIDTH = 40; // column width
    private static final int COLUMNS = 7; // columns

    /**
     * @param context
     */
    public EmojiView(Context context, EmojiBean[] emojis) {
        super(context);
        this.mEmojicons = emojis;
        init();
    }

    // init set
    private void init() {
        setColumnWidth(DeviceUtils.dp2px(getContext(), COLUMN_WIDTH));
        int color = Color.parseColor("#f4f4f6");
        setCacheColorHint(color);
        setDrawingCacheBackgroundColor(color);
        setBackgroundColor(color);
        setNumColumns(COLUMNS);
        setSelector(new ColorDrawable(color));
        setVerticalSpacing(DeviceUtils.dp2px(getContext(), 10));
        setFadingEdgeLength(0);

        EmojiAdapter adapter = new EmojiAdapter(getContext(), mEmojicons);
        setAdapter(adapter);
    }

    /**
     * set calback function</br>
     * 
     * @param listener
     */
    public void setOnItemClickListener(final OnEmojiItemClickListener listener) {
        setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemClick(mEmojicons[position]);
            }
        });
    }

}
