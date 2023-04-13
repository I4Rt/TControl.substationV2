package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.CloseData;
import com.i4rt.temperaturecontrol.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;


@Repository
public interface CloseDataRepo extends JpaRepository<CloseData, Long> {
    @Query(nativeQuery = true, value="SELECT * FROM close_data where control_object_id = :coID order by datetime desc limit 1")
    CloseData getLastById(@Param("coID") Long id);

    @Query(nativeQuery = true, value="SELECT * FROM close_data where control_object_id = :searchId and datetime >= :begin and datetime <= :end order by datetime desc")
    ArrayList<CloseData> getCloseDataByDatetimeInRange(@Param("searchId") Long searchId, @Param("begin") Date begin, @Param("end") Date end);

    @Query(nativeQuery = true, value="SELECT * FROM close_data where control_object_id = :searchId order by datetime desc limit :limit")
    ArrayList<CloseData> getCloseDataLimited(@Param("searchId") Long searchId, @Param("limit") Integer limit);

    @Query(nativeQuery = true, value = "SELECT * FROM close_data where control_object_id = :searchId and datetime >= :begin and datetime <= :end order by node_temperature desc limit 1")
    ArrayList<CloseData> getHighestTemperature(@Param("searchId") Long searchId, @Param("begin") Date begin, @Param("end") Date end);

    @Query(nativeQuery = true, value="SELECT * FROM close_data where control_object_id = :searchId and datetime >= :begin and datetime <= :end and (node_temperature >= :dangerTemp or node_temperature >= :warningTemp) order by datetime")
    ArrayList<CloseData> getOverheatingTemperatures(@Param("searchId") Long searchId, @Param("begin") Date begin, @Param("end") Date end, @Param("dangerTemp")Double dangerTemp, @Param("warningTemp")Double warningTemp);
}
