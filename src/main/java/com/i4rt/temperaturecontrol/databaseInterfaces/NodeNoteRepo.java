package com.i4rt.temperaturecontrol.databaseInterfaces;


import com.i4rt.temperaturecontrol.model.NodeNote;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface NodeNoteRepo extends JpaRepository<NodeNote, Long> {

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value="delete FROM node_note where control_object_id = :id")
    void deleteByCOID(@Param("id") Long id);
}
