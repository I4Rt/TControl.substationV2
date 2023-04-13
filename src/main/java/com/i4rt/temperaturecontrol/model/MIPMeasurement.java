package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MIPMeasurement extends MeasurementData{
    @Id
    @Column(name = "mip_measurement_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column
    private Double voltageA1;
    @Column
    private Double voltageB1;
    @Column
    private Double voltageC1;
    @Column
    private Double amperageA1;
    @Column
    private Double amperageB1;
    @Column
    private Double amperageC1;
    @Column
    private Double powerA1;
    @Column
    private Double powerB1;
    @Column
    private Double powerC1;

    @Column
    private Double voltageA2;
    @Column
    private Double voltageB2;
    @Column
    private Double voltageC2;
    @Column
    private Double amperageA2;
    @Column
    private Double amperageB2;
    @Column
    private Double amperageC2;
    @Column
    private Double powerA2;
    @Column
    private Double powerB2;
    @Column
    private Double powerC2;

    @Column(name = "datetime")
    private Date datetime;


}
