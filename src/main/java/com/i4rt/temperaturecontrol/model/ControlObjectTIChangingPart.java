package com.i4rt.temperaturecontrol.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.util.ArrayList;
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


    public void updateTemperatureClass(Double curTemp, Double curWeatherTemp, Double predicted, ArrayList<CloseData> lastCloseData){
//        System.out.println(curTemp);
//        System.out.println(curWeatherTemp);
//        System.out.println(predicted);
//        System.out.println(controlObject.getDangerTemp());
//        System.out.println(controlObject.getWarningTemp());
//        System.out.println(controlObject.getMaxPredictedDifference());

        setTemperatureClass("normal");

        if (predicted != null && curTemp - predicted > controlObject.getMaxPredictedDifference()){

            Boolean needSetPredicted = true;
            for(CloseData closeData: lastCloseData){
                if (closeData.getNodeTemperature() - closeData.getPredictedTemperature() < controlObject.getMaxPredictedDifference()){
                    needSetPredicted = false;
                }
            }
            if (needSetPredicted)
                setTemperatureClass("predicted");

        }
        if(curWeatherTemp != null && curTemp - curWeatherTemp > controlObject.getWarningTemp() ){
            Boolean needSetDangerDifference = true;
            for(CloseData closeData: lastCloseData){
                if (closeData.getNodeTemperature() - closeData.getTemperature() < controlObject.getWarningTemp()){
                    needSetDangerDifference = false;
                }
            }
            if (needSetDangerDifference)
                setTemperatureClass("dangerDifference");
        }
        if(curTemp > controlObject.getDangerTemp()) {
            Boolean needSetDanger = true;
            for(CloseData closeData: lastCloseData){
                if (closeData.getNodeTemperature() < controlObject.getDangerTemp()){
                    needSetDanger = false;
                }
            }
            if (needSetDanger)
                setTemperatureClass("danger");
        }


        System.out.println(this.temperatureClass);
        //System.out.println("temperature class of " + name + " is " + temperatureClass);
    }

    // may not work
    // public void addMeasurement(Measurement m){
    //     this.measurements.add(m);
    // }
}
