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

package com.umeng.comm.ui.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.location.Locateable;

/**
 * 默认的地理位置获取实现,使用高德定位SDK(http://lbs.amap.com/api/android-location-sdk/summary/)。
 */
public class DefaultLocationImpl implements AMapLocationListener, Locateable {

    private LocationManagerProxy mLocationManagerProxy;
    private static final int MIN_TIME = -1;// 位置变化的通知时间，单位为毫秒。如果为-1，定位只定位一次。
    private static final int MIN_DISTANCE = 15;// 位置变化通知距离，单位为米
    private SimpleFetchListener<Location> mListener;

    /**
     * 初始化LocationManagerProxy</br>
     * 
     * @param context
     */
    private void initLocation(Context context) {
        if (mLocationManagerProxy == null) {
            mLocationManagerProxy = LocationManagerProxy.getInstance(context);
        }
    }

    /**
     * 获取地理位置信息</br>
     * 
     * @param context
     * @param listener
     */
    public void requestLocation(Context context, SimpleFetchListener<Location> listener) {
        initLocation(context);
        this.mListener = listener;
        mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, MIN_TIME,
                MIN_DISTANCE, this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Location location = null;
        if (amapLocation != null
                && amapLocation.getAMapException().getErrorCode() == 0) {
            location = new Location("");
            location.setLatitude(amapLocation.getLatitude());
            location.setLongitude(amapLocation.getLongitude());
        }
        mListener.onComplete(location);
    }

    /**
     * 移除地理位置回调</br>
     */
    public void unbind() {
        destroy();
    }

    /**
     * 停止定位并销毁定位资源（高德地图建议）</br>
     */
    public void onPause() {
        destroy();
    }

    /**
     * 停止定位并销毁定位资源（高德地图建议）</br>
     */
    @SuppressWarnings("deprecation")
    private void destroy() {
        synchronized (DefaultLocationImpl.class) {
            if (mLocationManagerProxy != null) {
                mLocationManagerProxy.removeUpdates(this);
                mLocationManagerProxy.destory();
            }
            mLocationManagerProxy = null;
            mListener = null;
        }
    }

}
