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

import java.io.Serializable;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public class EmojiBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private int icon;
    private char value;
    private String emoji;
    public boolean isDouble = true;

    private EmojiBean() {
    }

    public static EmojiBean fromResource(int icon, int value) {
        EmojiBean emoji = new EmojiBean();
        emoji.icon = icon;
        emoji.value = (char) value;
        return emoji;
    }

    public static EmojiBean fromCodePoint(int codePoint) {
        EmojiBean emoji = new EmojiBean();
        emoji.emoji = newString(codePoint);
        return emoji;
    }

    public static EmojiBean fromChar(char ch) {
        EmojiBean emoji = new EmojiBean();
        emoji.emoji = Character.toString(ch);
        emoji.isDouble = false;
        return emoji;
    }

    public static EmojiBean fromChars(String chars) {
        EmojiBean emoji = new EmojiBean();
        emoji.emoji = chars;
        return emoji;
    }

    public EmojiBean(String emoji) {
        this.emoji = emoji;
    }

    public char getValue() {
        return value;
    }

    public int getIcon() {
        return icon;
    }

    public String getEmoji() {
        return emoji;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EmojiBean && emoji.equals(((EmojiBean) o).emoji);
    }

    @Override
    public int hashCode() {
        return emoji.hashCode();
    }

    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }
}
