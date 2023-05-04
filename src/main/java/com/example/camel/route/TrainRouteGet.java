package com.example.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component(value = "trainRouteGet")
public class TrainRouteGet extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);

        rest().get("/check-guid/{guid}")
                .consumes("application/json")
              //  .type(String.class)
                .to("direct:checkGuid");

        from("direct:checkGuid")
                .setBody(header("guid"))
                .log("Полученный GUID из второго реста: ${body}")
                .to("sql:SELECT id_train, upper(train_name) AS train_name, id_station_start, dt_start" +
                        " from trains WHERE dt_start > (select date_session FROM sessions where guid_session = :#${body})" +
                        " ORDER BY dt_start")
                .split().body().threads(5)
                .log("Результат: ${body}")
                .to("log:output");

    }
}

