package com.i4rt.temperaturecontrol.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Proxy(lazy = false)
public class ControlObjectTIChangingPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String temperatureClass;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "control_object_object_id")
    private ControlObject controlObject;

    @OneToMany(mappedBy = "controlObjectTIChangingPart", fetch = FetchType.LAZY, cascade=CascadeType.ALL)
    private List<Measurement> measurements;


    public void updateTemperatureClass(Double curTemp, Double curWeatherTemp, Double predicted){
        if(curTemp > controlObject.getDangerTemp()) {
            temperatureClass = "danger";
        }
        else if(curWeatherTemp != null && curTemp - curWeatherTemp > controlObject.getWarningTemp() ){
            temperatureClass = "dangerDifference";
        }
        else if (predicted != null ){
            if (predicted > controlObject.getMaxPredictedDifference()){
                temperatureClass = "predicted";
            }
        }
        else{
            temperatureClass = "normal";
        }

        //System.out.println("temperature class of " + name + " is " + temperatureClass);
    }

    // may not work
    // public void addMeasurement(Measurement m){
    //     this.measurements.add(m);
    // }
}
