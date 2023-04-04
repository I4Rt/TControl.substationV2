package com.i4rt.temperaturecontrol.model;

import lombok.*;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class NodeNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String userName;

    @Column
    private Date datetime;

    @Column
    private String record;

    @ManyToOne
    @JoinColumn(name="control_object_id")
    private ControlObject controlObject;

    public Map getMapped(){
        Map<String, Object> data = new HashMap<>();
        data.put("datetime", new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(datetime));
        data.put("user", userName);
        data.put("record", record);
        return data;
    }

    @Override
    public String toString(){
        return datetime + " " + record;
    }

}
