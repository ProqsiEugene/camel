package com.example.camel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Entity(name = "trains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Train {

    @Id
    @Column(name = "id_train")
    private Long idTrain;

    @Column(name = "dt_start")
    private LocalDateTime dt_start;

    @Column(name = "id_station_start")
    private Long idStationStart;

    @Column(name = "train_name")
    private String trainName;
}
