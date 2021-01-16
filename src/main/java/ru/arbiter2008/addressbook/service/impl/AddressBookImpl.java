package ru.arbiter2008.addressbook.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import ru.arbiter2008.addressbook.model.Person;
import ru.arbiter2008.addressbook.repository.IPersonRepository;
import ru.arbiter2008.addressbook.service.IAddressBook;


@Service
public class AddressBookImpl implements IAddressBook {

    private IPersonRepository personRepository;


    @Override
    public void add(Person person) {
        personRepository.save(person);
    }


    @Override
    public void update(Person person) {
        personRepository.save(person);
    }


    @Override
    public void delete(Person person) {
        personRepository.delete(person);
    }


    @Override
    public Page<Person> findAll(int from, int count) {
        return personRepository.findAll(PageRequest.of(from, count, Direction.ASC, "fio"));
    }


    @Override
    public Page<Person> findAll(int from, int count, String... text) {
        return personRepository.findByFioContainingIgnoreCase(text[0], PageRequest.of(from, count, Sort.Direction.ASC, "fio"));
    }


    @Autowired
    public void setPersonRepository(IPersonRepository personRepository) {
        this.personRepository = personRepository;
    }

}
