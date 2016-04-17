package eu.codesketch.spring.cloud.ribbon;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.PortType;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

@RunWith(MockitoJUnitRunner.class)
public class IpAddressLoadBalancerClientTest {

    @Mock
    private SpringClientFactory springClientFactory;
    @Mock
    private ILoadBalancer loadBalancer;

    private IpAddressLoadBalancerClient testObj;

    @Mock
    private DiscoveryEnabledServer discoveryEnabledServer;
    @Mock
    private InstanceInfo instanceInfo;
    @Mock
    private Server server;

    @Before
    public void before() {
        testObj = new IpAddressLoadBalancerClient(springClientFactory);
    }

    @Test
    public void testChooseString_DiscoveryEnabledServer() {
        String serviceId = "a-serivice-id";
        String ipAddress = "192.168.1.10";
        Integer port = 8080;
        when(springClientFactory.getLoadBalancer(serviceId)).thenReturn(loadBalancer);
        when(loadBalancer.chooseServer(Mockito.anyString())).thenReturn(discoveryEnabledServer);
        when(discoveryEnabledServer.getInstanceInfo()).thenReturn(instanceInfo);
        when(instanceInfo.getIPAddr()).thenReturn(ipAddress);
        when(instanceInfo.isPortEnabled(PortType.SECURE)).thenReturn(false);
        when(instanceInfo.getPort()).thenReturn(port);
        // act
        ServiceInstance actual = testObj.choose(serviceId);
        // assert
        assertEquals(ipAddress, actual.getHost());
        assertEquals(port.intValue(), actual.getPort());
    }

    @Test
    public void testChooseString_DiscoveryEnabledServer_securePort() {
        String serviceId = "a-serivice-id";
        String ipAddress = "192.168.1.10";
        Integer port = 8443;
        when(springClientFactory.getLoadBalancer(serviceId)).thenReturn(loadBalancer);
        when(loadBalancer.chooseServer(Mockito.anyString())).thenReturn(discoveryEnabledServer);
        when(discoveryEnabledServer.getInstanceInfo()).thenReturn(instanceInfo);
        when(instanceInfo.getIPAddr()).thenReturn(ipAddress);
        when(instanceInfo.isPortEnabled(PortType.SECURE)).thenReturn(true);
        when(instanceInfo.getSecurePort()).thenReturn(port);
        // act
        ServiceInstance actual = testObj.choose(serviceId);
        // assert
        assertEquals(ipAddress, actual.getHost());
        assertEquals(port.intValue(), actual.getPort());
    }

    @Test
    public void testChooseString_not_DiscoveryEnabledServer() {
        String serviceId = "a-serivice-id";
        String host = "hostname";
        Integer port = 8080;
        when(springClientFactory.getLoadBalancer(serviceId)).thenReturn(loadBalancer);
        when(loadBalancer.chooseServer(Mockito.anyString())).thenReturn(server);
        when(server.getHost()).thenReturn(host);
        when(server.getPort()).thenReturn(port);
        // act
        ServiceInstance actual = testObj.choose(serviceId);
        // assert
        assertEquals(host, actual.getHost());
        assertEquals(port.intValue(), actual.getPort());
    }
}
