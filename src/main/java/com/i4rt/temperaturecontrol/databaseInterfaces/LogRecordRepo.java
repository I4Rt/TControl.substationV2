package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.LogRecord;
import com.i4rt.temperaturecontrol.model.NodeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LogRecordRepo extends JpaRepository<LogRecord, Long> {
    @Query(nativeQuery = true, value = "SELECT * FROM log_record  order by id desc limit :limit")
    List<LogRecord> getLast(@Param("limit") int i);
}
