package com.alibaba.csp.sentinel.dashboard.influxdb;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.influxdb.config.InfluxDBConfig;
import com.alibaba.csp.sentinel.dashboard.influxdb.entity.InfluxDBMetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("influxDBMetricsRepository")
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

    @Autowired
    private InfluxDBConfig influxDBConfig;

    @Autowired
    public WriteApiBlocking writeApiBlocking;

    @Autowired
    public InfluxDBClient influxDBClient;

    @Autowired
    public QueryApi queryApi;


    /**
     * 保存数据
     * @param metric metric data to save
     */
    @Override
    public synchronized void save(MetricEntity metric) {

        try {
            // 记录数据
            InfluxDBMetricEntity entity = new InfluxDBMetricEntity();
            BeanUtils.copyProperties(metric, entity, new String[]{"gmtCreate", "gmtModified", "timestamp"});
            entity.setResource(metric.getResource());
            entity.setGmtCreate(metric.getGmtCreate().getTime());
            entity.setGmtModified(metric.getGmtModified().getTime());
            entity.setTimestamp(metric.getTimestamp().getTime());
            entity.setPassQps(metric.getPassQps());
            entity.setSuccessQps(metric.getSuccessQps());
            entity.setBlockQps(metric.getBlockQps());
            entity.setExceptionQps(metric.getExceptionQps());
            entity.setRt(metric.getRt());
            entity.setCount(metric.getCount());
            entity.setResourceCode(metric.getResourceCode());
            entity.setTime(Instant.now());
            writeApiBlocking.writeMeasurement(WritePrecision.MS, entity);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 批量保存
     * @param metrics metrics to save
     */
    @Override
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {

        if (metrics == null) {
            return;
        }
        metrics.forEach(metric -> {

            save(metric);

        });
    }

    /**
     * 根据时间范围查询数据
     * @param app       application name for Sentinel
     * @param resource  resource name
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return
     */
    @Override
    public synchronized List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {

        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {

            return results;
        }

        // 根据APP和RESOURCE查询时间范围内的数据
        String flux = String.format("from(bucket:\"%s\") |> range(start: %s, stop: %s)"
                        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"sentinelInfo\" and r[\"app\"] == \"%s\") and r[\"resource\"] == \"%s\")",
                influxDBConfig.getInfluxBucket(), startTime, endTime, app, resource);

        List<FluxTable> tables = queryApi.query(flux);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                MetricEntity metricEntity = MetricEntity.copyOf(fluxRecord);
                results.add(metricEntity);
            }
        }

        return results;
    }


    @Override
    public synchronized List<String> listResourcesOfApp(String app) {

        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {

            return results;
        }
        //查询最近5分钟的指标(实时数据)
        String command = String.format("from(bucket:\"%s\") |> range(start: -5m)"
                        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"sentinelInfo\" and r[\"app\"] == \"%s\") )",
                influxDBConfig.getInfluxBucket(), app);


        List<MetricEntity> influxResults = new ArrayList<>();

        // 查询
        List<FluxTable> tables = queryApi.query(command);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                MetricEntity metricEntity = MetricEntity.copyOf(fluxRecord);
                influxResults.add(metricEntity);
            }
        }

        try {

            if (CollectionUtils.isEmpty(influxResults)) {
                return results;
            }
            Map<String, MetricEntity> resourceCount = new HashMap<>(32);
            for (MetricEntity metricEntity : influxResults) {
                String resource = metricEntity.getResource();
                if (resourceCount.containsKey(resource)) {
                    // 累加统计
                    MetricEntity oldEntity = resourceCount.get(resource);
                    oldEntity.addPassQps(metricEntity.getPassQps());
                    oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                    oldEntity.addBlockQps(metricEntity.getBlockQps());
                    oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                    oldEntity.addCount(1);
                } else {

                    resourceCount.put(resource, metricEntity);
                }
            }
            //排序
            results = resourceCount.entrySet()
                    .stream()
                    .sorted((o1, o2) -> {
                        MetricEntity e1 = o1.getValue();
                        MetricEntity e2 = o2.getValue();
                        int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                        if (t != 0) {

                            return t;
                        }
                        return e2.getPassQps().compareTo(e1.getPassQps());
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {

            e.printStackTrace();
        }
        return results;
    }
}
