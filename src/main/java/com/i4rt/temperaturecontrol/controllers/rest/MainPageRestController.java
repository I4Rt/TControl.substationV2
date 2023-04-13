package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.SystemParametersHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.deviceControlThreads.MIPControlThread;
import com.i4rt.temperaturecontrol.deviceControlThreads.MIPSaver;
import com.i4rt.temperaturecontrol.deviceControlThreads.ThermalImagersMainControlThread;
import com.i4rt.temperaturecontrol.deviceControlThreads.WeatherStationControlThread;
import com.i4rt.temperaturecontrol.model.ControlObject;

import com.i4rt.temperaturecontrol.model.LogRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


@RestController
public class MainPageRestController {
    @Autowired
    private final ControlObjectRepo controlObjectRepo;
    @Autowired
    private final ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;

    @Autowired
    private final LogRecordRepo logRecordRepo;

    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    @Autowired
    private final MIPMeasurementRepo mipMeasurementRepo;

    public MainPageRestController(ControlObjectRepo controlObjectRepo, ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo, MeasurementRepo measurementRepo, UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo, MIPMeasurementRepo mipMeasurementRepo, LogRecordRepo logRecordRepo) {
        this.logRecordRepo = logRecordRepo;
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.userRepo = userRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.mipMeasurementRepo = mipMeasurementRepo;
        this.controlObjectTIChangingPartRepo = controlObjectTIChangingPartRepo;

        userRepo.setThermalImagerNotGrabbedForAllUsers();

        ThermalImagersMainControlThread.setInstance(controlObjectRepo,controlObjectTIChangingPartRepo, measurementRepo, userRepo, weatherMeasurementRepo, logRecordRepo);

        ThermalImagersMainControlThread thermalImagersMainControlThread = ThermalImagersMainControlThread.getInstance();

        thermalImagersMainControlThread.start();

        WeatherStationControlThread weatherStationControlThread = new WeatherStationControlThread(weatherMeasurementRepo);

        weatherStationControlThread.start();

        MIPControlThread mipControlThread = new MIPControlThread();
        mipControlThread.start();

        MIPSaver mipSaver = new MIPSaver(this.mipMeasurementRepo);
        mipSaver.start();


        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.add(Calendar.DAY_OF_MONTH, -15);

        System.out.println(prevCalendar);
//        Executor ex = new Executor(0,0,0, new DeleteCreateFolderTask(), "daily");
//        ex.start();

    }

    //{'id': id:Long, 'x': x:Integer, 'y': y:Integer, }
    @RequestMapping(value = "changeCoordinates", method = RequestMethod.POST)
    public void changeCoordinates(@RequestBody String jsonData){
        System.out.println("coordinate change");
        org.json.JSONObject data = new org.json.JSONObject(jsonData);

        ControlObject controlObject = controlObjectRepo.getById(data.getLong("id"));

        controlObject.setMapX(data.getInt("x"));
        controlObject.setMapY(data.getInt("y"));

        controlObjectRepo.save(controlObject);
    }

    @RequestMapping(value = "getMapAndListArraysJSON", method = RequestMethod.POST)
    public String getMapAndListArraysJSON(){

        List<ControlObject> controlObjectsToDisplay = controlObjectRepo.getControlObjectsToDisplay();
        List<ControlObject> controlObjectsList = controlObjectRepo.getOrderedByName();
        List<Map> controlObjectDataListToDisplay = new ArrayList<>();
        List<Map> controlObjectDataList = new ArrayList<>();

        for (ControlObject controlObject : controlObjectsToDisplay) {
            controlObjectDataListToDisplay.add(controlObject.getMap());
        }

        for (ControlObject controlObject : controlObjectsList) {
            controlObjectDataList.add(controlObject.getMap());
        }


        ArrayList<List> result = new ArrayList<>();
        result.add(controlObjectDataList);
        result.add(controlObjectDataListToDisplay);


        return JSONObject.valueToString(result);
    }


    @RequestMapping(value = "/dropMapCoordinates", method = RequestMethod.PUT)
    public void dropCoordinates(@RequestParam Long id){
        ControlObject co = controlObjectRepo.getById(id);

        co.setMapX(null);
        co.setMapY(null);

        controlObjectRepo.save(co);
    }

    @RequestMapping(value = "getLog", method = RequestMethod.POST)
    public String getLog(){
        List<LogRecord> logRecords =  logRecordRepo.getLast(SystemParametersHolder.getInstance().getLogLimit());
        List<Map> result = new ArrayList<>();
        for(LogRecord r: logRecords){
            result.add(r.getMapped());
        }
        return JSONObject.valueToString(result);
    }

    @RequestMapping(value = "setLogLimit", method = RequestMethod.POST)
    public String updateLogLimit(@RequestParam Integer limit){
        SystemParametersHolder.getInstance().setLogLimit(limit);
        return "ok";
    }
}
