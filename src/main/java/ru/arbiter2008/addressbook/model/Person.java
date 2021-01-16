package ru.arbiter2008.addressbook.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class Person {

    // Указываем, что поле заполняется в БД.
    // Когда добавляем новый объект в БД - он возвращается уже с новым id (autoincrement)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column
    private Long id;

    @Column
    private String fio;

    @Column
    private byte[] photo;

    @Column
    private String address;

    @Column
    private String phone;

}
