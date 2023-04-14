package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.*;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.util.*;
import java.util.List;

@Setter
@NoArgsConstructor
public class TITemperatureCheckThread extends Thread{



    private ControlObjectRepo controlObjectRepo;
    private ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo;
    private MeasurementRepo measurementRepo;


    private WeatherMeasurementRepo weatherMeasurementRepo;
    private UserRepo userRepo;
    private LogRecordRepo logRecordRepo;

    private CloseDataRepo closeDataRepo;

    private Long thermalImagerID;

    private final HttpSenderService pythonServer = new HttpSenderService("10.136.168.16", 5000);




    public TITemperatureCheckThread(ControlObjectRepo controlObjectRepo, ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo, MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, UserRepo userRepo, LogRecordRepo logRecordRepo, Long thermalImagerID) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.userRepo = userRepo;
        this.thermalImagerID = thermalImagerID;
        this.controlObjectTIChangingPartRepo = controlObjectTIChangingPartRepo;
        this.logRecordRepo = logRecordRepo;


    }



    



    @SneakyThrows
    public void run(){
        AlertHolder alertHolder = AlertHolder.getInstance();
        ThermalImager thermalImager = ThermalImagersHolder.getTIByID(thermalImagerID);
        System.out.println("cur: " + thermalImagerID);
        try {
            System.out.println("Begin leaf thread");
            User user = null;

            System.out.println("Thermal imager found");

            if (thermalImager != null) {
                if(!thermalImager.status().equals("ok")){
                    Logger.todayLogRecord("TI"+thermalImagerID + " unavailable");
                    AlertSetter.setAlert(thermalImager.getId(), true);
                    throw new Exception("TI"+thermalImagerID + " unavailable");
                }
                else{
                    System.out.println("Setting false");
                    AlertSetter.setAlert(thermalImager.getId(), false);
                }


                List<ControlObject> curControlObjects = this.controlObjectRepo.getControlObjectByTIID(thermalImager.getId());
                System.out.println("Collection size " + curControlObjects.size());
                Collections.sort(curControlObjects);
            /*
            List<ControlObject> curControlObjects = thermalImager.getControlObjectsArray();
            System.out.println("Collection size " + curControlObjects.size());

            Collections.sort(curControlObjects);

            List<Long> existsID = new ArrayList<>();


            List<ControlObject> curControlObjectsExist = new ArrayList<>();



            for(ControlObject co : curControlObjects){

                if(! existsID.contains(co.getId())){
                    existsID.add(co.getId());
                    curControlObjectsExist.add(co);
                }


            }

             */


                for (ControlObject co : curControlObjects) {
                    user = userRepo.getUserThatGrabbedThermalImager();
                    if (user != null) {
                        System.out.println("User is busy breaking leaf thread");
                        System.out.println("\n\n" + user.getThermalImagerGrabbed() + "\n\n");
                        break;
                    }
                    Thread.sleep(1000); // ???
                    System.out.println("Checking " + co.getName());
                    if (co.getHorizontal() != null && co.getVertical() != null && co.getFocusing() != null) {
                        System.out.println("Begin move to coordinates " + co.getName());
                        Boolean result1 = thermalImager.gotoCoordinates(co.getHorizontal(), co.getVertical(), co.getFocusing());
                        if(!result1){
                            Logger.todayLogRecord("TI"+thermalImagerID + " did not came to point");
                            System.out.println("Did not came to point " + co.getName());
                            throw new Exception();
                        }
                        else{
                            Thread.sleep(1500);
                            System.out.println("Going to the point result (" + co.getName() + ") is " + result1);
                            if (co.getX() != null && co.getY() != null && co.getAreaWidth() != null && co.getAreaHeight() != null) {
                                String configureResult = thermalImager.configureArea(co.getX(), co.getY(), co.getX() + co.getAreaWidth(), co.getY() + co.getAreaHeight());
                                System.out.println("Configure result to " + co.getName() + " is " + configureResult);
                                if (configureResult.equals("ok")) {

                                    //focussing

                                    Double curTemperature = (Double) thermalImager.getTemperatureInArea(1);
                                    System.out.println("Temperature of " + co.getName() + " is " + curTemperature);
                                    if (curTemperature != null) {
                                        ControlObject coToSave = controlObjectRepo.getById( co.getId() );
                                        ControlObjectTIChangingPart coTI = coToSave.getControlObjectTIChangingPart();
                                        Measurement m = new Measurement();

                                        m.setControlObjectTIChangingPart(coTI);

                                        m.setTemperature(curTemperature);
                                        if(weatherMeasurementRepo.getLastWeatherMeasurement() != null){
                                            m.setWeatherTemperatureDifference(Math.abs(curTemperature - weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature())); // !
                                        }
                                        else{
                                            m.setWeatherTemperatureDifference(Double.valueOf(0)); // !
                                        }
                                        m.setDatetime(new Date());


                                        String pythonResponse  = pythonServer.sendPostNoAuth("predict", CloseDataService.getLastDataJson(co.getId()));
                                        System.out.println("\n\n python: " + pythonResponse + "\n\n");
                                        if (pythonResponse != null){
                                            JSONObject data = new JSONObject(pythonResponse);
                                            CloseDataService.saveData(co.getId(), curTemperature, data.getDouble("predict"));
                                            coTI.updateTemperatureClass(curTemperature, weatherMeasurementRepo.getLastWeatherMeasurement() != null ? weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature() : null, data.getDouble("predict"));
                                        }
                                        else{
                                            coTI.updateTemperatureClass(curTemperature ,weatherMeasurementRepo.getLastWeatherMeasurement() != null ? weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature() : null, null);
                                            CloseDataService.saveData(co.getId(), curTemperature, null);
                                        }


                                        // coTI.addMeasurement(m);
                                        String resultImage = thermalImager.saveImage(co.getName());
                                        if (!resultImage.equals("ok")){
                                            Logger.todayLogRecord("TI"+thermalImagerID + " did not save image");
                                            System.out.println("Did not save image " + co.getName());
                                            throw new Exception();
                                        }

                                        controlObjectTIChangingPartRepo.save(coTI);
                                        measurementRepo.save(m);

                                        switch (coTI.getTemperatureClass()){
                                            case "danger":
                                                LogRecord logRecord1 = new LogRecord(co.getName() + ": Зафиксировано превышение допустимых границ температур");
                                                logRecordRepo.save(logRecord1);
                                                break;
                                            case "dangerDifference":
                                                LogRecord logRecord2 = new LogRecord(co.getName() + ": Зафиксировано превышение допустимой разницы температуры узла с температурой воздуха");
                                                logRecordRepo.save(logRecord2);
                                                break;
                                            case "predicted":
                                                LogRecord logRecord3 = new LogRecord(co.getName() + ": Зафиксировано отклонение от прогнозируемой температуры");
                                                logRecordRepo.save(logRecord3);
                                                break;
                                        }


                                        AlertSetter.setNeedBeep(controlObjectTIChangingPartRepo);
                                        System.out.println(alertHolder);

                                    } else {

                                        Logger.todayLogRecord("TI"+thermalImagerID + " getting temperature error");
                                    }

                                } else {


                                    Logger.todayLogRecord("TI"+thermalImagerID + " setting params error");
                                    //AlertSetter.setAlert(thermalImager.getId(), true);
                                }
                            } else {
                                System.out.println("passing");
                                continue;
                            }
                        }
                    }
                    //Thread.sleep(5000);
                }
            } else {
                System.out.println("Passing: Thermal imager with current id is not exist");
            }
            thermalImager.setIsBusy(false);


        }
        catch (Exception e){
            thermalImager.setIsBusy(false);
            System.out.println("Leaf thread error: " + e);
            //AlertSetter.setAlert(thermalImager.getId(), true);
            System.out.println(alertHolder);
        }
        Thread.sleep(1000);
    }
}
