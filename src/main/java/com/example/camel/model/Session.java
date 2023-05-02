package com.example.camel.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @Column(name = "id_session")
    private Long idSession;

    @Column(name = "ip_session")
    private Long ipSession;

    @Column(name = "guid_session")
    private String guidSession;

    @Column(name = "time_session")
    private String timeSession;

    @Column(name = "date_session")
    private String dateSession;
}
