/**
* (C)2015 Brocade Communications Systems, Inc.
* 130 Holger Way, San Jose, CA 95134.
* All rights reserved.
*
* @author Anton Ivanov <aivanov@brocade.com>
* Brocade, the B-wing symbol, Brocade Assurance, ADX, AnyIO, DCX, Fabric OS,
* FastIron, HyperEdge, ICX, MLX, MyBrocade, NetIron, OpenScript, VCS, VDX, and
* Vyatta are registered trademarks, and The Effortless Network and the On-Demand
* Data Center are trademarks of Brocade Communications Systems, Inc., in the
* United States and in other countries. Other brands and product names mentioned
* may be trademarks of others.
*
* Use of the software files and documentation is subject to license terms.
*/
package com.att.networkadapter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Defines a unit test class which tests the provider.
 *
 * This class leverages the AbstractDataBrokerTest class which starts a
 * real MD-SAL implementation to use inside of your unit tests.
 *
 * This is not an exhaustive test, but rather is used to illustrate how one
 * can leverage the AbstractDataBrokerTest to test MD-SAL providers/listeners.
 *
 */
public class NetworkadapterProviderTest extends AbstractDataBrokerTest{
//
//	ExecutorService threadPool = Executors.newSingleThreadExecutor();
//	NetworkadapterProvider provider;
//	DataBroker dataBroker;
//
//	/**
//	 * The @Before annotation is defined in the AbstractDataBrokerTest class. The method
//	 * setupWithDataBroker is invoked from inside the @Before method and is used
//	 * to initialize the databroker with objects for a test runs.
//	 *
//	 * In our case we use this oportunity to create an instance of our provider and
//	 * initialize it (which registers it as a listener etc).
//	 *
//	 * This method runs before every @Test method below.
//	 */
//	@Override
//	protected void setupWithDataBroker(DataBroker dataBroker) {
//		super.setupWithDataBroker(dataBroker);
//
//		this.dataBroker = dataBroker;
//
//		provider = new NetworkadapterProvider(getRpcRegistryDependency());
//		provider.setDataBroker( dataBroker );
//		provider.initialize();
//	}
//
//
//	@Override
//    protected void setupWithSchema(SchemaContext context) {
//        super.setupWithSchema(context);
//    	//This is a prime case for using a "Mock", but given that we know
//		//we have a real schema, we use the real thing.
////	    TypeEnumHelper helper = new TypeEnumHelper();
////        helper.setSchemaService( context );
////        helper.initialize();
////        provider.setTypeHelper( helper );
//    }
//
//	/**
//	 * Shuts down our provider, testing close code. @After runs after every @Test method below.
//	 */
//	@After
//	public void stop() throws Exception{
//		provider.close();
//	}
//
//	/**
//	 * This validates that when a task is created, the run count is initialized to 0
//	 */
//	@Test
//	public void setValueOnNewItemCreate() throws InterruptedException, ExecutionException {
//		createTask( "taskId1", "Task 1" );
//
////		Task t = readTask( "taskId1" );
////		assertEquals( 0, (int)t.getRunCount() );
//	}
//
//	/**
//	 * Returns the task item from the data broker which has the given task ID.
//	 */
////	private Task readTask( String taskId ) throws InterruptedException, ExecutionException {
////		ReadOnlyTransaction readTx = dataBroker.newReadOnlyTransaction();
////		InstanceIdentifier<Task> tId = InstanceIdentifier.builder( TaskContainer.class ).child( Task.class, new TaskKey( taskId ) ).build();
////		CheckedFuture<Optional<Task>, ReadFailedException> readResult = readTx.read(LogicalDatastoreType.OPERATIONAL, tId );
////
////		//Calling future.get inside of a test is OK because we WANT to block the junit test.
////		//Do NOT do this inside production code.
////		Optional<Task> optional = readResult.get();
////		Task t = optional.get();
////		return t;
////	}
//
//	/**
//	 * Validates that if no task was found with the given ID, that a failure is returned.
//	 */
//	@Test
//	public void returnErrorIfTaskIdNotFound() throws InterruptedException, ExecutionException{
//
////		RunTaskInput input = new RunTaskInputBuilder().setTaskId( "bad_task_name" ).build();
////		Future<RpcResult<Void>> future = provider.runTask( input );
//
////		assertNotNull( "Null future returned", future );
////
////		//Calling future.get inside of a test is OK because we WANT to block the junit test.
////		//Do NOT do this inside production code.
////		RpcResult<Void> rpcResult = future.get();
//
////		assertNotNull( "RPC result was null", rpcResult );
////
////		assertFalse( "Rpc Call was successfull even though it should not have been", rpcResult.isSuccessful() );
//	}
//
//	/**
//	 * Validates that if a task ID is found, that the runTask returns success AND that the task runCount is
//	 * incremented by 1.
//	 */
//	@Test
//	public void returnSuccessSinceTaskFound() throws InterruptedException, ExecutionException{
//
//		String taskId = "task-id-1";
//		String taskName = "Task 1";
//
//		createTask(taskId, taskName);
////
////		RunTaskInput input = new RunTaskInputBuilder().setTaskId( taskId ).build();
////		Future<RpcResult<Void>> future = provider.runTask( input );
//
////		assertNotNull( "Null future returned", future );
////
////		//Calling future.get inside of a test is OK because we WANT to block the junit test.
////		//Do NOT do this inside production code.
////		RpcResult<Void> rpcResult = future.get();
//
////		assertNotNull( "RPC result was null", rpcResult );
////
////		assertTrue( "Rpc Call was a failure even though it should not have been", rpcResult.isSuccessful() );
//
////		Task t = readTask( taskId );
////		assertEquals( 1, (int)t.getRunCount() );
//
//
//	}
//
//	/**
//	 * A helper method which creates a task inside of the data broker.
//	 */
//	private void createTask(String taskId, String taskName)
//			throws InterruptedException, ExecutionException {
//		WriteTransaction writeTx = dataBroker.newWriteOnlyTransaction();
//
////		TaskContainerBuilder builder = new TaskContainerBuilder();
////		TaskBuilder t1 = new TaskBuilder();
////		TaskKey tKey = new TaskKey( taskId );
////		t1.setKey( tKey );
////		t1.setTaskId(taskId);
////		t1.setName( taskName );
////		t1.setType( Type.UserTask );
////
////		builder.setTask(Collections.singletonList( t1.build() ) );
////
////		InstanceIdentifier<TaskContainer> myFirstAppID = InstanceIdentifier.builder( TaskContainer.class ).build();
////		writeTx.merge( LogicalDatastoreType.CONFIGURATION, myFirstAppID, builder.build());
//
//		CheckedFuture<Void, TransactionCommitFailedException> wFuture = writeTx.submit();
//
//		//Calling future.get inside of a test is OK because we WANT to block the junit test.
//		//Do NOT do this inside production code though.
//		wFuture.get();
//	}

}
