/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.umeng.comm.ui.emoji;

import android.content.Context;
import android.text.Spannable;
import android.util.SparseIntArray;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public final class EmojiHandler {
    private EmojiHandler() {
    }

    private static final SparseIntArray sEmojisMap = new SparseIntArray(200);
    private static final SparseIntArray sSoftbanksMap = new SparseIntArray();
    private static final int DELETE_KEY = -0xffffff;
    static {
        // People
        sEmojisMap.put(0x1f604, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f604"));
        sEmojisMap.put(0x1f603, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f603"));
        sEmojisMap.put(0x1f600, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f600"));
        sEmojisMap.put(0x1f60a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60a"));
        sEmojisMap.put(0x263a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_263a"));
        sEmojisMap.put(0x1f609, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f609"));
        sEmojisMap.put(0x1f60d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60d"));
        sEmojisMap.put(0x1f618, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f618"));
        sEmojisMap.put(0x1f61a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61a"));
        sEmojisMap.put(0x1f617, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f617"));
        sEmojisMap.put(0x1f619, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f619"));
        sEmojisMap.put(0x1f61c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61c"));
        sEmojisMap.put(0x1f61d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61d"));
        sEmojisMap.put(0x1f61b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61b"));
        sEmojisMap.put(0x1f633, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f633"));
        sEmojisMap.put(0x1f601, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f601"));
        sEmojisMap.put(0x1f614, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f614"));
        sEmojisMap.put(0x1f60c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60c"));
        sEmojisMap.put(0x1f612, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f612"));
        sEmojisMap.put(0x1f61e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61e"));
        sEmojisMap.put(0x1f623, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f623"));
        sEmojisMap.put(0x1f622, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f622"));
        sEmojisMap.put(0x1f602, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f602"));
        sEmojisMap.put(0x1f62d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62d"));
        sEmojisMap.put(0x1f62a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62a"));
        sEmojisMap.put(0x1f625, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f625"));
        sEmojisMap.put(0x1f630, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f630"));
        sEmojisMap.put(0x1f605, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f605"));
        sEmojisMap.put(0x1f613, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f613"));
        sEmojisMap.put(0x1f629, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f629"));
        sEmojisMap.put(0x1f62b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62b"));
        sEmojisMap.put(0x1f628, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f628"));
        sEmojisMap.put(0x1f631, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f631"));
        sEmojisMap.put(0x1f620, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f620"));
        sEmojisMap.put(0x1f621, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f621"));
        sEmojisMap.put(0x1f624, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f624"));
        sEmojisMap.put(0x1f616, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f616"));
        sEmojisMap.put(0x1f606, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f606"));
        sEmojisMap.put(0x1f60b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60b"));
        sEmojisMap.put(0x1f637, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f637"));
        sEmojisMap.put(0x1f60e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60e"));
        sEmojisMap.put(0x1f634, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f634"));
        sEmojisMap.put(0x1f635, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f635"));
        sEmojisMap.put(0x1f632, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f632"));
        sEmojisMap.put(0x1f61f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f61f"));
        sEmojisMap.put(0x1f626, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f626"));
        sEmojisMap.put(0x1f627, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f627"));
        sEmojisMap.put(0x1f608, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f608"));
        sEmojisMap.put(0x1f62e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62e"));
        sEmojisMap.put(0x1f62c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62c"));
        sEmojisMap.put(0x1f610, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f610"));
        sEmojisMap.put(0x1f615, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f615"));
        sEmojisMap.put(0x1f62f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f62f"));
        sEmojisMap.put(0x1f636, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f636"));
        sEmojisMap.put(0x1f607, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f607"));
        sEmojisMap.put(0x1f60f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f60f"));
        sEmojisMap.put(0x1f611, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f611"));
        sEmojisMap.put(0x1f466, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f466"));
        sEmojisMap.put(0x1f467, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f467"));
        sEmojisMap.put(0x1f468, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f468"));
        sEmojisMap.put(0x1f63a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63a"));
        sEmojisMap.put(0x1f638, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f638"));
        sEmojisMap.put(0x1f63b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63b"));
        sEmojisMap.put(0x1f63d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63d"));
        sEmojisMap.put(0x1f63c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63c"));
        sEmojisMap.put(0x1f640, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f640"));
        sEmojisMap.put(0x1f63f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63f"));
        sEmojisMap.put(0x1f639, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f639"));
        sEmojisMap.put(0x1f63e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f63e"));
        sEmojisMap.put(0x1f648, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f648"));
        sEmojisMap.put(0x1f649, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f649"));
        sEmojisMap.put(0x1f64a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64a"));
        sEmojisMap.put(0x1f525, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f525"));
        sEmojisMap.put(0x2728, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_2728"));
        sEmojisMap.put(0x1f440, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f440"));
        sEmojisMap.put(0x1f443, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f443"));
        sEmojisMap.put(0x1f444, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f444"));
        sEmojisMap.put(0x1f44d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44d"));
        sEmojisMap.put(0x1f44e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44e"));
        sEmojisMap.put(0x1f44c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44c"));
        sEmojisMap.put(0x1f44a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44a"));
        sEmojisMap.put(0x270a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_270a"));
        sEmojisMap.put(0x270c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_270c"));
        sEmojisMap.put(0x1f44b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44b"));
        sEmojisMap.put(0x270b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_270b"));
        sEmojisMap.put(0x1f446, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f446"));
        sEmojisMap.put(0x1f447, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f447"));
        sEmojisMap.put(0x1f449, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f449"));
        sEmojisMap.put(0x1f448, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f448"));
        sEmojisMap.put(0x1f64c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64c"));
        sEmojisMap.put(0x1f64f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64f"));
        sEmojisMap.put(0x261d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_261d"));
        sEmojisMap.put(0x1f44f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f44f"));
        sEmojisMap.put(0x1f4aa, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f4aa"));
        sEmojisMap.put(0x1f48f, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f48f"));
        sEmojisMap.put(0x1f491, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f491"));
        sEmojisMap.put(0x1f646, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f646"));
        sEmojisMap.put(0x1f645, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f645"));
        sEmojisMap.put(0x1f481, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f481"));
        sEmojisMap.put(0x1f64b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64b"));
        sEmojisMap.put(0x1f486, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f486"));
        sEmojisMap.put(0x1f487, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f487"));
        sEmojisMap.put(0x1f485, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f485"));
        sEmojisMap.put(0x1f64e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64e"));
        sEmojisMap.put(0x1f64d, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f64d"));
        sEmojisMap.put(0x1f647, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f647"));
        sEmojisMap.put(0x1f451, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f451"));
        sEmojisMap.put(0x1f380, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f380"));
        sEmojisMap.put(0x1f302, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f302"));
        sEmojisMap.put(0x1f484, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f484"));
        sEmojisMap.put(0x1f49b, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f49b"));
        sEmojisMap.put(0x1f499, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f499"));
        sEmojisMap.put(0x1f49c, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f49c"));
        sEmojisMap.put(0x1f49a, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f49a"));
        sEmojisMap.put(0x2764, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_2764"));
        sEmojisMap.put(0x1f494, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f494"));
        sEmojisMap.put(0x1f49e, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_1f49e"));
        sEmojisMap.put(0x1f349,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f349"));
        sEmojisMap.put(0x1f350,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f350"));
        sEmojisMap.put(0x1f351,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f351"));
        sEmojisMap.put(0x1f352,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f352"));
        sEmojisMap.put(0x1f354,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f354"));
        sEmojisMap.put(0x1f356,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f356"));
        sEmojisMap.put(0x1f360,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f360"));
        sEmojisMap.put(0x1f366,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f366"));
        sEmojisMap.put(0x1f370,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f370"));
        sEmojisMap.put(0x1f385,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f385"));
        sEmojisMap.put(0x1f3b1,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f3b1"));
        sEmojisMap.put(0x1f3b2,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f3b2"));
        sEmojisMap.put(0x1f40d,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f40d"));
        sEmojisMap.put(0x1f414,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f414"));
        sEmojisMap.put(0x1f417,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f417"));
        sEmojisMap.put(0x1f418,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f418"));
        sEmojisMap.put(0x1f419,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f419"));
        sEmojisMap.put(0x1f420,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f420"));
        sEmojisMap.put(0x1f421,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f421"));
        sEmojisMap.put(0x1f422,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f422"));
        sEmojisMap.put(0x1f424,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f424"));
        sEmojisMap.put(0x1f426,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f426"));
        sEmojisMap.put(0x1f427,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f427"));
        sEmojisMap.put(0x1f428,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f428"));
        sEmojisMap.put(0x1f429,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f429"));
        sEmojisMap.put(0x1f42c,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f42c"));
        sEmojisMap.put(0x1f42d,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f42d"));
        sEmojisMap.put(0x1f42e,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f42e"));
        sEmojisMap.put(0x1f42f,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f42f"));
        sEmojisMap.put(0x1f430,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f430"));
        sEmojisMap.put(0x1f431,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f431"));
        sEmojisMap.put(0x1f432,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f432"));
        sEmojisMap.put(0x1f433,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f433"));
        sEmojisMap.put(0x1f434,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f434"));
        sEmojisMap.put(0x1f435,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f435"));
        sEmojisMap.put(0x1f436,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f436"));
        sEmojisMap.put(0x1f437,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f437"));
        sEmojisMap.put(0x1f438,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f438"));
        sEmojisMap.put(0x1f439,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f439"));
        sEmojisMap.put(0x1f43a,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f43a"));
        sEmojisMap.put(0x1f43b,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f43b"));
        sEmojisMap.put(0x1f43c,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f43c"));
        sEmojisMap.put(0x1f43d,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f43d"));
        sEmojisMap.put(0x1f469,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f469"));
        sEmojisMap.put(0x1f470,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f470"));
        sEmojisMap.put(0x1f471,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f471"));
        sEmojisMap.put(0x1f472,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f472"));
        sEmojisMap.put(0x1f473,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f473"));
        sEmojisMap.put(0x1f474,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f474"));
        sEmojisMap.put(0x1f475,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f475"));
        sEmojisMap.put(0x1f476,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f476"));
        sEmojisMap.put(0x1f477,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f477"));
        sEmojisMap.put(0x1f478,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f478"));
        sEmojisMap.put(0x1f493,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f493"));
        sEmojisMap.put(0x1f4a6,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f4a6"));
        sEmojisMap.put(0x1f4a8,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f4a8"));
        sEmojisMap.put(0x1f4a9,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f4a9"));
        sEmojisMap.put(0x1f680,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f680"));
        sEmojisMap.put(0x1f681,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f681"));
        sEmojisMap.put(0x1f682,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f682"));
        sEmojisMap.put(0x1f683,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f683"));
        sEmojisMap.put(0x1f684,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f684"));
        sEmojisMap.put(0x1f685,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f685"));
        sEmojisMap.put(0x1f688,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f688"));
        sEmojisMap.put(0x1f689,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f689"));
        sEmojisMap.put(0x1f690,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f690"));
        sEmojisMap.put(0x1f691,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f691"));
        sEmojisMap.put(0x1f692,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f692"));
        sEmojisMap.put(0x1f693,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f693"));
        sEmojisMap.put(0x1f696,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f696"));
        sEmojisMap.put(0x1f698,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_1f698"));
        sEmojisMap.put(0x2122,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_2122"));
        sEmojisMap.put(0x2600,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_2600"));
        sEmojisMap.put(0x2601,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_2601"));
        sEmojisMap.put(0x2614,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_2614"));
        sEmojisMap.put(0x26a1,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_26a1"));
        sEmojisMap.put(0x2744,ResFinder.getResourceId(ResType.DRAWABLE,"emoji_2744"));

        // 删除按钮对应的资源文件
        sSoftbanksMap.put(DELETE_KEY, ResFinder.getResourceId(ResType.DRAWABLE, "emoji_delete"));
    }

    private static boolean isSoftBankEmoji(char c) {
        return ((c >> 12) == 0xe);
    }

    private static int getEmojiResource(Context context, int codePoint) {
        return sEmojisMap.get(codePoint);
    }

    private static int getSoftbankEmojiResource(char c) {
        return sSoftbanksMap.get(c);
    }

    /**
     * Convert emoji characters of the given Spannable to the according
     * emojicon.
     * 
     * @param context
     * @param text
     * @param emojiSize
     */
    public static void addEmojis(Context context, Spannable text, int emojiSize, int textSize) {
        addEmojis(context, text, emojiSize, textSize, 0, -1, false);
    }

    /**
     * Convert emoji characters of the given Spannable to the according
     * emojicon.
     * 
     * @param context
     * @param text
     * @param emojiSize
     * @param index
     * @param length
     */
    public static void addEmojis(Context context, Spannable text, int emojiSize, int textSize,
            int index, int length) {
        addEmojis(context, text, emojiSize, textSize, index, length, false);
    }

    /**
     * Convert emoji characters of the given Spannable to the according
     * emojicon.
     * 
     * @param context
     * @param text
     * @param emojiSize
     * @param useSystemDefault
     */
    public static void addEmojis(Context context, Spannable text, int emojiSize, int textSize,
            boolean useSystemDefault) {
        addEmojis(context, text, emojiSize, textSize, 0, -1, useSystemDefault);
    }

    /**
     * Convert emoji characters of the given Spannable to the according
     * emojicon.
     * 
     * @param context
     * @param text
     * @param emojiSize
     * @param index
     * @param length
     * @param useSystemDefault
     */
    public static void addEmojis(Context context, Spannable text, int emojiSize, int textSize,
            int index, int length, boolean useSystemDefault) {
        if (useSystemDefault) {
            return;
        }

        // It's delete icon 
        if (text.toString().equals(EmojiBorad.DELETE_KEY)) {
            text.setSpan(new EmojiSpan(context, sSoftbanksMap.get(DELETE_KEY), emojiSize,
                    textSize), 0, EmojiBorad.DELETE_KEY.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return;
        }

        int textLength = text.length();
        int textLengthToProcessMax = textLength - index;
        int textLengthToProcess = length < 0 || length >= textLengthToProcessMax ? textLength
                : (length + index);

        // remove spans throughout all text
        EmojiSpan[] oldSpans = text.getSpans(0, textLength, EmojiSpan.class);
        for (int i = 0; i < oldSpans.length; i++) {
            text.removeSpan(oldSpans[i]);
        }

        int skip;
        for (int i = index; i < textLengthToProcess; i += skip) {
            skip = 0;
            int icon = 0;
            char c = text.charAt(i);
            if (isSoftBankEmoji(c)) {
                icon = getSoftbankEmojiResource(c);
                skip = icon == 0 ? 0 : 1;
            }

            if (icon == 0) {
                int unicode = Character.codePointAt(text, i);
                skip = Character.charCount(unicode);

                if (unicode > 0xff) {
                    icon = getEmojiResource(context, unicode);
                }
            }

            if (icon > 0) {
                text.setSpan(new EmojiSpan(context, icon, emojiSize, textSize), i, i + skip,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
}
