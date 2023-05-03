package com.example.camel.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component(value = "trainRouteGet")
public class TrainRouteGet extends RouteBuilder {

    @Autowired
    private DataSource dataSource;

    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);

        rest().get("/train/{guid}").to("direct:getFromDtoToDB");

        from("direct:getFromDtoToDB")
                .process(exchange -> {
                    String guid = exchange.getIn().getHeader("guid", String.class);
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
