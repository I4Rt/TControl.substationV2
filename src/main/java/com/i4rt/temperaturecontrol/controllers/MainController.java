package com.i4rt.temperaturecontrol.controllers;

import com.i4rt.temperaturecontrol.Services.AlertSetter;
import com.i4rt.temperaturecontrol.Services.SystemParametersHolder;
import com.i4rt.temperaturecontrol.additional.GotPicImageCounter;
import com.i4rt.temperaturecontrol.additional.UploadedImageCounter;
import com.i4rt.temperaturecontrol.databaseInterfaces.*;
import com.i4rt.temperaturecontrol.device.ThermalImager;
import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.LogRecord;
import com.i4rt.temperaturecontrol.model.User;
import org.hibernate.tool.schema.internal.StandardAuxiliaryDatabaseObjectExporter;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.xml.bind.SchemaOutputResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Controller
public class MainController {

    @Autowired
    private ControlObjectRepo controlObjectRepo;
    @Autowired
    private final MeasurementRepo measurementRepo;
    @Autowired
    private final ThermalImagerRepo thermalImagerRepo;
    @Autowired
    private final UserRepo userRepo;
    @Autowired
    private LogRecordRepo logRecordRepo;

    @Autowired
    ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo;


    public MainController(ControlObjectRepo controlObjectRepo, ControlObjectTIChangingPartRepo controlObjectTIChangingPartRepo, MeasurementRepo measurementRepo, ThermalImagerRepo thermalImagerRepo, UserRepo userRepo, LogRecordRepo logRecordRepo) {
        this.controlObjectTIChangingPartRepo = controlObjectTIChangingPartRepo;
        this.controlObjectRepo = controlObjectRepo;
        this.measurementRepo = measurementRepo;
        this.thermalImagerRepo = thermalImagerRepo;
        this.userRepo = userRepo;
        this.logRecordRepo = logRecordRepo;

        AlertSetter.setNeedBeep(controlObjectTIChangingPartRepo);
    }

    @GetMapping("/")
    public String gotoMain(){
        return "redirect:/main";
    }

    @GetMapping("main")
    public String getMainPage(Model model) throws IOException {

        List<ControlObject> controlObjects = controlObjectRepo.findAll();
        List<ControlObject> controlObjectsToDisplay = controlObjectRepo.getControlObjectsToDisplay();
        model.addAttribute("controlObjects", controlObjects);
        model.addAttribute("controlObjectsToDisplay", controlObjectsToDisplay);
        model.addAttribute("limit", SystemParametersHolder.getInstance().getLogLimit());

        List<Map> result = new ArrayList<>();
        for(LogRecord r: logRecordRepo.getLast(SystemParametersHolder.getInstance().getLogLimit())){
            result.add(r.getMapped());
        }
        model.addAttribute("logList", result);
        model.addAttribute("src", "\"img/bg/map"+ UploadedImageCounter.getCurrentCounter() +".png\""); //Make getCurrentCounter method do not throw exception (try/catch)

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);

        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "mainWindow";
    }

    @GetMapping("adding")
    public String getAddingPage(Model model) throws IOException {

        List<ControlObject> controlObjects = controlObjectRepo.findAll();
        model.addAttribute("controlObjects", controlObjects);

        ThermalImager ti = thermalImagerRepo.getById(Long.valueOf(1));

        model.addAttribute("horizontal", ti.getCurHorizontal());
        model.addAttribute("vertical", ti.getCurVertical());
        model.addAttribute("focusing", 500); // not work (non functionality)
        model.addAttribute("src", "img/loading.gif");


        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(true);
        userRepo.save(user);


        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "addingWindow";

    }


    @GetMapping("area")
    public String getAreaPage(@RequestParam Long id, Model model){
        ControlObject controlObject =  controlObjectRepo.getById(id);

        model.addAttribute("controlObject", controlObject);
        model.addAttribute("coordinatesString",  controlObject.getCoordinatesString());
        model.addAttribute("sector",
                "top: " + controlObject.getY() +
                        "px; left: " + controlObject.getX() +
                        "px; width: " + controlObject.getAreaWidth() +
                        "px; height: " + controlObject.getAreaHeight() +"px;");


        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "areaWindow";
    }

    @GetMapping("newArea")
    public String addingAreaPage(Model model){
        ControlObject controlObject =  new ControlObject();
        System.out.println(controlObjectRepo.getLastObjectId());



        model.addAttribute("controlObject", controlObject);
        model.addAttribute("coordinatesString",  "");

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "newAreaWindow";
    }


    @GetMapping("/register")
    public String registerPage(Model model){

        User user = new User();

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        model.addAttribute("user",  user);
        model.addAttribute("message",  "");

        return "registerPage";
    }

    @PostMapping("/registerUser")
    public String registerUser(@ModelAttribute User user,Model model){

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        model.addAttribute("user",  user);

        if(userRepo.getByUserLogin(user.getLogin()) == null){
            if(user.getPassword().length() >= 10){
                if(user.getPassword().equals(user.getPasswordRepeat())){
                    user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPassword()));
                    user.setPasswordRepeat(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPasswordRepeat()));
                    user.setRole(user.getRole());

                    System.out.println(user);

                    userRepo.save(user);
                    model.addAttribute("message",  "Пользователь создан");
                }
                else{
                    model.addAttribute("message",  "Пароли не совпали");
                }
            }
            else{
                model.addAttribute("message",  "Длина пароля минимум 10 символов");
            }

        }
        else{
            model.addAttribute("message",  "Пользователь с таким логином существует");
        }

        return "registerPage";
    }

    @GetMapping("/login")
    public String loginPage(Model model){

        User user = new User();

        model.addAttribute("user", user);
        model.addAttribute("message", "");
        return "loginPage";
    }

    @GetMapping("/report")
    public String reportPage(Model model){

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);

        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "reportPage";
    }

    @GetMapping("/usersList")
    public String userListPage(Model model){
        List<User> userList = userRepo.getOperatorUsers();
        model.addAttribute(userList);

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        model.addAttribute(user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        model.addAttribute("adderClass", "");

        return "userListPage";
    }

    @GetMapping("/user")
    public String userPage(Model model){
        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        model.addAttribute("user", user);
        model.addAttribute("updateUser", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }
        return "userPage";
    }


    @PostMapping( "/updatePassword")
    public String updatePassword(@ModelAttribute User user, Model model){

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());

        model.addAttribute("user",  curUser);
        model.addAttribute("updateUser", user);

        System.out.println(user);

        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        if(user.getPassword().length() >= 10){
            if(user.getPassword().equals(user.getPasswordRepeat())){
                user.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPasswordRepeat()));
                user.setPasswordRepeat(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(user.getPasswordRepeat()));
                userRepo.save(user);
                model.addAttribute("message",  "Пароль изменен");
            }
            else{
                model.addAttribute("message",  "Пароли не совпали");
            }
        }
        else{
            model.addAttribute("message",  "Длина пароля минимум 10 символов");
        }

        if(curUser.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "userPage";
    }

    @GetMapping("/updateUser")
    public String updateUser(@RequestParam Long id, Model model){
        User updateUser = userRepo.getById(id);
        System.out.println(updateUser);
        model.addAttribute("updateUser", updateUser);

        User curUser = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", curUser);

        curUser.setThermalImagerGrabbed(false);
        userRepo.save(curUser);

        if(curUser.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "userPage";
    }

    @GetMapping("/deleteUser")
    public String deleteUser(@RequestParam Long id, Model model){
        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        userRepo.deleteById(id);

        List<User> userList = userRepo.getOperatorUsers();
        model.addAttribute(userList);

        model.addAttribute("adderClass", "");

        return "userListPage";
    }


    @PostMapping("/authorizeUser")
    public String authorizeUser(@ModelAttribute User user, Model model){

        User curUser = userRepo.getByUserLogin(user.getLogin());

        model.addAttribute("message", "Неверные данные входа");
        return "login";
    }


    @GetMapping("/pointReport")
    public String pointReportPage(@RequestParam Long id, Model model){
        String pointName = controlObjectRepo.getById(id).getName();

        User user = userRepo.getByUserLogin(SecurityContextHolder.getContext().getAuthentication().getName());
        model.addAttribute("user", user);
        model.addAttribute("pointName", pointName);
        model.addAttribute("pointId", id);

        user.setThermalImagerGrabbed(false);
        userRepo.save(user);

        if(user.getRole().equals("ADMIN")){
            model.addAttribute("adderClass", "");
        }
        else{
            model.addAttribute("adderClass", "hidden");
        }

        return "/pointReportPage";
    }


}
