
package noumena.payment.gash.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

import noumena.payment.gash.GASHCharge;
import noumena.payment.gash.GASHParams;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "checkorder", targetNamespace = "http://egsys.org/", wsdlLocation = "http://stage-api.eg.gashplus.com/CP_Module/checkorder.asmx?wsdl")
public class Checkorder
    extends Service
{

    private final static URL CHECKORDER_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(Checkorder.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = Checkorder.class.getResource(".");
            String checkorderwsdl = "";
        	if (GASHCharge.isTestmode() == true)
        	{
        		checkorderwsdl = GASHParams.GASH_CHECK_URL_TEST;
        	}
        	else
        	{
        		checkorderwsdl = GASHParams.GASH_CHECK_URL_RELEASE;
			}
            url = new URL(baseUrl, checkorderwsdl + "?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://stage-api.eg.gashplus.com/CP_Module/checkorder.asmx?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        CHECKORDER_WSDL_LOCATION = url;
    }

    public Checkorder(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public Checkorder() {
        super(CHECKORDER_WSDL_LOCATION, new QName("http://egsys.org/", "checkorder"));
    }

    /**
     * 
     * @return
     *     returns CheckorderSoap
     */
    @WebEndpoint(name = "checkorderSoap")
    public CheckorderSoap getCheckorderSoap() {
        return super.getPort(new QName("http://egsys.org/", "checkorderSoap"), CheckorderSoap.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CheckorderSoap
     */
    @WebEndpoint(name = "checkorderSoap")
    public CheckorderSoap getCheckorderSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://egsys.org/", "checkorderSoap"), CheckorderSoap.class, features);
    }

}
