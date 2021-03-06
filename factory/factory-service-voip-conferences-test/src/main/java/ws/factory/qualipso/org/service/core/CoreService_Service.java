
package ws.factory.qualipso.org.service.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-b01-
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "CoreService", targetNamespace = "http://org.qualipso.factory.ws/service/core", wsdlLocation = "http://localhost:3000/factory-core/core?wsdl")
public class CoreService_Service
    extends Service
{

    private final static URL CORESERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(ws.factory.qualipso.org.service.core.CoreService_Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = ws.factory.qualipso.org.service.core.CoreService_Service.class.getResource(".");
            url = new URL(baseUrl, "http://localhost:3000/factory-core/core?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://localhost:3000/factory-core/core?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        CORESERVICE_WSDL_LOCATION = url;
    }

    public CoreService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public CoreService_Service() {
        super(CORESERVICE_WSDL_LOCATION, new QName("http://org.qualipso.factory.ws/service/core", "CoreService"));
    }

    /**
     * 
     * @return
     *     returns CoreService
     */
    @WebEndpoint(name = "CoreServicePort")
    public CoreService getCoreServicePort() {
        return super.getPort(new QName("http://org.qualipso.factory.ws/service/core", "CoreServicePort"), CoreService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CoreService
     */
    @WebEndpoint(name = "CoreServicePort")
    public CoreService getCoreServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://org.qualipso.factory.ws/service/core", "CoreServicePort"), CoreService.class, features);
    }

}
