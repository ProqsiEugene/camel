package com.example.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class TrainRoutePostToDB extends RouteBuilder {


    @Override
    public void configure() throws Exception {

        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);

        rest().post("/train-to-db")
                .consumes("application/json")
                .to("direct:convertToTrain");

        from("direct:sendDtoToDB")
                .routeId("sendDtoToDB")
                .log("До метода setBody в sendDtoToDB: " + "${body}")
                .setBody(simple("${exchangeProperty.bodyValue}"))
                .log("После метода setBody в sendDtoToDB: " + "${body}")
                .log("${body.getTime}")


                .to("sql:INSERT INTO sessions (time_session, ip_session, date_session, guid_session) " +
                        "values (:#${body.getTime}, :#${body.getIp}, :#${body.getDate}, :#${body.getGuid})")
                .to("log:output")
                .to("direct:fromDtoToDB");

        from("direct:fromDtoToDB")
                .log("До  fromDtoToDB: " + "${body}")
                .setBody(simple("${exchangeProperty.bodyValue.getGuid}")) //достаю тело из переменной bodyValue и сохр в body
                .log("guid: " + "${body}")
                .to("log:output");
    }
}
