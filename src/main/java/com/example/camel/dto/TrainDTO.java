package com.example.camel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@AllArgsConstructor
@NoArgsConstructor
@Data
//@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@XmlAccessorType(XmlAccessType.FIELD)
public class TrainDTO {
    @XmlElement
    String time;

    @XmlElement
    String ip;

    @XmlElement
    String date;

    @XmlElement
    String guid;
}
