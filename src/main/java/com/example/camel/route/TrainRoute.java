package com.example.camel.route;

import com.example.camel.dto.TrainDTO;
import com.example.camel.service.MyProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(value = "trainRoute")
public class TrainRoute extends RouteBuilder {

    @Autowired
    private MyProcessor myProcessor;

    @Autowired
    private DataSource dataSource;

    List<TrainDTO> trains = new ArrayList<>();

    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);

        rest().post("/train")
                .consumes("application/json")
                .type(TrainDTO.class)
                .to("direct:body");

        from("direct:body")
                .routeId("bodyMessage")
                .process(myProcessor)
                //Логирование
//                .log(LoggingLevel.INFO, "Guid: " + "${body.getGuid}")
//                .log(LoggingLevel.INFO, "IP: " + "${body.getIp}")
//                .log(LoggingLevel.INFO, "Time: " + "${body.getTime}")
//                .log(LoggingLevel.INFO, "Date: " + "${body.getDate}")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        TrainDTO trainDTO = exchange.getIn().getBody(TrainDTO.class);

                        exchange.setProperty("guid", trainDTO.getGuid());
                        exchange.setProperty("ip", trainDTO.getIp());
                        exchange.setProperty("time", trainDTO.getTime());
                        exchange.setProperty("date", trainDTO.getDate());
                    }
                })
                .to("bean:protobufService?method=convertDtoToProtobuf")
                .to("direct:kafka");

        from("direct:kafka")
                .routeId("sendToKafka")
                .to("kafka:{{kafka.topic}}?brokers={{kafka.brokers}}")
                .transform().simple("${body}")
                .to("direct:guid");

        from("direct:guid")
                .routeId("sendGuid")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getMessage().setBody(exchange.getProperty("guid"));
                    }
                })
                .to("direct:sendDtoToDB");

        from("direct:sendDtoToDB")
                .process(exchange -> {
                    //Логирование
//                    log.info("Guid: " + exchange.getProperty("guid"));
//                    log.info("IP: " + exchange.getProperty("ip"));
//                    log.info("Time: " + exchange.getProperty("time"));
//                    log.info("Date: " + exchange.getProperty("date"));

                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                    jdbcTemplate.update("INSERT INTO sessions " +
                                    "(guid_session, ip_session, time_session, date_session) " +
                                    "values (?, ?, ?, ?)",
                            exchange.getProperty("guid"),
                            exchange.getProperty("ip"),
                            exchange.getProperty("time"),
                            exchange.getProperty("date"));

                    exchange.getMessage().setBody(exchange.getProperty("guid"));
                })
                .to("direct:fromDtoToDB");

        from("direct:fromDtoToDB")
                .process(exchange -> {
                    log.info("Guid: " + exchange.getProperty("guid"));
                    String guid = (String) exchange.getProperty("guid");

                    // Выборка из БД по заданным условиям
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                    List<Map<String, Object>> trains = jdbcTemplate.queryForList(
                            "SELECT id_train, upper(train_name) AS train_name, id_station_start, dt_start from trains\n" +
                                    "WHERE dt_start > (select date_session FROM sessions where guid_session = ?)\n" +
                                    "ORDER BY dt_start;\n", guid);
                    if (trains.isEmpty()) {
                        exchange.getMessage().setBody("No data found for guid: " + guid);
                    } else {
                        exchange.getMessage().setBody(trains);
                    }
                })
                .split().body().threads(5);
    }
}