package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.CloseDataService;
import com.i4rt.temperaturecontrol.Services.Logger;
import com.i4rt.temperaturecontrol.Services.SystemParametersHolder;
import com.i4rt.temperaturecontrol.Services.ThermalImagersHolder;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Setter
@Getter
@NoArgsConstructor
public class ThermalImagersMainControlThread extends Thread {

    private static ThermalImagersMainControlThread instance;


    private ControlObjectRepo controlObjectRepo;

    private MeasurementRepo measurementRepo;
    private WeatherMeasurementRepo weatherMeasurementRepo;
    private ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo;

    private CloseDataRepo closeDataRepo;

    private LogRecordRepo logRecordRepo;

    private UserRepo userRepo;

    private final Integer maxChildThreadsCountBeforeReboot = 500;
    private HashMap<Long, Integer> curChildThreadsCount;



    public ThermalImagersMainControlThread(ControlObjectRepo controlObjectRepo, ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo, MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo,  UserRepo userRepo, LogRecordRepo logRecordRepo) {
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.userRepo = userRepo;
    }

    public static void setInstance(ControlObjectRepo controlObjectRepo,ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo, MeasurementRepo measurementRepo,  UserRepo userRepo, WeatherMeasurementRepo weatherMeasurementRepo, LogRecordRepo logRecordRepo){

        if(instance == null){
            instance = new ThermalImagersMainControlThread();
        }
        instance.setLogRecordRepo(logRecordRepo);
        instance.setControlObjectTIChangingPartRepo(controlObjectTIChangingPartRepo);
        instance.setControlObjectRepo(controlObjectRepo);
        instance.setMeasurementRepo(measurementRepo);
        instance.setWeatherMeasurementRepo(weatherMeasurementRepo);
        instance.setUserRepo(userRepo);
    }
    public static ThermalImagersMainControlThread getInstance(){
        if(instance == null){
            instance = new ThermalImagersMainControlThread();
        }
        return instance;
    }



    @SneakyThrows
    public void run(){
        System.out.println("Тепловизоры инициализированы");
        curChildThreadsCount = new HashMap<>();


        for(ThermalImager ti : ThermalImagersHolder.findAll()){
            ti.setIsBusy(false);
            ti.setNeedReboot(true);

            curChildThreadsCount.put(ti.getId(), 0);
        }



        for(User u : userRepo.findAll()){
            u.setThermalImagerGrabbed(false);
            userRepo.save(u);
        }

        User user = null;
        while(true) {


            System.out.println(ThermalImagersHolder.findAll());
            user = userRepo.getUserThatGrabbedThermalImager();
            //Double lastTemperature = weatherMeasurementRepo.getLastWeatherMeasurement().getTemperature();
            SystemParametersHolder systemParametersHolder = SystemParametersHolder.getInstance();
            if(systemParametersHolder.getTooLowTemperatureStopMark()){
                Logger.todayLogRecord("ВНИМАНИЕ: Поток сканирования приостановлен по достижении критических температур окружающей среды."); // to log
                //System.out.println("ВНИМАНИЕ: Поток сканирования приостановлен по достижении критических температур окружающей среды."); // to log
                Thread.sleep(20000); // 20 seconds
                continue;
            }
            if (user != null) {
                Thread.sleep(500);
                continue;
            } else {
                List<ThermalImager> thermalImagers = ThermalImagersHolder.findAll();

                for(Integer i = 0; i < thermalImagers.size(); i++){
                    //проверка на занятость
                    if( !thermalImagers.get(i).getIsBusy() ){
                        curChildThreadsCount.merge(thermalImagers.get(i).getId(), 1, Integer::sum);
                        if(thermalImagers.get(i).getNeedReboot()){
                            Logger.todayLogRecord("Инициализация перезапуска ТВ" + thermalImagers.get(i).getId());
                            thermalImagers.get(i).reboot();

                        }
                        else{
                            //System.out.println("Starting thermal imager thread");
                            System.out.println("initing leaf thread for ti" + thermalImagers.get(i).getId());
                            TITemperatureCheckThread tiTemperatureCheckThread= new TITemperatureCheckThread(controlObjectRepo, controlObjectTIChangingPartRepo, measurementRepo, weatherMeasurementRepo, userRepo, logRecordRepo, thermalImagers.get(i).getId());

                            tiTemperatureCheckThread.start();

                            thermalImagers.get(i).setIsBusy(true);

                            if(curChildThreadsCount.get(thermalImagers.get(i).getId()) >= maxChildThreadsCountBeforeReboot){
                                //System.out.println("set need reboot ti" + thermalImagers.get(i).getId());
                                thermalImagers.get(i).setNeedReboot(true);
                                curChildThreadsCount.put(thermalImagers.get(i).getId(), 0);
                            }
                        }
                    }
                    else{
                        Thread.sleep(500);
                    }
                }
            }
        }


    }
}
