package com.github.arielcarrera.cdi.test.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer value;

    @Column(nullable = true, unique = true)
    private Integer uniqueValue;

    public TestEntity() {
	super();
    }

    public TestEntity(Integer id, Integer value) {
	super();
	this.id = id;
	this.value = value;
    }

    public TestEntity(Integer id, Integer value, Integer uniqueValue) {
	super();
	this.id = id;
	this.value = value;
	this.uniqueValue = uniqueValue;
    }

    public TestEntity(Integer id, Integer value, Integer uniqueValue, int status) {
	super();
	this.id = id;
	this.value = value;
	this.uniqueValue = uniqueValue;
    }


    public Integer getId() {
	return id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public Integer getValue() {
	return value;
    }

    public void setValue(Integer value) {
	this.value = value;
    }

    public Integer getUniqueValue() {
	return uniqueValue;
    }

    public void setUniqueValue(Integer uniqueValue) {
	this.uniqueValue = uniqueValue;
    }


    @Override
    public String toString() {
	return "[" + TestEntity.class.getSimpleName() + ": id=" + String.valueOf(id) + ", value=" + String.valueOf(value) + ", uniqueValue="
		+ String.valueOf(uniqueValue) + "]";
    }

    @Override
    public boolean equals(Object o) {
	if (this == o) {
	    return true;
	}

	if (o == null || getClass() != o.getClass()) {
	    return false;
	}

	TestEntity that = (TestEntity) o;

	return value.equals(that.value) && uniqueValue.equals(that.uniqueValue);

    }

    @Override
    public int hashCode() {
	return value != null ? value.hashCode() : 0;
    }

}
