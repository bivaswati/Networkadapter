package org.opendaylight.yang.gen.v1.brocade.training.networkadapter.provider.impl.rev140523;

import com.att.networkadapter.NetworkadapterProvider;

public class NetworkadapterProviderModule extends org.opendaylight.yang.gen.v1.brocade.training.networkadapter.provider.impl.rev140523.AbstractNetworkadapterProviderModule {
    public NetworkadapterProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NetworkadapterProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.brocade.training.networkadapter.provider.impl.rev140523.NetworkadapterProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
// add custom validation form module attributes here.
    }

    @Override


    public java.lang.AutoCloseable createInstance() {

        final NetworkadapterProvider provider = new NetworkadapterProvider(getRpcRegistryDependency());

        return new AutoCloseable() {
            public void close() throws Exception {
                provider.close();
            }
        };
    }


}
