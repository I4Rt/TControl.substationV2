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
        System.out.println(curTemp);
        System.out.println(curWeatherTemp);
        System.out.println(predicted);
        System.out.println(controlObject.getDangerTemp());
        System.out.println(controlObject.getWarningTemp());
        System.out.println(controlObject.getMaxPredictedDifference());

        if(curTemp > controlObject.getDangerTemp()) {
            System.out.println("h1");
            setTemperatureClass("danger");
        }
        else if(curWeatherTemp != null && Math.abs(curTemp - curWeatherTemp) > controlObject.getWarningTemp() ){
            System.out.println("h2");
            setTemperatureClass("dangerDifference");
        }
        else if (predicted != null && Math.abs(curTemp - predicted) > controlObject.getMaxPredictedDifference()){
            System.out.println("h3");
            setTemperatureClass("predicted");
        }
        else{
            System.out.println("h4");
            setTemperatureClass("normal");
        }


        System.out.println(this.temperatureClass);
        //System.out.println("temperature class of " + name + " is " + temperatureClass);
    }

    // may not work
    // public void addMeasurement(Measurement m){
    //     this.measurements.add(m);
    // }
}
