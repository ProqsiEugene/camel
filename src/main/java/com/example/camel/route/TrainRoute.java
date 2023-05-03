package com.example.camel.route;

import com.example.camel.dto.DateDTO;
import com.example.camel.dto.TrainDTO;
import com.example.camel.service.MyProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

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
                .type(DateDTO.class)
                .to("direct:convertToTrain");

        from("direct:convertToTrain")
                .log("Что попадает в конвектор: " + "${body}")
                .process(exchange -> {
                    TrainDTO trainDTO = new TrainDTO();
                    trainDTO.setDate(exchange.getIn().getBody(DateDTO.class).getDate().toString());
                    exchange.getIn().setBody(trainDTO);
                })
                .log("Что выходит из конвектора: " + "${body}")
                .to("direct:body");

        from("direct:body")
                .routeId("bodyMessage")
                .log("Что попадает в процессор: " + "${body}")
                .process(myProcessor)
                .setProperty("bodyValue", body()) // сохраняю body в переменную bodyValue
                .log("Что выходит из процессора: " + "${body}")
                .to("bean:protobufService?method=convertDtoToProtobuf")
                .to("direct:kafka");

        from("direct:kafka")
                .routeId("sendToKafka")
                .to("kafka:{{kafka.topic}}?brokers={{kafka.brokers}}")
                .transform().simple("${body}")
                .to("direct:guid");

        from("direct:guid")
                .log("До метода setBody в guid: " + "${body}")
                .routeId("sendGuid")
                .setBody(simple("${exchangeProperty.bodyValue.getGuid}")) //достаю тело из переменной bodyValue и сохр в body
                .log("После метода setBody в guid: " + "${body}")
                .to("direct:sendDtoToDB");

        from("direct:sendDtoToDB")
                .routeId("sendDtoToDB")
                .log("До метода setBody в sendDtoToDB: " + "${body}")
                .setBody(simple("${exchangeProperty.bodyValue}"))
                .log("После метода setBody в sendDtoToDB: " + "${body}")

                //.to("INSERT INTO sessions (guid_session, ip_session, time_session, date_session) values (:#guid, :#ip, :#time, :#date)}")

                .to("log:output");
    }
}
//моё можно удалять
//        .unmarshal().protobuf().to("INSERT INTO sessions (guid_session, ip_session, time_session, date_session) values (?, ?, ?, ?)")
//         .to("INSERT INTO sessions (guid_session, ip_session, time_session, date_session) values (?, ?, ?, ?)")
// .to("sqlComponent:{INSERT INTO sessions (guid_session, ip_session, time_session, date_session) values (?, ?, ?, ?)}")







//        from("direct:fromDtoToDB")
//                .process(exchange -> {
//                    log.info("Guid: " + exchange.getProperty("guid"));
//                    String guid = (String) exchange.getProperty("guid");
//
//                    // Выборка из БД по заданным условиям
//                    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
//
//                    List<Map<String, Object>> trains = jdbcTemplate.queryForList(
//                            "SELECT id_train, upper(train_name) AS train_name, id_station_start, dt_start from trains\n" +
//                                    "WHERE dt_start > (select date_session FROM sessions where guid_session = ?)\n" +
//                                    "ORDER BY dt_start;\n", guid);
//                    if (trains.isEmpty()) {
//                        exchange.getMessage().setBody("No data found for guid: " + guid);
//                    } else {
//                        exchange.getMessage().setBody(trains);
//                    }
//                })
//                .split().body().threads(5);
//    }
//}