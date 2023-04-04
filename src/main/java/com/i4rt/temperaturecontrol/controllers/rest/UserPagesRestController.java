package com.i4rt.temperaturecontrol.controllers.rest;

import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserPagesRestController {
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

    public UserPagesRestController(UserRepo userRepo, NodeNoteRepo nodeNoteRepo, ControlObjectRepo controlObjectRepo,ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo , MeasurementRepo measurementRepo, WeatherMeasurementRepo weatherMeasurementRepo, LogRecordRepo logRecordRepo) {
        this.userRepo = userRepo;
        this.nodeNoteRepo = nodeNoteRepo;
        this.controlObjectRepo = controlObjectRepo;
        this.controlObjectTIChangingPartRepo = controlObjectTIChangingPartRepo;
        this.measurementRepo = measurementRepo;
        this.weatherMeasurementRepo = weatherMeasurementRepo;
        this.logRecordRepo = logRecordRepo;
    }

//    @RequestMapping(value = "deleteUser", method = RequestMethod.POST)
//    public String deleteArea(@RequestParam Long id){
//        measurementRepo.deleteByCOID(id);
//        controlObjectTIChangingPartRepo.deleteByCOId(id);
//        nodeNoteRepo.deleteByCOID(id);
//        controlObjectRepo.deleteByID(id);
//        System.out.println("Deleting area by id " + id);
//        return "Область контроля удалена";
//    }


}
