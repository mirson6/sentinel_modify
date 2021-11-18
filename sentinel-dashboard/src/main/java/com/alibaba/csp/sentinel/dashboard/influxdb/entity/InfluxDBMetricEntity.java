package com.alibaba.csp.sentinel.dashboard.influxdb.entity;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

/**
 * <p>Description: </p>
 * @author Mirson 
 */
@Data
@Measurement(name = "sentinelInfo")
public class InfluxDBMetricEntity {

    @Column(name = "time")
    private Instant time;
    @Column(name = "gmtCreate", tag = true)
    private long gmtCreate;
    @Column(name = "gmtModified")
    private long gmtModified;

    /**
     * 监控信息的时间戳
     */
    @Column(name = "app", tag = true)
    private String app;
    @Column(name = "resource", tag = true)
    private String resource;
    @Column(name = "timestamp", tag = true)
    private long timestamp;
    @Column(name = "passQps", tag = true)
    private long passQps;//通过qps
    @Column(name = "successQps", tag = true)
    private long successQps;//成功qps
    @Column(name = "blockQps", tag = true)
    private long blockQps;//限流qps
    @Column(name = "_exceptionQps")
    private long exceptionQps;//异常qps

    /**
     * 所有successQps的rt的和
     */
    @Column(name = "rt", tag = true)
    private double rt;

    /**
     * 本次聚合的总条数
     */
    @Column(name = "count", tag = true)
    private int count;
    @Column(name = "resourceCode", tag = true)
    private int resourceCode;

}
