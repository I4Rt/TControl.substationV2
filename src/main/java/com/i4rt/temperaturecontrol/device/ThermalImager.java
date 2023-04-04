package com.i4rt.temperaturecontrol.device;

import com.i4rt.temperaturecontrol.Services.AlertHolder;
import com.i4rt.temperaturecontrol.Services.Logger;
import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.additional.UploadedImageCounter;
import com.i4rt.temperaturecontrol.basic.FileUploadUtil;
import com.i4rt.temperaturecontrol.basic.HttpSenderService;
import com.i4rt.temperaturecontrol.model.ControlObject;
import lombok.*;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.hibernate.annotations.Proxy;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Proxy(lazy = false)
public class ThermalImager {

    @Id
    @Column(name = "thermal_imager_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String IP;

    @Column
    private String realm;

    @Column
    private String nonce;

    @Column
    private Integer port;

    @Column
    private Boolean isBusy;

    @Column
    private Boolean needReboot;

    @Column
    private Boolean isRebooting;





    public boolean connection(){
        return true;
    }

    public double getTemperature(Double horizontal, Double vertical, Integer x, Integer y, Double focusing){

        gotoCoordinates(horizontal, vertical, focusing);
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        return random.nextDouble() % 10 * 10 + 100.0;
    }

    public Double getCurHorizontal(){
        return 0.0;
    }
    public Double getCurVertical(){
        return 0.0;
    }
    public Double getCurFocusing(){
        return 200.0;
    }


    public Boolean gotoCoordinatesNoConfig(Double horizontal, Double vertical, Double focusing){
        try {
            horizontal *= 10;
            Integer parsedHorizontal = horizontal.intValue();

            vertical *= 10;
            Integer parsedVertical = vertical.intValue();

            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);


            String bodyMove = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<PTZData version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <AbsoluteHigh>\n" +
                    "        <elevation>"+parsedVertical+"</elevation>\n" +
                    "        <azimuth>"+parsedHorizontal+"</azimuth>\n" +
                    "        <absoluteZoom>50</absoluteZoom>\n" +
                    "    </AbsoluteHigh>\n" +
                    "</PTZData>";


            System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absolute", bodyMove));

            String answer = "";
            Map<String, String> parsedAnswer;

            Integer tryCounter = 0;

            while(true){
                answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");
                System.out.println("counter: " + tryCounter);
                Thread.sleep(400);
                parsedAnswer = HttpSenderService.getMapFromXMLString(answer);


                System.out.println("tryCounter: " + tryCounter);

                if(Double.parseDouble(parsedAnswer.get("elevation")) == vertical / 10 && Double.parseDouble(parsedAnswer.get("azimuth")) == horizontal / 10){
                    Thread.sleep(1500);
                    Integer focusingCounter = 0;
                    while(true){
                        focusingCounter += 1;
                        String focusingBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<PTZAbsoluteEx xmlns=\"http://www.isapi.org/ver20/XMLSchema\" version=\"2.0\">\n" +
                                "    <elevation>" + vertical / 10 + "</elevation>\n" +
                                "    <azimuth>" + horizontal / 10 + "</azimuth>\n" +
                                "    <absoluteZoom>1.00</absoluteZoom>\n" +
                                "    <focus>" + focusing.intValue() + "</focus>\n" +
                                "    <focalLen>10000</focalLen>\n" +
                                "    <horizontalSpeed>10.00</horizontalSpeed>\n" +
                                "    <verticalSpeed>10.00</verticalSpeed>\n" +
                                "    <zoomType>absoluteZoom</zoomType>\n" +
                                "    <objectDistance>1</objectDistance>\n" +
                                "    <isContinuousTrackingEnabled>true</isContinuousTrackingEnabled>\n" +
                                "    <lookDownUpAngle>0.00</lookDownUpAngle>\n" +
                                "</PTZAbsoluteEx>";
                        System.out.println("focusing");
                        System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx", focusingBody));

                        Thread.sleep(400);
                        answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");

                        parsedAnswer = HttpSenderService.getMapFromXMLString(answer);


                        if(!(Integer.parseInt(parsedAnswer.get("focus")) < focusing.intValue() + 100 && Integer.parseInt(parsedAnswer.get("focus")) > focusing.intValue() - 100)){
                            System.out.println("cur_focus " + Integer.parseInt(parsedAnswer.get("focus")));
                            System.out.println("need focus " + focusing.intValue());
                            System.out.println(Integer.parseInt(parsedAnswer.get("focus")) < focusing.intValue() + 100 && Integer.parseInt(parsedAnswer.get("focus")) > focusing.intValue() - 100);
                        }
                        else{
                            System.out.println("focused");
                            break;
                        }

                        if(focusingCounter > 25){
                            System.out.println("Предупреждение: Не установлена нужная фокусировка.");
                            System.out.println("Тестовый режим: Возвращаю картинку.");
                            break;
                            //return false;
                        }
                    }

                    return true;
                }

                if(tryCounter > 25){
                    System.out.println("Too many trys");
                    return false;
                }

                tryCounter += 1;
            }

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }

    }


    public Boolean gotoCoordinates(Double horizontal, Double vertical, Double focusing){
        try {
            horizontal *= 10;
            Integer parsedHorizontal = horizontal.intValue();

            vertical *= 10;
            Integer parsedVertical = vertical.intValue();

            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);
            

            String bodyMove = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<PTZData version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <AbsoluteHigh>\n" +
                    "        <elevation>"+parsedVertical+"</elevation>\n" +
                    "        <azimuth>"+parsedHorizontal+"</azimuth>\n" +
                    "        <absoluteZoom>50</absoluteZoom>\n" +
                    "    </AbsoluteHigh>\n" +
                    "</PTZData>";


            System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absolute", bodyMove));

            String answer = "";
            Map<String, String> parsedAnswer;

            Integer tryCounter = 0;

            while(true){
                answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");
                System.out.println("counter: " + tryCounter);
                Thread.sleep(400);
                System.out.println(answer);
                parsedAnswer = HttpSenderService.getMapFromXMLString(answer);

                System.out.println(Double.parseDouble(parsedAnswer.get("elevation")) + " " + vertical / 10);
                System.out.println(Double.parseDouble(parsedAnswer.get("azimuth")) + " " + horizontal / 10);
                System.out.println("tryCounter: " + tryCounter);

                if(Double.parseDouble(parsedAnswer.get("elevation")) == vertical / 10 && Double.parseDouble(parsedAnswer.get("azimuth")) == horizontal / 10){
                    Thread.sleep(1500);
                    Integer focusingCounter = 0;
                    while(true){
                        focusingCounter += 1;

                        String focusingBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<PTZAbsoluteEx xmlns=\"http://www.isapi.org/ver20/XMLSchema\" version=\"2.0\">\n" +
                                "    <elevation>" + vertical / 10 + "</elevation>\n" +
                                "    <azimuth>" + horizontal / 10 + "</azimuth>\n" +
                                "    <absoluteZoom>1.00</absoluteZoom>\n" +
                                "    <focus>" + focusing.intValue() + "</focus>\n" +
                                "    <focalLen>10000</focalLen>\n" +
                                "    <horizontalSpeed>10.00</horizontalSpeed>\n" +
                                "    <verticalSpeed>10.00</verticalSpeed>\n" +
                                "    <zoomType>absoluteZoom</zoomType>\n" +
                                "    <objectDistance>1</objectDistance>\n" +
                                "    <isContinuousTrackingEnabled>true</isContinuousTrackingEnabled>\n" +
                                "    <lookDownUpAngle>0.00</lookDownUpAngle>\n" +
                                "</PTZAbsoluteEx>";
                        System.out.println("focusing");
                        System.out.println("focusing result: " + httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx", focusingBody));

                        Thread.sleep(400);

                        answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");

                        parsedAnswer = HttpSenderService.getMapFromXMLString(answer);


                        if(!(Integer.parseInt(parsedAnswer.get("focus")) < focusing.intValue() + 100 && Integer.parseInt(parsedAnswer.get("focus")) > focusing.intValue() - 100)){
                            System.out.println("cur_focus " + Integer.parseInt(parsedAnswer.get("focus")));
                            System.out.println("need focus " + focusing.intValue());
                            System.out.println(Integer.parseInt(parsedAnswer.get("focus")) < focusing.intValue() + 100 && Integer.parseInt(parsedAnswer.get("focus")) > focusing.intValue() - 100);
                        }
                        else{
                            System.out.println("focused");
                            break;
                        }
                        if(focusingCounter > 25){
                            System.out.println("Предупреждение: не установлена нужная фокусировка.");
                            System.out.println("Тестовый режим: Возвращаю картинку.");
                            break;
                            //return false;
                        }
                    }



                    String presetBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                            "<PTZPreset version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                            "    <enabled>true</enabled>\n" +
                            "    <id>1</id>\n" +
                            "    <presetName>area 1</presetName> \n" +
                            "    <AbsoluteHigh>\n" +
                            "        <elevation>" + parsedVertical + "</elevation>\n" +
                            "        <azimuth>" + parsedHorizontal + "</azimuth>\n" +
                            "        <absoluteZoom>150</absoluteZoom>\n" +
                            "    </AbsoluteHigh>\n" +
                            "</PTZPreset>";

                    httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/presets/1", presetBody);

                    System.out.println(httpSenderService.sendPutRequest("/ISAPI/PTZCtrl/channels/2/presets/1/goto", ""));


                    return true;
                }

                if(tryCounter > 25){
                    return false;
                }

                tryCounter += 1;
            }

        } catch (Exception e) {
            System.out.println("Goto coordinates error: " + e);
            return false;
        }

    }

    public String gotoAndGetImage(Double horizontal, Double vertical, Double focusing){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);
            System.out.println("SENDER created");
            Boolean gotoResult = gotoCoordinatesNoConfig(horizontal, vertical, focusing);
            System.out.println("went To Coordinates No Config");
            System.out.println(gotoResult);
            if(gotoResult){
                // получить новое фото и сохранить его с новым индексом
                System.out.println("preparing for image getting");
                if(httpSenderService.getImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\img", "/ISAPI/Streaming/channels/2/picture").equals("conError")){
                    return "conError";
                }
                else{
                    System.out.println("Image got");
                    return "img/got_pic" + GotPicImageCounter.getCurrentCounter() + ".jpg"; //!
                }


            }
            else{
                return "conError";
            }
        }
        catch (Exception e) {
            System.out.println("Goto coordinates error(no config): " + e);
            return "conError";
        }
    }

    public String gotoAndSaveImage(Double horizontal, Double vertical, Double focusing, String areaName){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);

            Boolean gotoResult = gotoCoordinates(horizontal, vertical, focusing);
            System.out.println(gotoResult);


            if(gotoResult){
                // получить новое фото и сохранить его с новым индексом
                Calendar calendar = Calendar.getInstance();
                httpSenderService.saveImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + calendar.get(Calendar.DAY_OF_MONTH) +"_"+ (calendar.get(Calendar.MONTH) + 1) + "\\" + areaName, "/ISAPI/Streaming/channels/2/picture", calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" +calendar.get(Calendar.SECOND));
                return "ok"; //!
            }

        } catch (Exception e) {
            System.out.println("Goto and save image error: " + e);
        }

        return "error";
    }

    public String saveImage(String areaName){
        try{
            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);
            Calendar calendar = Calendar.getInstance();
            httpSenderService.saveImage(System.getProperty("user.dir")+"\\src\\main\\upload\\static\\imgData\\" + calendar.get(Calendar.DAY_OF_MONTH) +"_"+ (calendar.get(Calendar.MONTH) + 1) + "\\" + areaName, "/ISAPI/Streaming/channels/2/picture", calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" +calendar.get(Calendar.SECOND));
            return "ok";

        } catch ( Exception e){
            System.out.println("Save image error: " + e);
        }

        return "error";
    }

    public String configureArea(Integer x1, Integer y1, Integer x2, Integer y2){
        try {
            System.out.println("x1 " + x1);
            System.out.println("y1 " + y1);
            System.out.println("x2 " + x2);
            System.out.println("y2 " + y2);

            Integer integerX1 = x1 * 1000 / 640 ;
            Integer integerX2 = x2 * 1000 / 640 ;

            Integer integerY1 = y1 * 1000 / 380;
            Integer integerY2 = y2 * 1000 / 380;

            System.out.println("x1" + integerX1);
            System.out.println("y1" + integerY2);
            System.out.println("x2" + integerX2);
            System.out.println("y2" + integerY2);



            String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<ThermometryRegion version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <id>1</id>\n" +
                    "    <enabled>true</enabled>\n" +
                    "    <name>Current area</name>\n" +
                    "    <emissivity>0.98</emissivity>\n" +
                    "    <distance>0</distance>\n" +
                    "    <reflectiveEnable>false</reflectiveEnable>\n" +
                    "    <reflectiveTemperature>20.0</reflectiveTemperature>\n" +
                    "    <type>region</type>\n" +
                    "    <Region>\n" +
                    "        <RegionCoordinatesList>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>" + integerX1 + "</positionX>\n" +
                    "                <positionY>" + integerY1 + "</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>" + integerX1 + "</positionX>\n" +
                    "                <positionY>" + integerY2 + "</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>"+ integerX2 +"</positionX>\n" +
                    "                <positionY>"+ integerY2 +"</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "            <RegionCoordinates>\n" +
                    "                <positionX>"+ integerX2 +"</positionX>\n" +
                    "                <positionY>"+ integerY1 +"</positionY>\n" +
                    "            </RegionCoordinates>\n" +
                    "        </RegionCoordinatesList>\n" +
                    "    </Region>\n" +
                    "    <distanceUnit>centimeter</distanceUnit>\n" +
                    "    <emissivityMode>customsettings</emissivityMode>\n" +
                    "</ThermometryRegion>" ;

            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);

            System.out.println(httpSenderService.sendPutRequest("/ISAPI/Thermal/channels/2/thermometry/1/regions/1", body)); // Need errors handler
            return "ok";
        } catch (IOException e) {
            System.out.println("Configure area error: " + e);
        }
        return "error";
    }

    public Object getTemperatureInArea(Integer areaId){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);



            String jsonString = httpSenderService.sendGetRequest("/ISAPI/Thermal/channels/2/thermometry/1/rulesTemperatureInfo/" + areaId + "?format=json");

            System.out.println(jsonString);

            org.json.JSONObject data = new org.json.JSONObject(jsonString);

            data = data.getJSONObject("ThermometryRulesTemperatureInfo");

            Double temperature = data.getDouble("maxTemperature");

            //System.out.println(temperature);

            return temperature;

        } catch (Exception e) {
            System.out.println("Getting temperature error: " + e);
        }

        return null;
    }



    public Integer setAutoFocus(){
        try {
            String body1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                           "<FocusConfiguration version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                           "    <focusStyle>MANUAL</focusStyle>\n" +
                           "    <focusLimited>5000</focusLimited>\n" +
                           "    <focusSpeed>1</focusSpeed>\n" +
                           "    <focusSensitivity>1</focusSensitivity>\n" +
                           "    <temperatureChangeAdaptEnabled>true</temperatureChangeAdaptEnabled>\n" +
                           "</FocusConfiguration>";

            String body2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<FocusConfiguration version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\">\n" +
                    "    <focusStyle>SEMIAUTOMATIC</focusStyle>\n" +
                    "    <focusLimited>10000</focusLimited>\n" +
                    "    <focusSpeed>1</focusSpeed>\n" +
                    "    <focusSensitivity>1</focusSensitivity>\n" +
                    "    <temperatureChangeAdaptEnabled>true</temperatureChangeAdaptEnabled>\n" +
                    "</FocusConfiguration>";

            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);
            


            httpSenderService.sendPutRequest("/ISAPI/Image/channels/2/focusConfiguration", body1);
            httpSenderService.sendPutRequest("/ISAPI/Image/channels/2/focusConfiguration", body2);
            Thread.sleep(5000);
            Integer previousFocusing = -29029304; // ???
            while (true){
                String answer = httpSenderService.sendGetRequest("/ISAPI/PTZCtrl/channels/2/absoluteEx");
                Thread.sleep(500);
                Map<String, String> parsedAnswer = HttpSenderService.getMapFromXMLString(answer);

                Integer curFocusing = Integer.parseInt( parsedAnswer.get("focus") );

                System.out.println("prev: " + previousFocusing);
                System.out.println("cur: " + curFocusing);
                if(curFocusing.equals(previousFocusing) ){
                    return curFocusing;
                }
                previousFocusing = curFocusing;

            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String reboot(){
        if (! isRebooting){
            AlertHolder alertHolder = AlertHolder.getInstance();
            switch (id.toString()){
                case "1":
                    alertHolder.setFirstTIError(true);
                    break;
                case "2":
                    alertHolder.setSecondTIError(true);
                    break;
                case "3":
                    alertHolder.setThirdTIError(true);
                    break;
                case "4":
                    alertHolder.setFourthTIError(true);
                    break;
            }
            new Thread((Runnable) () -> {
                this.isRebooting = true;
                this.isBusy = true;
                this.needReboot = false;
                try {
                    Integer count = 0;
                    HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);

                    if (! httpSenderService.sendPutRequest("/ISAPI/System/reboot", "").equals("")){
                        //ISAPI/System/reboot
                        while(! status().equals("ok")){
                            Thread.sleep(30000);
                            count += 1;
                            if(count > 10){
                                Logger.todayLogRecord("TI"+this.id + " команда reboot не прошла");
                                System.out.println("Reboot Error");
                                break;
                            }
                        }
                        Thread.sleep(120000);
                    }
                    else{
                        System.out.println("reboot did not send for " + this.id );
                        this.needReboot = true;
                    }


                } catch (Exception e) {
                    Logger.todayLogRecord("TI"+this.id + " ошибка функции reboot");
                    System.out.println("Reboot Error");
                }
                finally {
                    this.isRebooting = false;
                    this.isBusy = false;
                }
            }).start();
            return "Перезапуск инициализирован";
        }
        return "Тепловизор уже перерезапускаетcя";
    }

    public String status(){
        try {
            HttpSenderService httpSenderService = HttpSenderService.getHttpSenderService(IP, port, realm, nonce);
            String status = httpSenderService.sendGetRequest("/ISAPI/System/status");
            System.out.println("status: "  + status);
            if(status.equals("conError")){
                return status;
            }
        }
        catch (Exception e) {
            System.out.println("Status error: " + e);
            return "conError";
        }
        return "ok";
    }



//    public String lensDeicing(){
//        return "ok";
//    }

}
