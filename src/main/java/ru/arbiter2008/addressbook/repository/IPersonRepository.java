package ru.arbiter2008.addressbook.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.arbiter2008.addressbook.model.Person;


public interface IPersonRepository extends JpaRepository<Person, Long> {

    // поиск без постраничности (сразу все данные)
    List<Person> findByFioContainingIgnoreCase(String fio);

    // поиск с постраничностью
    Page<Person> findByFioContainingIgnoreCase(String fio, Pageable pageable);

}
