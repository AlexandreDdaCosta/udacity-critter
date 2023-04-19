package com.udacity.jdnd.course3.critter.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    CustomerNote findByIdAndCustomerId(Long noteId, Long customerId);

    @Query("select new com.udacity.jdnd.course3.critter.note.Note(u.id, u.description, u.note, u.lastUpdateTime) from CustomerNote u where customer.id = ?1")
    List<Note> findAllProjectedBy(Long customerId);

    @Query("select u.id from CustomerNote u where customer.id = ?1")
    List<Long> findAllIdsProjectedBy(Long customerId);
}

