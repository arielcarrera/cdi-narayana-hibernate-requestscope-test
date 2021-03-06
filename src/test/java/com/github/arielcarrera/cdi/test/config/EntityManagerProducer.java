package com.github.arielcarrera.cdi.test.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * EntityManager producer
 * @author Ariel Carrera <carreraariel@gmail.com>
 *
 */
@ApplicationScoped
public class EntityManagerProducer {

    @Inject
    private EntityManagerFactory emf;

    @Produces
    @RequestScoped
    public EntityManager produceEntityManager() {
        return emf.createEntityManager();
    }

    public void close(@Disposes EntityManager em) {
        em.close();
    }
    
    
    @Inject
    private TransactionManager tm;

    @PostConstruct
    public void setTimeout() {
	try {
	    tm.setTransactionTimeout(600000);
	} catch (SystemException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
