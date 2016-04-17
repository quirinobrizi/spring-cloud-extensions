/**
 * 
 */
package eu.codesketch.spring.cloud.ribbon;

import java.net.URI;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.PortType;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

/**
 * A {@link RibbonLoadBalancerClient} extension that builds an IP address aware
 * {@link ServiceInstance}.
 * 
 * @author quirino
 *
 */
public class IpAddressLoadBalancerClient extends RibbonLoadBalancerClient {

    public IpAddressLoadBalancerClient(SpringClientFactory clientFactory) {
        super(clientFactory);
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        Server server = getServer(serviceId);
        if (server == null) {
            return null;
        }
        return new IpAddressServiceInstance(serviceId, server);
    }

    protected class IpAddressServiceInstance implements ServiceInstance {

        private String serviceId;
        private Server server;

        public IpAddressServiceInstance(String serviceId, Server server) {
            this.serviceId = serviceId;
            this.server = server;
        }

        @Override
        public String getServiceId() {
            return this.serviceId;
        }

        @Override
        public String getHost() {
            if (isDiscoveryEnabledServer()) {
                return getInstanceInfo().getIPAddr();
            }
            return this.server.getHost();
        }

        @Override
        public int getPort() {
            if (isDiscoveryEnabledServer()) {
                InstanceInfo instanceInfo = getInstanceInfo();
                return isSecure() ? instanceInfo.getSecurePort() : instanceInfo.getPort();
            }
            return this.server.getPort();
        }

        @Override
        public boolean isSecure() {
            if (isDiscoveryEnabledServer()) {
                return getInstanceInfo().isPortEnabled(PortType.SECURE);
            }
            return false;
        }

        @Override
        public URI getUri() {
            return DefaultServiceInstance.getUri(this);
        }

        private InstanceInfo getInstanceInfo() {
            return ((DiscoveryEnabledServer) this.server).getInstanceInfo();
        }

        private boolean isDiscoveryEnabledServer() {
            return DiscoveryEnabledServer.class.isAssignableFrom(this.server.getClass());
        }

    }
}
