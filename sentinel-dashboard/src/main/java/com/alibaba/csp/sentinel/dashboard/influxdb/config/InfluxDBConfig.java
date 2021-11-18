package com.alibaba.csp.sentinel.dashboard.influxdb.config;


import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class InfluxDBConfig {

    /**
     * influxdb连接URL
     */
    @Value("${spring.influx.url:''}")
    private String influxUrl;

    /**
     * influxdb的访问token权限
     */
    @Value("${spring.influx.token:''}")
    private String influxToken;

    /**
     * org组织名称
     */
    @Value("${spring.influx.org:''}")
    private String influxOrg;

    /**
     * bucket名称
     */
    @Value("${spring.influx.bucket:''}")
    private String influxBucket;



    /**
     * 初始化influx client
     * @return
     */
    @Bean
    public InfluxDBClient influxDBClient() {
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
        return influxDBClient;
    }

    /**
     * 创建Write Api （负责写入）
     * @param influxDBClient
     * @return
     */
    @Bean
    public WriteApiBlocking writeApiBlocking (InfluxDBClient influxDBClient) {
        WriteApiBlocking writeApiBlocking = influxDBClient.getWriteApiBlocking();
        return writeApiBlocking;
    }

    /**
     * 创建Query Api （负责查询）
     * @param influxDBClient
     * @return
     */
    @Bean
    public QueryApi queryApi(InfluxDBClient influxDBClient) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        return queryApi;
    }

}
