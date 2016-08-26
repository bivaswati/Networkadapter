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
 * <p>
 * Use of the software files and documentation is subject to license terms.
 */
package com.att.networkadapter;

import com.att.networkadapter.cliadapter.SshClient;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.com.att.networkadapter.rev150115.NetworkadapterService;
import org.opendaylight.yang.gen.v1.com.att.networkadapter.rev150115.SendCommandInput;
import org.opendaylight.yang.gen.v1.com.att.networkadapter.rev150115.SendCommandOutput;
import org.opendaylight.yang.gen.v1.com.att.networkadapter.rev150115.SendCommandOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Defines a base implementation for your provider. This class extends from a helper class
 * which provides storage for the most commonly used components of the MD-SAL. Additionally the
 * base class provides some basic logging and initialization / clean up methods.
 * <p>
 * To use this, copy and paste (overwrite) the following method into the TestApplicationProviderModule
 * class which is auto generated under src/main/java in this project
 * (created only once during first compilation):
 * <p>
 * <pre>
 *
 * @Override public java.lang.AutoCloseable createInstance() {
 *
 * com.att.networkadapter.TypeEnumHelper helper = new com.att.networkadapter.TypeEnumHelper();
 * helper.setSchemaService( getRootSchemaServiceDependency().getGlobalContext() );
 * helper.initialize();
 *
 * final NetworkadapterProvider provider = new NetworkadapterProvider();
 * provider.setTypeHelper( helper );
 * provider.setDataBroker( getDataBrokerDependency() );
 * provider.setNotificationService( getNotificationServiceDependency() );
 * provider.setRpcRegistry( getRpcRegistryDependency() );
 * provider.initialize();
 *
 * return new AutoCloseable() {
 *
 * @Override public void close() throws Exception {
 * //TODO: CLOSE ANY REGISTRATION OBJECTS CREATED USING ABOVE BROKER/NOTIFICATION
 * //SERVIE/RPC REGISTRY
 * provider.close();
 * }
 * };
 * }
 *
 *
 * </pre>
 */
public class NetworkadapterProvider implements AutoCloseable, NetworkadapterService {

    static SshClient sshClient = new SshClient(10000, 1, 1000, 50);
    private final Logger log = LoggerFactory.getLogger(NetworkadapterProvider.class);
    private final String appName = "Networkadapter";
    private final ExecutorService executor;
    protected RpcProviderRegistry rpcRegistry;
    protected BindingAwareBroker.RpcRegistration<NetworkadapterService> rpcRegistration;

    public NetworkadapterProvider(RpcProviderRegistry rpcProviderRegistry) {
        this.log.info("Creating provider for " + appName);
        executor = Executors.newFixedThreadPool(1);
        rpcRegistry = rpcProviderRegistry;
        initialize();
    }

    public void initialize() {
        log.info("Initializing provider for " + appName);
        rpcRegistration = rpcRegistry.addRpcImplementation(NetworkadapterService.class, this);
        initializeChild();
        log.info("Initialization complete for " + appName);
    }

    protected void initializeChild() {
//Override if you have custom initialization intelligence
    }

    @Override
    public void close() throws Exception {
        log.info("Closing provider for " + appName);
        executor.shutdown();
        rpcRegistration.close();
        log.info("Successfully closed provider for " + appName);
    }


    @Override
    public Future<RpcResult<SendCommandOutput>> sendCommand(SendCommandInput input) {

        SendCommandOutputBuilder sendCommandOutputBuilder = new SendCommandOutputBuilder();
        try {
            sshClient.connect(input.getIp().getValue(), input.getUserName(), input.getPassword());
        } catch (Exception e) {
            sendCommandOutputBuilder.setStatus("Failed");
            e.printStackTrace();
        }

        List<String> commands = set_Commands(input.getInterfaceName(), input.getOperationType(), input.getDescription());
        try {
            sshClient.sendCommands(commands, 5, 2);
            sendCommandOutputBuilder.setStatus("Success");
            System.out.println("Command Sent ");
        } catch (Exception e) {
            sendCommandOutputBuilder.setStatus("Failed");
            e.printStackTrace();
        }
        sshClient.disconnect();

        return RpcResultBuilder.success(sendCommandOutputBuilder.build()).buildFuture();
    }


    private List<String> set_Commands(String interfaceName, String operationType, String description) {
        List<String> commands = new ArrayList<String>();
        if (operationType.equalsIgnoreCase("add")) {

// For the time being change the interface description
            commands.add("configure terminal");
            commands.add("interface " + interfaceName);
            commands.add("description "+description);
            commands.add("end");
        }

        return commands;
    }


}
