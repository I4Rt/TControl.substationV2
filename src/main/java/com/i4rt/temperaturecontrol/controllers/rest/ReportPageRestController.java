package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.tasks.CreateExcelReport;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ReportPageRestController {
    @Autowired
    private final ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    @Autowired
    private final MIPMeasurementRepo mipMeasurementRepo;
    @Autowired
    private final CloseDataRepo closeDataRepo;

    public ReportPageRestController(ControlObjectRepo controlObjectRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo, MIPMeasurementRepo mipMeasurementRepo, CloseDataRepo closeDataRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;
        this.closeDataRepo = closeDataRepo;
    }


    @RequestMapping(value = "/getReport", method = RequestMethod.POST)
    public String getReport(@RequestBody String jsonData) {
        JSONObject data = new JSONObject(jsonData);

        String begin = data.getString("begin");
        String end = data.getString("end");
        Long id = Long.parseLong("26");
        String name = "error";
        Map<String, String> result = new HashMap<>();

        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);
            System.out.println("dates " + beginningDate + " ----> " + endingDate);

//            System.out.println("\n\n\n" + results.get(1) + "\n\n\n");

            CreateExcelReport createExcelReport = new CreateExcelReport(this.controlObjectRepo, this.measurementRepo, this.weatherMeasurementRepo, this.mipMeasurementRepo, this.closeDataRepo);
            name = createExcelReport.createMainSheet(beginningDate, endingDate, new long[]{});

            result.put("reportName", "reports/" + name);

            String jsonStringToSend = JSONObject.valueToString(result);

//            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);
//            System.out.println("Clipped array: " + jsonStringToSend);
            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println("Report generation error: " + e);
        }
        return "error: getting temperature";

    }

    @RequestMapping(value = "/getPointReport", method = RequestMethod.POST)
    public String getPointReport(@RequestBody String jsonData) {
        JSONObject data = new JSONObject(jsonData);
        Long id = data.getLong("id");
        String begin = data.getString("begin");
        String end = data.getString("end");

        String name = "error";
        Map<String, String> result = new HashMap<>();

        try {
            long[] areaIdList = new long[]{id};
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);
            System.out.println("dates " + beginningDate + " ----> " + endingDate);

//            System.out.println("\n\n\n" + results.get(1) + "\n\n\n");

            CreateExcelReport createExcelReport = new CreateExcelReport(this.controlObjectRepo, this.measurementRepo, this.weatherMeasurementRepo, this.mipMeasurementRepo, this.closeDataRepo);
            name = createExcelReport.createMainSheet(beginningDate, endingDate, areaIdList);

            // Вызов нужного метода!!!!
            result.put("reportName", "reports/" + name);

            String jsonStringToSend = JSONObject.valueToString(result);


            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            System.out.println("Report generation error: " + e);
        }
        return "error: getting temperature";

    }
}
