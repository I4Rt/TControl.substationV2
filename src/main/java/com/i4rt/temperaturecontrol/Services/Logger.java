package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.model.NodeNote;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Date;

public class Logger {
    public static Boolean makeLogRecord(String fileName, NodeNote logRecord){
        try {
            // Defining the file name of the file
            Path filePath = Path.of(System.getProperty("user.dir")+"\\reports\\" +fileName);

            // Writing into the file
            if (Files.exists(filePath)){
                Files.writeString(filePath, logRecord + "\n", StandardOpenOption.APPEND);
            }
            else{
                Files.writeString(filePath, logRecord + "\n", StandardOpenOption.CREATE);
            }

            System.out.println("saved file-log record");
            return true;
        } catch (Exception e) {
            System.out.println("file-log error: " + e);
            return false;
        }
    }

    public static Boolean todayLogRecord(NodeNote logRecord){
        Calendar calendar = Calendar.getInstance();
        String fileName = (calendar.getInstance().get(Calendar.MONTH) + 1) +"_"+ calendar.get(Calendar.DAY_OF_MONTH) + ".log";
        return makeLogRecord(fileName, logRecord);
    }

    public static Boolean todayLogRecord(String text){
        System.out.println("saving file-log record");
        NodeNote logRecord = new NodeNote();
        logRecord.setDatetime(new Date());
        logRecord.setRecord(text);
        Calendar calendar = Calendar.getInstance();
        String fileName = (calendar.getInstance().get(Calendar.MONTH) + 1) +"_"+ calendar.get(Calendar.DAY_OF_MONTH) + ".log";
        return makeLogRecord(fileName, logRecord);
    }
}
