package org.qualipso.factory.client.test.ws;

import static org.junit.Assert.fail;

import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.ws.core.StubExt;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.qualipso.factory.client.test.AllTests;
import org.qualipso.factory.client.ws.Bootstrap;
import org.qualipso.factory.client.ws.BootstrapServiceException_Exception;
import org.qualipso.factory.client.ws.Bootstrap_Service;
import org.qualipso.factory.client.ws.Browser;
import org.qualipso.factory.client.ws.Browser_Service;
import org.qualipso.factory.client.ws.Core;
import org.qualipso.factory.client.ws.CoreServiceException_Exception;
import org.qualipso.factory.client.ws.Core_Service;

/**
 * @author Jerome Blanchard (jayblanc@gmail.com)
 * @date 26 august 2009
 */
public class BrowserServiceWSTest {
	
	private static Log logger = LogFactory.getLog(BrowserServiceWSTest.class);
	
	private Browser browser;
	private Core core;
	
	@BeforeClass
	public static void init() {
		try {
			Bootstrap port = new Bootstrap_Service().getBootstrapServiceBeanPort(); 
			((StubExt) port).setConfigName("Standard WSSecurity Client");
			port.bootstrap();
		} catch (BootstrapServiceException_Exception e) {
			logger.error("unable to bootstrap factory", e);
		}
	}
	
	public BrowserServiceWSTest() {
		browser = new Browser_Service().getBrowserServiceBeanPort();
        core = new Core_Service().getCoreServiceBeanPort();
	}
	
	@Before
	public void setup() throws CoreServiceException_Exception {
		((StubExt) core).setConfigName("Standard WSSecurity Client");
        Map<String, Object> reqContext = ((BindingProvider) core).getRequestContext();
        reqContext.put(StubExt.PROPERTY_AUTH_TYPE, StubExt.PROPERTY_AUTH_TYPE_WSSE);
        reqContext.put(BindingProvider.USERNAME_PROPERTY, "root");
        reqContext.put(BindingProvider.PASSWORD_PROPERTY, AllTests.ROOT_ACCOUNT_PASS);
        
		core.createFolder("/bingo", "Bingo", "BingoBingo");
	}
	
	@After
	public void teardown() throws CoreServiceException_Exception {
		((StubExt) core).setConfigName("Standard WSSecurity Client");
        Map<String, Object> reqContext = ((BindingProvider) core).getRequestContext();
        reqContext.put(StubExt.PROPERTY_AUTH_TYPE, StubExt.PROPERTY_AUTH_TYPE_WSSE);
        reqContext.put(BindingProvider.USERNAME_PROPERTY, "root");
        reqContext.put(BindingProvider.PASSWORD_PROPERTY, AllTests.ROOT_ACCOUNT_PASS);
        
		core.deleteFolder("/bingo");
	}
	
	
	@Test
    public void testFindResource() {
		try {
			((StubExt) browser).setConfigName("Standard WSSecurity Client");
            Map<String, Object> reqContext = ((BindingProvider) browser).getRequestContext();
            reqContext.put(StubExt.PROPERTY_AUTH_TYPE, StubExt.PROPERTY_AUTH_TYPE_WSSE);
            reqContext.put(BindingProvider.USERNAME_PROPERTY, "root");
            reqContext.put(BindingProvider.PASSWORD_PROPERTY, AllTests.ROOT_ACCOUNT_PASS);
            
            browser.findResource("/bingo");
            
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.error(e);
            fail(e.getMessage());
        }
	}

}
