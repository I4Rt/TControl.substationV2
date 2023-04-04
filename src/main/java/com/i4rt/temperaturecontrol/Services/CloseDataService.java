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


        String pattern = "MM-dd-yyyy hh:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date curDate = new Date();
        String dateStr = simpleDateFormat.format(curDate);

        closeData.setDatetime(curDate);
        closeData.setDatetimeStr(dateStr);
        closeData.setControlObjectId(id);

        String phase = controlObject.getVoltageMeasurementChannel();
        switch (phase){
            case "A":
                closeData.setAmperage(mipMeasurement.getAmperageA());
                closeData.setVoltage(mipMeasurement.getVoltageA());
                closeData.setPower(mipMeasurement.getPowerA());
                break;
            case "B":
                closeData.setAmperage(mipMeasurement.getAmperageB());
                closeData.setVoltage(mipMeasurement.getVoltageB());
                closeData.setPower(mipMeasurement.getPowerB());
                break;
            case "C":
                closeData.setAmperage(mipMeasurement.getAmperageC());
                closeData.setVoltage(mipMeasurement.getVoltageC());
                closeData.setPower(mipMeasurement.getPowerC());
                break;
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
        MIPMeasurement mipMeasurement = mipMeasurementRepo.getLastMipMeasurement();
        WeatherMeasurement weatherMeasurement =  weatherMeasurementRepo.getLastWeatherMeasurement();

        Map result =  new HashMap<Object, Object>(){{
            put("controlObjectId", coId);
            put("atmospherePressure", weatherMeasurement.getAtmospherePressure());
            put("humidity", weatherMeasurement.getHumidity());
            put("temperature", weatherMeasurement.getTemperature());
            put("windForce", weatherMeasurement.getWindForce());
        }};
        switch (phase){
            case "A":
                result.put("amperage", mipMeasurement.getAmperageA());
                result.put("power", mipMeasurement.getPowerA());
                result.put("voltage", mipMeasurement.getVoltageA());
                break;
            case "B":
                result.put("amperage", mipMeasurement.getAmperageB());
                result.put("power", mipMeasurement.getPowerB());
                result.put("voltage", mipMeasurement.getVoltageB());
                break;
            case "C":
                result.put("amperage", mipMeasurement.getAmperageC());
                result.put("power", mipMeasurement.getPowerC());
                result.put("voltage", mipMeasurement.getVoltageC());
                break;
        }


        return result;
    }
}
