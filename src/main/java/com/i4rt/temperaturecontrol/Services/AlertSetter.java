package com.i4rt.temperaturecontrol.Services;

import com.i4rt.temperaturecontrol.databaseInterfaces.ControlObjectTIChangingPartRepo;
import com.i4rt.temperaturecontrol.model.ControlObjectTIChangingPart;

import java.util.List;

public class AlertSetter{
    public static void setAlert(Long id, Boolean value){
        AlertHolder alertHolder = AlertHolder.getInstance();
        System.out.println("id:" + id.toString());
        switch (id.toString()){
            case "1":
                alertHolder.setFirstTIError(value);
                break;
            case "2":
                alertHolder.setSecondTIError(value);
                break;
            case "3":
                alertHolder.setThirdTIError(value);
                break;
            case "4":
                alertHolder.setFourthTIError(value);
                break;
        }
    }

    public static void setNeedBeep(ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo){
        List<ControlObjectTIChangingPart> dangerControlObjects = controlObjectTIChangingPartRepo.getDanger();
        List<ControlObjectTIChangingPart> dangerDifControlObjects = controlObjectTIChangingPartRepo.getDangerDifference();

        for(ControlObjectTIChangingPart coti : dangerControlObjects){
            if(coti.getControlObject().getNeedBeep()){
                AlertHolder.getInstance().setNeedBeep(true);
                return;
            }
        }
        for(ControlObjectTIChangingPart coti : dangerDifControlObjects){
            if(coti.getControlObject().getNeedBeep()){
                AlertHolder.getInstance().setNeedBeep(true);
                return;
            }
        }
        AlertHolder.getInstance().setNeedBeep(false);
    }
}