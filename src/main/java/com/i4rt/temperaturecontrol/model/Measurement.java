package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Measurement extends MeasurementData{
    @Id
    @Column(name = "measurement_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ti_control_object_id")
    private ControlObjectTIChangingPart controlObjectTIChangingPart;

    @Column(nullable = false)
    private Double temperature;
    @Column(nullable = false)
    private Double weatherTemperatureDifference;


    @Column(nullable = false)
    private Date datetime;
}
