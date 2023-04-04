package com.i4rt.temperaturecontrol.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date datetime;

    @Column(nullable = false)
    private String text;

    public LogRecord( String text) {
        this.datetime = new Date();
        this.text = text;
    }

    public Map getMapped(){
        Map<String, Object> data = new HashMap<>();
        data.put("datetime", new SimpleDateFormat("MM-dd-yyyy hh:mm:ss").format(datetime));
        data.put("text", text);
        return data;
    }
}
