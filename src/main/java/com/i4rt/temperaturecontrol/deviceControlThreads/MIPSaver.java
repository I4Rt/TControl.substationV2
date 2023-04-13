package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.SystemParametersHolder;
import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;

@Setter
@NoArgsConstructor
public class MIPSaver extends Thread{


    private MIPMeasurementRepo MIPMeasurementRepo;



    public MIPSaver(MIPMeasurementRepo voltageMeasurementRepo){
        this.MIPMeasurementRepo = voltageMeasurementRepo;
    }



    @SneakyThrows
    public void run(){
        while(true){
            try {
                while (true){
                    Thread.sleep(20000);
                    MIPMeasurement mipMeasurement = new MIPMeasurement();
                    mipMeasurement.setPowerA1(MIPControlThread.lastMeasurement.getPowerA1());
                    mipMeasurement.setPowerB1(MIPControlThread.lastMeasurement.getPowerB1());
                    mipMeasurement.setPowerC1(MIPControlThread.lastMeasurement.getPowerC1());
                    mipMeasurement.setAmperageA1(MIPControlThread.lastMeasurement.getAmperageA1());
                    mipMeasurement.setAmperageB1(MIPControlThread.lastMeasurement.getAmperageB1());
                    mipMeasurement.setAmperageC1(MIPControlThread.lastMeasurement.getAmperageC1());
                    mipMeasurement.setVoltageA1(MIPControlThread.lastMeasurement.getVoltageA1());
                    mipMeasurement.setVoltageB1(MIPControlThread.lastMeasurement.getVoltageB1());
                    mipMeasurement.setVoltageC1(MIPControlThread.lastMeasurement.getVoltageC1());

                    mipMeasurement.setPowerA2(MIPControlThread.lastMeasurement.getPowerA2());
                    mipMeasurement.setPowerB2(MIPControlThread.lastMeasurement.getPowerB2());
                    mipMeasurement.setPowerC2(MIPControlThread.lastMeasurement.getPowerC2());
                    mipMeasurement.setAmperageA2(MIPControlThread.lastMeasurement.getAmperageA2());
                    mipMeasurement.setAmperageB2(MIPControlThread.lastMeasurement.getAmperageB2());
                    mipMeasurement.setAmperageC2(MIPControlThread.lastMeasurement.getAmperageC2());
                    mipMeasurement.setVoltageA2(MIPControlThread.lastMeasurement.getVoltageA2());
                    mipMeasurement.setVoltageB2(MIPControlThread.lastMeasurement.getVoltageB2());
                    mipMeasurement.setVoltageC2(MIPControlThread.lastMeasurement.getVoltageC2());

                    mipMeasurement.setDatetime(MIPControlThread.lastMeasurement.getDatetime());

                    if(MIPControlThread.lastMeasurement.getPowerA2() != null)
                        MIPMeasurementRepo.save(mipMeasurement);


                }

            } catch (Exception e) {
                System.out.println("MIP save data error: " + e );

                MIPSaver mipSaver = new MIPSaver(MIPMeasurementRepo);
                mipSaver.start();
                return;

            }

        }

    }
}
