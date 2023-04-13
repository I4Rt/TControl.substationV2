package com.i4rt.temperaturecontrol.deviceControlThreads;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.CloseDataService;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

@Setter
@NoArgsConstructor
public class MIPControlThread extends Thread{


    public static BufferedReader in1 = null;
    public static BufferedReader in2 = null;

    public static MIPMeasurement lastMeasurement = new MIPMeasurement();



    @SneakyThrows
    public void run(){

        // Hope it helps to handle mistook connections problem
        try{
            in1.close();
        }
        catch (Exception e){
            System.out.println("Drop connection error: " + e);
        }
        finally{
            in1 = null;
        }

        try{
            in2.close();
        }
        catch (Exception e){
            System.out.println("Drop connection error: " + e);
        }
        finally{
            in2 = null;
        }

        while(true){


            try {

                URL oracle1 = new URL("http://192.168.200.31/eventsource/telemech.csv?mode=0");
                in1  = new BufferedReader(new InputStreamReader(oracle1.openStream()));

                URL oracle2 = new URL("http://192.168.200.31/eventsource/telemech.csv?mode=1");
                in2  = new BufferedReader(new InputStreamReader(oracle2.openStream()));

                while (true){
                    AlertHolder alertHolder = AlertHolder.getInstance();
                    alertHolder.setMIPError(false);
                    String resultString1 = in1.readLine();
                    String resultString2 = in2.readLine();
                    if(resultString1 != null && resultString2 != null){
                        if(resultString1.length() > 20 && resultString2.length() > 20){
                            //System.out.println(resultString);
                            String[] dataArray1 = resultString1.split(",");
                            String[] dataArray2 = resultString2.split(",");
                            try{
                                MIPMeasurement mipMeasurement = new MIPMeasurement();


                                mipMeasurement.setAmperageA1(Double.parseDouble(dataArray1[22]));
                                mipMeasurement.setAmperageB1(Double.parseDouble(dataArray1[23]));
                                mipMeasurement.setAmperageC1(Double.parseDouble(dataArray1[24]));
                                mipMeasurement.setVoltageA1(Double.parseDouble(dataArray1[34]));
                                mipMeasurement.setVoltageB1(Double.parseDouble(dataArray1[35]));
                                mipMeasurement.setVoltageC1(Double.parseDouble(dataArray1[36]));
                                mipMeasurement.setPowerA1(Double.parseDouble(dataArray1[45]));
                                mipMeasurement.setPowerB1(Double.parseDouble(dataArray1[46]));
                                mipMeasurement.setPowerC1(Double.parseDouble(dataArray1[47]));

                                mipMeasurement.setAmperageA2(Double.parseDouble(dataArray2[22]));
                                mipMeasurement.setAmperageB2(Double.parseDouble(dataArray2[23]));
                                mipMeasurement.setAmperageC2(Double.parseDouble(dataArray2[24]));
                                mipMeasurement.setVoltageA2(Double.parseDouble(dataArray2[34]));
                                mipMeasurement.setVoltageB2(Double.parseDouble(dataArray2[35]));
                                mipMeasurement.setVoltageC2(Double.parseDouble(dataArray2[36]));
                                mipMeasurement.setPowerA2(Double.parseDouble(dataArray2[45]));
                                mipMeasurement.setPowerB2(Double.parseDouble(dataArray2[46]));
                                mipMeasurement.setPowerC2(Double.parseDouble(dataArray2[47]));

                                mipMeasurement.setDatetime(Calendar.getInstance().getTime());
                                lastMeasurement = mipMeasurement;
                            }catch (Exception e){
                                System.out.println("MIP split error: " + e);
                            }
                        }

                        in1.mark(0);
                        in1.reset();

                        in2.mark(0);
                        in2.reset();
                    }
                }
            } catch (Exception e) {
                AlertHolder alertHolder = AlertHolder.getInstance();
                alertHolder.setMIPError(true);
                if(in1 != null){
                    in1.close();
                }
                if(in2 != null){
                    in2.close();
                }


                System.out.println("MIP error: " );
                System.out.println(e.toString());
                Thread.sleep(5000);

                MIPControlThread mipControlThread = new MIPControlThread();
                mipControlThread.start();
                return;
            }

        }

    }
}
