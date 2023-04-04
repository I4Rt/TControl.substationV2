package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.login = :login")
    User getByUserLogin(@Param("login") String login);

    @Query("SELECT u FROM User u WHERE u.thermalImagerGrabbed = true")
    User getUserThatGrabbedThermalImager();
    @Query("SELECT u FROM User u WHERE u.thermalImagerGrabbed = true")
    ArrayList<User> getAllUsersThatGrabbedThermalImager();

    @Query(nativeQuery = true, value = "SELECT * FROM all_users where role = 'OPERATOR'")
    List<User> getOperatorUsers();

    @Modifying
    @Transactional
    @Query( nativeQuery = true, value="update all_users set thermal_imager_grabbed = false where thermal_imager_grabbed = true")
    void setThermalImagerNotGrabbedForAllUsers();
}
