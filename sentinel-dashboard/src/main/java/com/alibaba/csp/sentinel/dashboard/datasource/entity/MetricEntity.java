/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import com.alibaba.csp.sentinel.dashboard.influxdb.entity.InfluxDBMetricEntity;
import com.influxdb.query.FluxRecord;

import java.util.Date;

/**
 * @author leyou
 */
public class MetricEntity {
    private Long id;
    private Date gmtCreate;
    private Date gmtModified;
    private String app;
    /**
     * 监控信息的时间戳
     */
    private Date timestamp;
    private String resource;
    private Long passQps;
    private Long successQps;
    private Long blockQps;
    private Long exceptionQps;

    /**
     * summary rt of all success exit qps.
     */
    private double rt;

    /**
     * 本次聚合的总条数
     */
    private int count;

    private int resourceCode;

    /**
     * 对象转换（MetricEntity）
     * @param oldEntity
     * @return
     */
    public static MetricEntity copyOf(MetricEntity oldEntity) {
        MetricEntity entity = new MetricEntity();
        entity.setId(oldEntity.getId());
        entity.setGmtCreate(oldEntity.getGmtCreate());
        entity.setGmtModified(oldEntity.getGmtModified());
        entity.setApp(oldEntity.getApp());
        entity.setTimestamp(oldEntity.getTimestamp());
        entity.setResource(oldEntity.getResource());
        entity.setPassQps(oldEntity.getPassQps());
        entity.setBlockQps(oldEntity.getBlockQps());
        entity.setSuccessQps(oldEntity.getSuccessQps());
        entity.setExceptionQps(oldEntity.getExceptionQps());
        entity.setRt(oldEntity.getRt());
        entity.setCount(oldEntity.getCount());
        return entity;
    }

    /**
     * 对象转换（FluxRecord）
     * @param fluxRecord
     * @return
     */
    public static MetricEntity copyOf(FluxRecord fluxRecord) {
        MetricEntity entity = new MetricEntity();
        entity.setApp(toStr(fluxRecord.getValueByKey("app")));
        entity.setResource(toStr(fluxRecord.getValueByKey("resource")));
        entity.setBlockQps(toLongZero(fluxRecord.getValueByKey("blockQps")));
        entity.setCount(toInt(fluxRecord.getValueByKey("count")));
        entity.setExceptionQps(toLongZero(fluxRecord.getValueByKey("_exceptionQps")));
        entity.setGmtCreate(toDate(toLong(fluxRecord.getValueByKey("gmtCreate"))));
        entity.setGmtModified(toDate(toLong(fluxRecord.getValueByKey("gmtModified"))));
        entity.setPassQps(toLongZero(fluxRecord.getValueByKey("passQps")));
        entity.setSuccessQps(toLongZero(fluxRecord.getValueByKey("successQps")));
        entity.setRt(toDouble(fluxRecord.getValueByKey("rt")));
        entity.setTimestamp(toDate(toLong(fluxRecord.getValueByKey("timestamp"))));
        return entity;
    }

    /**
     * 对象转换（FluxRecord）
     * @param fluxRecord
     * @return
     */
    public static MetricEntity copyOf(InfluxDBMetricEntity fluxRecord) {
        MetricEntity entity = new MetricEntity();
        entity.setGmtCreate(toDate(fluxRecord.getGmtCreate()));
        entity.setGmtModified(toDate(fluxRecord.getGmtModified()));
        entity.setApp(fluxRecord.getApp());
        entity.setTimestamp(toDate(fluxRecord.getTimestamp()));
        entity.setResource(fluxRecord.getResource());
        entity.setPassQps(fluxRecord.getPassQps());
        entity.setBlockQps(fluxRecord.getBlockQps());
        entity.setSuccessQps(fluxRecord.getSuccessQps());
        entity.setExceptionQps(fluxRecord.getExceptionQps());
        entity.setRt(fluxRecord.getRt());
        entity.setCount(fluxRecord.getCount());
        return entity;
    }

    /**
     * 转换为时间
     * @param time
     * @return
     */
    private static Date toDate(Long time) {
        if(null != time) {
            return new Date(time);
        }
        return new Date();
    }


    /**
     * 转换为字符串
     *
     * @param obj
     * @return
     */
    private static String toStr(Object obj) {
        return String.valueOf(obj);
    }

    /**
     * 转为换双精度类型
     *
     * @param obj
     * @return
     */
    private static double toDouble(Object obj) {
        if (null != obj) {
            return Double.valueOf(toStr(obj));
        }
        return 0;
    }


    /**
     * 转换为长整形， 默认为0
     * @param obj
     * @return
     */
    private static Long toLongZero(Object obj) {
        if (null != obj) {
            return Long.valueOf(toStr(obj));
        }
        return 0L;
    }

    /**
     * 转换为长整型
     *
     * @param obj
     * @return
     */
    private static Long toLong(Object obj) {
        if (null != obj) {
            return Long.valueOf(toStr(obj));
        }
        return null;
    }

    /**
     * 转换为整型
     *
     * @param obj
     * @return
     */
    private static int toInt(Object obj) {
        if (null != obj) {
            return Integer.valueOf(toStr(obj));
        }
        return 0;
    }

    public synchronized void addPassQps(Long passQps) {
        this.passQps += passQps;
    }

    public synchronized void addBlockQps(Long blockQps) {
        this.blockQps += blockQps;
    }

    public synchronized void addExceptionQps(Long exceptionQps) {
        this.exceptionQps += exceptionQps;
    }

    public synchronized void addCount(int count) {
        this.count += count;
    }

    public synchronized void addRtAndSuccessQps(double avgRt, Long successQps) {
        this.rt += avgRt * successQps;
        this.successQps += successQps;
    }

    /**
     * {@link #rt} = {@code avgRt * successQps}
     *
     * @param avgRt      average rt of {@code successQps}
     * @param successQps
     */
    public synchronized void setRtAndSuccessQps(double avgRt, Long successQps) {
        this.rt = avgRt * successQps;
        this.successQps = successQps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
        this.resourceCode = resource.hashCode();
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getResourceCode() {
        return resourceCode;
    }

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    @Override
    public String toString() {
        return "MetricEntity{" +
                "id=" + id +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", app='" + app + '\'' +
                ", timestamp=" + timestamp +
                ", resource='" + resource + '\'' +
                ", passQps=" + passQps +
                ", blockQps=" + blockQps +
                ", successQps=" + successQps +
                ", exceptionQps=" + exceptionQps +
                ", rt=" + rt +
                ", count=" + count +
                ", resourceCode=" + resourceCode +
                '}';
    }

}
