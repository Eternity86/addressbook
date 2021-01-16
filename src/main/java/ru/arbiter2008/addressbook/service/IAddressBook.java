package ru.arbiter2008.addressbook.service;


import org.springframework.data.domain.Page;

import ru.arbiter2008.addressbook.model.Person;


public interface IAddressBook {

    void add(Person person);

    void update(Person person);

    void delete(Person person);


    // постраничный вывод

    Page<Person> findAll(int from, int count);

    Page<Person> findAll(int from, int count, String... text);

}
