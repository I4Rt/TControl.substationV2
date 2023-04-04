package com.i4rt.temperaturecontrol.databaseInterfaces;

import com.i4rt.temperaturecontrol.model.ControlObject;
import com.i4rt.temperaturecontrol.model.ControlObjectTIChangingPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public interface ControlObjectTIChangingPartRepo extends JpaRepository<ControlObjectTIChangingPart, Long> {

    @Query(nativeQuery = true, value="SELECT * FROM control_objecttichanging_part where temperature_class = 'danger'")
    List<ControlObjectTIChangingPart> getDanger();
    @Query(nativeQuery = true, value="SELECT * FROM control_objecttichanging_part where temperature_class = 'dangerDifference'")
    List<ControlObjectTIChangingPart> getDangerDifference();

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM control_objecttichanging_part where control_object_object_id = :id")
    void deleteByCOId(@Param("id") Long id);


}
