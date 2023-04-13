package com.i4rt.temperaturecontrol.model;

import lombok.*;
import org.hibernate.annotations.Proxy;
import org.json.JSONObject;


import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Setter
@Getter
@AllArgsConstructor
//@NoArgsConstructor
@ToString
@Proxy(lazy = false)
public class ControlObject implements Comparable<ControlObject>{



    @Id
    @Column(name = "object_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    // линия
    @Column
    private String voltageMeasurementLine;
    // фаза
    @Column
    private String voltageMeasurementChannel;

    @OneToMany(mappedBy = "controlObject", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NodeNote> nodeNotes;

    @OneToOne(mappedBy = "controlObject", fetch = FetchType.EAGER, orphanRemoval = true, cascade=CascadeType.ALL)
    private ControlObjectTIChangingPart controlObjectTIChangingPart;


    @Column(name = "thermal_imager_id", nullable=true)
    private Long thermalImager;

    // ImageViewer

    @Column
    private Double horizontal;
    @Column
    private Double vertical;

    @Column
    private Integer x;
    @Column
    private Integer y;
    @Column
    private Integer areaWidth;
    @Column
    private Integer areaHeight;

    @Column
    private Double focusing;

    // Map

    @Column
    private Integer mapX;
    @Column
    private Integer mapY;

    @Column(nullable = false)
    private Double warningTemp; // This is a danger temperature difference
    @Column(nullable = false)
    private Double dangerTemp;

    @Column
    private boolean needMute;

    @Column(nullable = false)
    private Double maxPredictedDifference;

    public ControlObject() {
        this.controlObjectTIChangingPart = new ControlObjectTIChangingPart(); //make the same id
        this.needMute = false;
    }

    public void addNodeNotes(NodeNote record){
        nodeNotes.add(record);
    }

    public Boolean parseJsonStringAreaPageAndUpdateData(String rowJsonData){

        JSONObject data = new org.json.JSONObject(rowJsonData);

        System.out.println(data.getLong("id"));
        System.out.println(data.getString("name"));
        System.out.println(data.getDouble("warningTemp"));
        System.out.println(data.getDouble("dangerTemp"));

        return false;
    }



    public String getJsonStringNoMap(){

        return "{" +
                "\"id\": " + this.id + ", " +
                "\"name\": \"" + this.name + "\", " +
                "\"temperatureClass\": \"" + this.controlObjectTIChangingPart.getTemperatureClass() + "\"" +
                "\"needMute\": " + this.needMute +
                "}";
    }

    public String getJsonStringWithMap(){

        return "{" +
                "\"id\": " + this.id + ", " +
                "\"name\": \"" + this.name + "\", " +
                "\"temperatureClass\": \"" + this.controlObjectTIChangingPart.getTemperatureClass() + "\", " +
                "\"mapX\": " + this.mapX + ", " +
                "\"mapY\": " + this.mapY +
                "\"needMute\": " + this.needMute +
                "}";
    }
    public Map getMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("name", this.name);
        map.put("temperatureClass", this.controlObjectTIChangingPart.getTemperatureClass());
        map.put("mapX", this.mapX);
        map.put("mapY", this.mapY);
        map.put("needMute", this.needMute);
        return map;
    }

    public String getCoordinatesString(){
        if(getHorizontal() != null && getVertical() != null && getX() != null && getY() != null && getFocusing() != null){
            return "ТВ" + thermalImager + ": (("+getHorizontal()+"°, "+getVertical()+"°), ("+getX()+", "+getY()+") - ("+(int)(getX() + getAreaWidth())+", "+(int)(getY() + getAreaHeight())+"), "+getFocusing()+")";
        }
        return "Не заданы";

    }

    @Override
    public int compareTo(ControlObject o) {
        return this.getId().compareTo(o.getId());
    }

    public Boolean getNeedBeep(){
        if (!this.needMute && !(this.controlObjectTIChangingPart.getTemperatureClass().equals("noData") || this.controlObjectTIChangingPart.getTemperatureClass().equals("normal")))
            return true;
        return false;
    }
}
