package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.Services.AlertSetter;

import com.i4rt.temperaturecontrol.basic.FolderManager;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class AreaPageRestController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    private NodeNoteRepo nodeNoteRepo;
    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final WeatherMeasurementRepo weatherMeasurementRepo;
    @Autowired
    private ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo;
    @Autowired
    private LogRecordRepo logRecordRepo;

    @Autowired
    private CloseDataRepo closeDataRepo;

    public AreaPageRestController(UserRepo userRepo, NodeNoteRepo nodeNoteRepo, ControlObjectRepo controlObjectRepo,ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo , MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, LogRecordRepo logRecordRepo, CloseDataRepo closeDataRepo) {
        this.userRepo = userRepo;
        this.nodeNoteRepo = nodeNoteRepo;
        this.controlObjectRepo = controlObjectRepo;
        this.controlObjectTIChangingPartRepo = controlObjectTIChangingPartRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.logRecordRepo = logRecordRepo;
        this.closeDataRepo = closeDataRepo;
    }

    //Может быть баг с добавлением новой области
    //{"id": 1, "name": "Зона 1", "warningTemp": 60, "dangerTemp": 120}
    @RequestMapping(value = "saveArea", method = RequestMethod.POST)
    public String addingAreaPage(@RequestBody String dataJson){
        FolderManager folderManager = new FolderManager(this.controlObjectRepo);
        System.out.println("saving");
        System.out.println(dataJson);
        Map<String, String> result = new HashMap<>();

        try{
            JSONObject data = new org.json.JSONObject(dataJson);
            ControlObject curControlObject = new ControlObject();
            curControlObject.getControlObjectTIChangingPart().setTemperatureClass("noData");
            if(! data.get("id").equals(null)){
                if(!(controlObjectRepo.findById(data.getLong("id")).isEmpty())){
                    curControlObject = controlObjectRepo.getById(data.getLong("id"));
                    if(!curControlObject.getName().equals(data.getString("name"))) folderManager.renameFolders(curControlObject, data.getString("name"));
                    curControlObject.setName(data.getString("name"));
                    curControlObject.setWarningTemp(data.getDouble("warningTemp"));
                    curControlObject.setDangerTemp(data.getDouble("dangerTemp"));
                    curControlObject.setMaxPredictedDifference(data.getDouble("maxPredictedDifference"));
                    curControlObject.setVoltageMeasurementChannel(data.getString("MIPChanel"));
                    curControlObject.setVoltageMeasurementLine(String.valueOf(data.getInt("MIPLine")));

                    CloseData lastCloseData = closeDataRepo.getLastById(curControlObject.getId());
                    System.out.println("last: " + lastCloseData);

                    if (lastCloseData != null){
                        curControlObject.getControlObjectTIChangingPart().updateTemperatureClass(lastCloseData.getNodeTemperature(), lastCloseData.getTemperature(), lastCloseData.getPredictedTemperature());

                        System.out.println("new temp class: " + curControlObject.getControlObjectTIChangingPart().getTemperatureClass());
                    }
                }
            }
            else{
                curControlObject.setName(data.getString("name"));
                curControlObject.setWarningTemp(data.getDouble("warningTemp"));
                curControlObject.setDangerTemp(data.getDouble("dangerTemp"));
                curControlObject.setMaxPredictedDifference(data.getDouble("maxPredictedDifference"));
                curControlObject.setVoltageMeasurementChannel(data.getString("MIPChanel"));
                curControlObject.setVoltageMeasurementLine(String.valueOf((data.getInt("MIPLine"))));
                curControlObject.setNeedMute(false);
            }


            result.put("temperatureClass", curControlObject.getControlObjectTIChangingPart().getTemperatureClass());

            controlObjectRepo.save(curControlObject);

            curControlObject.getControlObjectTIChangingPart().setControlObject(curControlObject);
            controlObjectTIChangingPartRepo.save(curControlObject.getControlObjectTIChangingPart());
            AlertSetter.setNeedBeep(controlObjectTIChangingPartRepo);
            result.put("message", "Данные сохранены");
        }
        catch (Exception e){
            e.printStackTrace();
            result.put("message", "Данные не сохранены");
        }
        finally {
            String jsonStringToSend = JSONObject.valueToString(result);
            return jsonStringToSend;
        }
    }

    @RequestMapping(value="getTemperature",method = RequestMethod.POST)
    public String getTemperatureMeasurementJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        Integer limit = data.getInt("limit");


        ArrayList<CloseData> closeDataList = closeDataRepo.getCloseDataLimited(searchControlObjectId, limit);

        HashMap<String, ArrayList> results2 = new HashMap<>();
        results2.put("temperatureClass", new ArrayList<>(){{
            add(controlObjectRepo.getById(searchControlObjectId).getControlObjectTIChangingPart().getTemperatureClass());
        }} );
        results2.put("temperature", new ArrayList<Double>());
        results2.put("weather", new ArrayList<Double>());
        results2.put("power", new ArrayList<Double>());
        results2.put("predicted", new ArrayList<Double>());
        results2.put("time", new ArrayList<Date>());
        for(CloseData closeData : closeDataList){
            results2.get("temperature").add(closeData.getNodeTemperature());
            results2.get("weather").add(closeData.getTemperature());
            results2.get("power").add(closeData.getPower());
            results2.get("predicted").add(closeData.getPredictedTemperature());
            results2.get("time").add(closeData.getDatetimeStr());

        }
        Collections.reverse(results2.get("temperature"));
        Collections.reverse(results2.get("weather"));
        Collections.reverse(results2.get("power"));
        Collections.reverse(results2.get("predicted"));
        Collections.reverse(results2.get("time"));



//        List<Measurement> curMeasurements = measurementRepo.getMeasurementByDatetime(controlObjectRepo.getById(searchControlObjectId).getControlObjectTIChangingPart().getId(), limit);
//
//        Map<String, Object> preparedToSendData = new HashMap<>();
//
//        preparedToSendData.put("time", new ArrayList<String>());
//        preparedToSendData.put("temperature", new ArrayList<Double>());
//        System.out.println(controlObjectRepo.getById(searchControlObjectId).getControlObjectTIChangingPart().getTemperatureClass() + "\n\n\n\n\n");
//        preparedToSendData.put("temperatureClass", controlObjectRepo.getById(searchControlObjectId).getControlObjectTIChangingPart().getTemperatureClass());
//
//        for(Measurement measurement: curMeasurements){
//            ((ArrayList<String>)preparedToSendData.get("time")).add(measurement.getDatetime().toString());
//            ((ArrayList<Double>)preparedToSendData.get("temperature")).add(measurement.getTemperature());
//        }




        
        String jsonStringToSend = JSONObject.valueToString(results2);

        //System.out.println("temp_data: " + jsonStringToSend);

        return jsonStringToSend;
    }
    @RequestMapping(value = "getDataToUpdate", method = RequestMethod.POST)
    public String getDataToUpdate(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        Integer limit = data.getInt("limit");

        ArrayList<CloseData> closeDataList = closeDataRepo.getCloseDataLimited(searchControlObjectId, limit);

        HashMap<String, ArrayList> results2 = new HashMap<>();
        results2.put("temperature", new ArrayList<Double>());
        results2.put("weather", new ArrayList<Double>());
        results2.put("power", new ArrayList<Double>());
        results2.put("predicted", new ArrayList<Double>());
        results2.put("time", new ArrayList<String>());
        results2.put("timeReal", new ArrayList<Date>());
        for(CloseData closeData : closeDataList){
            results2.get("temperature").add(closeData.getNodeTemperature());
            results2.get("weather").add(closeData.getTemperature());
            results2.get("power").add(closeData.getPower());
            results2.get("predicted").add(closeData.getPredictedTemperature());
            results2.get("time").add(closeData.getDatetimeStr());
            results2.get("timeReal").add(closeData.getDatetime());
        }
        Collections.reverse(results2.get("temperature"));
        Collections.reverse(results2.get("weather"));
        Collections.reverse(results2.get("power"));
        Collections.reverse(results2.get("predicted"));
        Collections.reverse(results2.get("time"));
        Collections.reverse(results2.get("timeReal"));
        // getting images names:
        results2.put("imagesNames", new ArrayList<String>());

        if(((ArrayList<Date>) results2.get("timeReal")).size() > 1){
            System.out.println("dates found");
            Date beginningDate =((ArrayList<Date>) results2.get("timeReal")).get(0);
            Date endingDate =  ((ArrayList<Date>) results2.get("timeReal")).get(((ArrayList<Date>) results2.get("timeReal")).size() - 1);

            System.out.println("date " + beginningDate + " --> " + endingDate);

            File folder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                System.out.println("folder");
                //System.out.println("folder: " + listOfFiles[i].getName());
                File insideFolder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName());
                File[] listOfInsideFolders = insideFolder.listFiles();

                for(int j = 0; j < listOfInsideFolders.length; j++){
                    //System.out.println("insideFolder");
                    //System.out.println("area folder: " + listOfInsideFolders[j].getName());
                    //System.out.println("equals: " + listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName()));
                    if(listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName())){
                        System.out.println("CorrectInsideFolder");
                        File insideFiles = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName() + "\\" + listOfInsideFolders[j].getName());
                        File[] listOfInsideFiles = insideFiles.listFiles();
                        Arrays.sort(listOfInsideFiles, Comparator.comparingLong(File::lastModified));
                        System.out.println(listOfInsideFiles.length + "images found");
                        for(int n = 0; n < listOfInsideFiles.length; n++){
                            //System.out.println("image: " + listOfInsideFiles[n].getName());
                            Date tempDate = null; // select year!
                            try {
                                tempDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").parse(listOfFiles[i].getName() + "_" + Calendar.getInstance().get(Calendar.YEAR) + "_" + listOfInsideFiles[n].getName());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            //System.out.println("image dates: beginning: " + beginningDate + " ending: " + endingDate + " cur: " + tempDate + " result: " + (endingDate.compareTo(tempDate) >= 0 && beginningDate.compareTo(tempDate) <= 0));
                            if(endingDate.compareTo(tempDate) >= 0 && beginningDate.compareTo(tempDate) <= 0){
                                //System.out.println("append image");
                                ((ArrayList<String>)results2.get("imagesNames")).add("imgData/" + listOfFiles[i].getName() + "/" + listOfInsideFolders[j].getName() + "/" + listOfInsideFiles[n].getName());
                            }
                        }


                    }
                }
            }
        }
        results2.remove("timeReal");
        System.out.println("images " + JSONObject.valueToString(results2));

        return JSONObject.valueToString(results2);
    }


    @RequestMapping(value="getWeatherTemperature",method = RequestMethod.POST)
    public String getTemperatureWeatherMeasurementJsonString(@RequestBody String dataJson){



        JSONObject data = new JSONObject(dataJson);



        String begin = data.getString("begin");
        String end = data.getString("end");

        try {
            Date beginningDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(begin);
            Date endingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(end);
            System.out.println("dates " + beginningDate + " ----> " + endingDate);

            List<WeatherMeasurement> results = weatherMeasurementRepo.getWeatherMeasurementByDatetimeInRange(beginningDate, endingDate);

            System.out.println("Weather array size_" + results.size());

            Map<String, Object> preparedToSendData = new HashMap<>();

            preparedToSendData.put("time", new ArrayList<String>());
            preparedToSendData.put("weatherTemperature", new ArrayList<Double>());


            for(WeatherMeasurement weatherMeasurement: results){
                //System.out.println(weatherMeasurement.getId());
                ((ArrayList<String>)preparedToSendData.get("time")).add(weatherMeasurement.getDatetime().toString());

                ((ArrayList<Double>)preparedToSendData.get("weatherTemperature")).add(weatherMeasurement.getTemperature());
            }

            String jsonStringToSend = JSONObject.valueToString(preparedToSendData);

            return jsonStringToSend;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "error: getting temperature";

    }

    @RequestMapping(value="getTemperatureInRange",method = RequestMethod.POST)
    public String getTemperatureMeasurementInRangeJsonString(@RequestBody String dataJson){
        JSONObject data = new JSONObject(dataJson);
        Long searchControlObjectId = data.getLong("id");
        String begin = data.getString("begin");
        String end = data.getString("end");
        System.out.println("In Range begin: " + begin);
        System.out.println("In Range end: " + end);


        try {
            Date beginningDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(begin);
            Date endingDate = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss").parse(end);

            ArrayList<CloseData> closeDataList = closeDataRepo.getCloseDataByDatetimeInRange(searchControlObjectId, beginningDate, endingDate);

            HashMap<String, ArrayList> results2 = new HashMap<>();
            results2.put("temperature", new ArrayList<Double>());
            results2.put("weather", new ArrayList<Double>());
            results2.put("power", new ArrayList<Double>());
            results2.put("predicted", new ArrayList<Double>());
            results2.put("time", new ArrayList<String>());
            results2.put("timeReal", new ArrayList<Date>());
            for(CloseData closeData : closeDataList){
                results2.get("temperature").add(closeData.getNodeTemperature());
                results2.get("weather").add(closeData.getTemperature());
                results2.get("power").add(closeData.getPower());
                results2.get("predicted").add(closeData.getPredictedTemperature());
                results2.get("time").add(closeData.getDatetimeStr());
                results2.get("timeReal").add(closeData.getDatetimeStr());
            }

            Collections.reverse(results2.get("temperature"));
            Collections.reverse(results2.get("weather"));
            Collections.reverse(results2.get("power"));
            Collections.reverse(results2.get("predicted"));
            Collections.reverse(results2.get("time"));
            Collections.reverse(results2.get("timeReal"));

//            HashMap<String, Object> results = MeasurementsDisplayPrepareService.getPreparedMeasurementsArrays(searchControlObjectId, beginningDate, endingDate);
//
//            System.out.println("temperature: " + (ArrayList<Double>) results.get("temperature"));
//            System.out.println("weather: " + (ArrayList<Double>) results.get("weather"));
//            System.out.println("power: " + (ArrayList<Double>) results.get("power"));
//            System.out.println("time: " + (ArrayList<Date>) results.get("time"));


            // getting images names:
            results2.put("imagesNames", new ArrayList<String>());

            if(((ArrayList<Date>) results2.get("timeReal")).size() > 1){


                System.out.println("date " + beginningDate + " --> " + endingDate);

                File folder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData");
                File[] listOfFiles = folder.listFiles();

                for (int i = 0; i < listOfFiles.length; i++) {
                    System.out.println("folder");
                    //System.out.println("folder: " + listOfFiles[i].getName());
                    File insideFolder = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName());
                    File[] listOfInsideFolders = insideFolder.listFiles();

                    for(int j = 0; j < listOfInsideFolders.length; j++){
                        System.out.println("insideFolder");
                        //System.out.println("area folder: " + listOfInsideFolders[j].getName());
                        //System.out.println("equals: " + listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName()));
                        if(listOfInsideFolders[j].getName().equals(controlObjectRepo.getById(searchControlObjectId).getName())){
                            System.out.println("CorrectInsideFolder");
                            File insideFiles = new File(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + listOfFiles[i].getName() + "\\" + listOfInsideFolders[j].getName());
                            File[] listOfInsideFiles = insideFiles.listFiles();
                            Arrays.sort(listOfInsideFiles, Comparator.comparingLong(File::lastModified));
                            System.out.println(listOfInsideFiles.length + "images found");
                            for(int n = 0; n < listOfInsideFiles.length; n++){
                                //System.out.println("image: " + listOfInsideFiles[n].getName());
                                Date tempDate = null; // select year!
                                try {
                                    tempDate = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").parse(listOfFiles[i].getName() + "_" + Calendar.getInstance().get(Calendar.YEAR) + "_" + listOfInsideFiles[n].getName());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                if(endingDate.compareTo(tempDate) >= 0 && beginningDate.compareTo(tempDate) <= 0){
                                    //System.out.println("append image");
                                    ((ArrayList<String>)results2.get("imagesNames")).add("imgData/" + listOfFiles[i].getName() + "/" + listOfInsideFolders[j].getName() + "/" + listOfInsideFiles[n].getName());
                                }
                            }


                        }
                    }
                }
            }


            results2.remove("timeReal");
            String jsonStringToSend = JSONObject.valueToString(results2);
            return jsonStringToSend;
        } catch (Exception e) {
            System.out.println("Get measurements in range error: " + e);

        }
        return "error";
    }

    @RequestMapping(value = "deleteArea", method = RequestMethod.POST)
    public String deleteArea(@RequestParam Long id){
        measurementRepo.deleteByCOID(id);
        controlObjectTIChangingPartRepo.deleteByCOId(id);
        nodeNoteRepo.deleteByCOID(id);
        controlObjectRepo.deleteByID(id);
        System.out.println("Deleting area by id " + id);

        return "ok";
    }

    @RequestMapping(value = "getNodeNotes")
    public String getNodeNotes(@RequestBody String dataJson){
        JSONObject data = new org.json.JSONObject(dataJson);

        System.out.println(data.getLong("id"));
        System.out.println(data.getInt("limit"));

        ControlObject controlObject =  controlObjectRepo.getById(data.getLong("id"));
        Integer start = controlObject.getNodeNotes().size() - data.getInt("limit");
        if (start < 0){
            start = 0;
        }

        List<NodeNote> records = controlObject.getNodeNotes().subList(start, controlObject.getNodeNotes().size());
        List<Map> result = new ArrayList<>();
            for(NodeNote r: records){
            result.add(r.getMapped());
        }
        Map<String, Object> dataToSend = new HashMap<>();
        dataToSend.put("status", "ok");
        dataToSend.put("logRecords", result);
        //System.out.println(JSONObject.valueToString(dataToSend));
        return JSONObject.valueToString(dataToSend);
    }

    @Transactional
    @RequestMapping(value = "saveNodeNote", method = RequestMethod.POST)
    public String saveNodeNote(@RequestBody String jsonData){
        JSONObject data = new org.json.JSONObject(jsonData);
        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        //System.out.println(data.getLong("id"));

        NodeNote nodeNote = new NodeNote();
        nodeNote.setUserName(user.getName() + " " + user.getSurname());
        nodeNote.setControlObject(controlObjectRepo.getById(data.getLong("id")));
        nodeNote.setDatetime(new Date());
        nodeNote.setRecord(data.getString("text"));
        nodeNoteRepo.save(nodeNote);
        return "Сохранено";
    }



    @RequestMapping(value = "getMute", method = RequestMethod.POST)
    public String getMute(@RequestParam Long id){
        ControlObject co = controlObjectRepo.getById(id);

        return co.isNeedMute() ? "true" : "false";
    }

    //@PreAuthorize("hasAuthority('ADMIN')") //WORKS!
    //@Secured({"ADMIN"})
    @RequestMapping(value = "setMute", method = RequestMethod.POST)
    public void set(HttpServletResponse response, @RequestBody String jsonData){
        JSONObject data = new org.json.JSONObject(jsonData);

        ControlObject co = controlObjectRepo.getById(data.getLong("id"));
        co.setNeedMute(data.getBoolean("mute"));
        controlObjectRepo.save(co);
        AlertSetter.setNeedBeep(controlObjectTIChangingPartRepo);
        //System.out.println("need mute: " + data.getBoolean("mute"));
    }

}
