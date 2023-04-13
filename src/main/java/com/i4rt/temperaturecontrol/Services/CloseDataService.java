package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.databaseInterfaces.CloseDataRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.MIPMeasurementRepo;
import com.i4rt.temperaturecontrol.databaseInterfaces.WeatherMeasurementRepo;
import com.i4rt.temperaturecontrol.model.CloseData;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.MIPMeasurement;
import com.i4rt.temperaturecontrol.model.WeatherMeasurement;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Component
public class CloseDataService {
    private static ControlObjectRepo controlObjectRepo;
    private static MIPMeasurementRepo mipMeasurementRepo;
    private static WeatherMeasurementRepo weatherMeasurementRepo;
    private static CloseDataRepo closeDataRepo;

    @Autowired
    public CloseDataService(ControlObjectRepo controlObjectRepo, MIPMeasurementRepo mipMeasurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, CloseDataRepo closeDataRepo){
        CloseDataService.controlObjectRepo = controlObjectRepo;
        CloseDataService.mipMeasurementRepo = mipMeasurementRepo;
        CloseDataService.weatherMeasurementRepo = weatherMeasurementRepo;
        CloseDataService.closeDataRepo = closeDataRepo;
    }


    public static void saveData(Long id, Double temperature, Double predictedTemperature){
        ControlObject controlObject = controlObjectRepo.getById(id);
        CloseData closeData = new CloseData();
        MIPMeasurement mipMeasurement = mipMeasurementRepo.getLastMipMeasurement();
        WeatherMeasurement weatherMeasurement =  weatherMeasurementRepo.getLastWeatherMeasurement();


        String pattern = "dd.MM  HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date curDate = new Date();
        String dateStr = simpleDateFormat.format(curDate);

        closeData.setDatetime(curDate);
        closeData.setDatetimeStr(dateStr);
        closeData.setControlObjectId(id);

        String line = controlObject.getVoltageMeasurementLine();
        String phase = controlObject.getVoltageMeasurementChannel();

        if (line == null | line.equals("1")){
            switch (phase){
                case "A":
                    closeData.setAmperage(mipMeasurement.getAmperageA1());
                    closeData.setVoltage(mipMeasurement.getVoltageA1());
                    closeData.setPower(mipMeasurement.getPowerA1());
                    break;
                case "B":
                    closeData.setAmperage(mipMeasurement.getAmperageB1());
                    closeData.setVoltage(mipMeasurement.getVoltageB1());
                    closeData.setPower(mipMeasurement.getPowerB1());
                    break;
                case "C":
                    closeData.setAmperage(mipMeasurement.getAmperageC1());
                    closeData.setVoltage(mipMeasurement.getVoltageC1());
                    closeData.setPower(mipMeasurement.getPowerC1());
                    break;
            }
        }
        else if (line.equals("2")){
            switch (phase){
                case "A":
                    closeData.setAmperage(mipMeasurement.getAmperageA2());
                    closeData.setVoltage(mipMeasurement.getVoltageA2());
                    closeData.setPower(mipMeasurement.getPowerA2());
                    break;
                case "B":
                    closeData.setAmperage(mipMeasurement.getAmperageB2());
                    closeData.setVoltage(mipMeasurement.getVoltageB2());
                    closeData.setPower(mipMeasurement.getPowerB2());
                    break;
                case "C":
                    closeData.setAmperage(mipMeasurement.getAmperageC2());
                    closeData.setVoltage(mipMeasurement.getVoltageC2());
                    closeData.setPower(mipMeasurement.getPowerC2());
                    break;
            }
        }




        closeData.setHumidity(weatherMeasurement.getHumidity());
        closeData.setAtmospherePressure(weatherMeasurement.getAtmospherePressure());
        closeData.setTemperature(weatherMeasurement.getTemperature());
        closeData.setWindForce(weatherMeasurement.getWindForce());

        closeData.setNodeTemperature(temperature);
        closeData.setPredictedTemperature(predictedTemperature);

        closeDataRepo.save(closeData);
    }

    public static Map<Object, Object> getLastDataJson(Long coId){
        String phase = controlObjectRepo.getCOByID(coId).getVoltageMeasurementChannel();
        String line = controlObjectRepo.getCOByID(coId).getVoltageMeasurementLine();
        MIPMeasurement mipMeasurement = mipMeasurementRepo.getLastMipMeasurement();
        WeatherMeasurement weatherMeasurement =  weatherMeasurementRepo.getLastWeatherMeasurement();

        Map result =  new HashMap<Object, Object>(){{
            put("controlObjectId", coId);
            put("atmospherePressure", weatherMeasurement.getAtmospherePressure());
            put("humidity", weatherMeasurement.getHumidity());
            put("temperature", weatherMeasurement.getTemperature());
            put("windForce", weatherMeasurement.getWindForce());
        }};

        if (line == null | line.equals("1")){
            switch (phase){
                case "A":
                    result.put("amperage", mipMeasurement.getAmperageA1());
                    result.put("power", mipMeasurement.getPowerA1());
                    result.put("voltage", mipMeasurement.getVoltageA1());
                    break;
                case "B":
                    result.put("amperage", mipMeasurement.getAmperageB1());
                    result.put("power", mipMeasurement.getPowerB1());
                    result.put("voltage", mipMeasurement.getVoltageB1());
                    break;
                case "C":
                    result.put("amperage", mipMeasurement.getAmperageC1());
                    result.put("power", mipMeasurement.getPowerC1());
                    result.put("voltage", mipMeasurement.getVoltageC1());
                    break;
            }
        }
        else if (line.equals("2")){
            switch (phase){
                case "A":
                    result.put("amperage", mipMeasurement.getAmperageA2());
                    result.put("power", mipMeasurement.getPowerA2());
                    result.put("voltage", mipMeasurement.getVoltageA2());
                    break;
                case "B":
                    result.put("amperage", mipMeasurement.getAmperageB2());
                    result.put("power", mipMeasurement.getPowerB2());
                    result.put("voltage", mipMeasurement.getVoltageB2());
                    break;
                case "C":
                    result.put("amperage", mipMeasurement.getAmperageC2());
                    result.put("power", mipMeasurement.getPowerC2());
                    result.put("voltage", mipMeasurement.getVoltageC2());
                    break;
            }
        }

        return result;
    }
}
