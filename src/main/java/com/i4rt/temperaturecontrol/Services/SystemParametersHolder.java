package com.i4rt.temperaturecontrol.Services;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class SystemParametersHolder {

    private static SystemParametersHolder instance;

    private Boolean tooLowTemperatureStopMark;
    private Integer logLimit;

    private SystemParametersHolder() {
        this.tooLowTemperatureStopMark = false;
        this.logLimit = 100;
    }

    public static SystemParametersHolder getInstance() {

        if(instance == null){
            instance = new SystemParametersHolder();
        }
        return instance;
    }
}
