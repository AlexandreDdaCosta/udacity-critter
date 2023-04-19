package com.udacity.jdnd.course3.critter.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PetNoteRepository extends JpaRepository<PetNote, Long> {

    PetNote findByIdAndPetId(Long noteId, Long petId);

    @Query("select new com.udacity.jdnd.course3.critter.note.Note(u.id, u.description, u.note, u.lastUpdateTime) from PetNote u where pet.id = ?1")
    List<Note> findAllProjectedBy(Long petId);

    @Query("select u.id from PetNote u where pet.id = ?1")
    List<Long> findAllIdsProjectedBy(Long petId);
}

