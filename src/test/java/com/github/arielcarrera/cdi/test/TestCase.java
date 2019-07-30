package com.github.arielcarrera.cdi.test;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.jnp.server.NamingBeanImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.github.arielcarrera.cdi.test.config.DummyXAResourceRecoveryHelper;
import com.github.arielcarrera.cdi.test.config.JtaEnvironment;
import com.github.arielcarrera.cdi.test.config.TransactionalConnectionProvider;
import com.github.arielcarrera.cdi.test.entities.TestEntity;

/**
 * 
 * @author Ariel Carrera
 *
 */
public class TestCase {

    @Rule
    public WeldInitiator weld = WeldInitiator.from(new Weld()).inject(this).build();

    /**
     * JNDI server.
     */
    private static final NamingBeanImpl NAMING_BEAN = new NamingBeanImpl();

    /**
     * Transaction manager for transaction demarcation.
     */
    private static TransactionManager transactionManager;

    @Inject
    private Repository1 repo1;
    
    @Inject
    private Repository1 repo2;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
	System.out.println("Starting beforeClass() method");
	NAMING_BEAN.start();
	JNDIManager.bindJTAImplementation();
	new InitialContext().bind(TransactionalConnectionProvider.DATASOURCE_JNDI,
		TransactionalConnectionProvider.getDataSource());
	recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryModuleClassNames(getRecoveryModuleClassNames());
	recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);
	JtaEnvironment.setObjectStoreDir();
	RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT).getModules().stream()
		.filter(m -> m instanceof XARecoveryModule)
		.forEach(m -> ((XARecoveryModule) m).addXAResourceRecoveryHelper(new DummyXAResourceRecoveryHelper()));
    }

    static List<String> getRecoveryModuleClassNames() {
	List<String> recoveryModuleClassNames = new ArrayList<>();
	recoveryModuleClassNames.add(AtomicActionRecoveryModule.class.getName());
	recoveryModuleClassNames.add(XARecoveryModule.class.getName());

	return recoveryModuleClassNames;
    }

    @AfterClass
    public static void afterClass() {
	System.out.println("Starting afterClass() method");
	// Stop JNDI server
	NAMING_BEAN.stop();
    }

    @Before
    public void before() throws Exception {
	System.out.println("Starting before() method");
	transactionManager = InitialContext.doLookup("java:/TransactionManager");
    }

    @After
    public void after() {
	try {
	    System.out.println("Starting after() method");
	    transactionManager.rollback();
	    repo1.clear();
	    repo2.clear();
	} catch (Throwable t) {
	}
//	weld.shutdown();
    }

    private void assertEntities(TestEntity... expected) throws Exception {
	System.out.println("Starting assertEntities() method");
	assertEquals(Arrays.asList(expected), getEntitiesFromTheDatabase());
    }

    private List<TestEntity> getEntitiesFromTheDatabase() throws Exception {
	DataSource dataSource = InitialContext.doLookup("java:/testDS");
	Connection connection = dataSource.getConnection(TransactionalConnectionProvider.USERNAME,
		TransactionalConnectionProvider.PASSWORD);
	Statement statement = connection.createStatement();
	ResultSet resultSet = statement.executeQuery("SELECT `id`,`value`,`uniqueValue` FROM `TestEntity`");
	List<TestEntity> entities = new LinkedList<>();
	while (resultSet.next()) {
	    entities.add(
		    new TestEntity(resultSet.getInt("id"), resultSet.getInt("value"), resultSet.getInt("uniqueValue")));
	}
	resultSet.close();
	statement.close();
	connection.close();
	return entities;
    }

    private int count = 0;

    private TestEntity getNewEntity() {
	return new TestEntity(null, ++count, count);
    }
    
    //FAILED TEST
    @Test(expected=RuntimeException.class)
    public void testSuspendMergeFirst() throws Exception {
	System.out.println("Starting testSuspendMergeFirst() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
	try {
	    repo1.suspendAndRollback_MergeFirst(firstEntity, secondEntity);
	} catch (Exception e) {
	    assertEntities(secondEntity);
	    throw e;
	}
    }

    @Test(expected=RuntimeException.class)
    public void testSuspendNewTxFirst() throws Exception {
	System.out.println("Starting testSuspendNewTxFirst() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
	try {
	    repo1.suspendAndRollback_NewTxFirst(firstEntity, secondEntity);
	} catch (Exception e) {
	    assertEntities(firstEntity);
	    throw e;
	}
    }

    /**
     * Adds two entries to the database and commits the transaction. At the end of the test two entries should be in the
     * database.
     *
     * @throws Exception
     */
    @Test
    public void testCommit() throws Exception {
	System.out.println("Starting testCommit() method");
        TestEntity firstEntity = getNewEntity();
        TestEntity secondEntity = getNewEntity();
        transactionManager.begin();
        repo1.save(firstEntity);
        repo1.save(secondEntity);
        transactionManager.commit();
        assertEntities(firstEntity, secondEntity);
    }

    /**
     * Adds two entries to the database and rolls back the transaction. At the end of the test no entries should be in the
     * database.
     * 
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
	System.out.println("Starting testRollback() method");
	TestEntity firstEntity = getNewEntity();
	TestEntity secondEntity = getNewEntity();
        transactionManager.begin();
        repo1.save(firstEntity);
        repo1.save(secondEntity);
        transactionManager.rollback();
        assertEntities();
    }

}
