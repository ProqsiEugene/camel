package com.example.camel.route;

import com.example.camel.dto.DateDTO;
import com.example.camel.dto.TrainDTO;
import com.example.camel.service.MyProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "trainRoute")
public class TrainRoutePost extends RouteBuilder {

    @Autowired
    private MyProcessor myProcessor;

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
    }
}