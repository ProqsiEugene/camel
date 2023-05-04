package com.example.camel.route;

import com.example.camel.dto.DateDTO;
import com.example.camel.dto.TrainDTO;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component(value = "trainRouteGet")
public class TrainRouteGet extends RouteBuilder {

    @Autowired
    private DataSource dataSource;

    List<TrainDTO> trains = new ArrayList<>();

    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.auto);

        rest().get("/check-guid")
                .consumes("application/json")
                .type(String.class)
                .to("direct:checkGuid");

        from("direct:checkGuid")
                .log("Полученный GUID из второго реста: ${body}")
                .to("sql:SELECT id_train, upper(train_name) AS train_name, id_station_start, dt_start" +
                        " from trains WHERE dt_start > (select date_session FROM sessions where guid_session = :#${body})" +
                        " ORDER BY dt_start")
                .split().body().threads(5)



                .log("Результат: ${body}")
                .to("direct:backInFirst");

    }
}

//    from("direct:getFromDtoToDB")
//                .process(exchange->{
//                        String guid=exchange.getIn().getHeader("guid",String.class);
//        JdbcTemplate jdbcTemplate=new JdbcTemplate(dataSource);
//
//        List<Map<String, Object>>trains=jdbcTemplate.queryForList(
//        "SELECT id_train, upper(train_name) AS train_name, id_station_start, dt_start from trains\n" +
//        "WHERE dt_start > (select date_session FROM sessions where guid_session = ?)\n" +
//        "ORDER BY dt_start;\n",guid);
//        if(trains.isEmpty()){
//        exchange.getMessage().setBody("No data found for guid: " +guid);
//        }else{
//        exchange.getMessage().setBody(trains);
//        }
//        })
//        .split().body().threads(5);
//    }
//}
