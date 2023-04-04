package com.i4rt.temperaturecontrol.model;

import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CloseData {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Date datetime;
    @Column
    private String datetimeStr;
    @Column
    private Long controlObjectId;
    @Column
    private Double nodeTemperature;
    @Column
    private Double amperage;
    @Column
    private Double power;
    @Column
    private Double voltage;
    @Column
    private Double atmospherePressure;
    @Column
    private Double humidity;
    @Column
    private Double temperature;
    @Column
    private Double windForce;

    // ?
    @Column
    private Double predictedTemperature;

}
