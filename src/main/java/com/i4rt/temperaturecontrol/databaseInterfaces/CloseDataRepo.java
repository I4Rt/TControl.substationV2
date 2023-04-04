package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.CloseData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface CloseDataRepo extends JpaRepository<CloseData, Long> {
    @Query(nativeQuery = true, value="SELECT * FROM close_data where control_object_id = :coID order by id desc limit 1")
    CloseData getLastById(@Param("coID") Long id);
}
