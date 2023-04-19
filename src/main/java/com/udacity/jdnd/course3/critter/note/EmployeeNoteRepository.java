package com.udacity.jdnd.course3.critter.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeNoteRepository extends JpaRepository<EmployeeNote, Long> {

    EmployeeNote findByIdAndEmployeeId(Long noteId, Long employeeId);

    @Query("select new com.udacity.jdnd.course3.critter.note.Note(u.id, u.description, u.note, u.lastUpdateTime) from EmployeeNote u where employee.id = ?1")
    List<Note> findAllProjectedBy(Long employeeId);

    @Query("select u.id from EmployeeNote u where employee.id = ?1")
    List<Long> findAllIdsProjectedBy(Long employeeId);
}

