package org.qualipso.factory.test.sessionbean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.qualipso.factory.binding.BindingServiceException;
import org.qualipso.factory.binding.entity.Node;
import org.qualipso.factory.eventqueue.EventQueueService;
import org.qualipso.factory.eventqueue.EventQueueServiceException;
import org.qualipso.factory.eventqueue.entity.Event;
import org.qualipso.factory.membership.MembershipServiceException;
import org.qualipso.factory.membership.entity.Profile;
import org.qualipso.factory.notification.NotificationService;
import org.qualipso.factory.notification.NotificationServiceBean;
import org.qualipso.factory.notification.NotificationServiceException;
import org.qualipso.factory.notification.entity.Rule;
import org.qualipso.factory.security.pap.PAPServiceException;
import org.qualipso.factory.security.pep.PEPServiceException;

import com.bm.testsuite.BaseSessionBeanFixture;

/**
 * @author Yiqing LI
 * @author Marlène HANTZ
 * @author Nicolas HENRY
 * @author Philippe SCHMUCKER
 */
public class NotificationServiceTest extends BaseSessionBeanFixture<NotificationServiceBean> {
    private static Log logger = LogFactory.getLog(NotificationServiceTest.class);

    @SuppressWarnings("unchecked")
    private static final Class[] usedBeans = { Rule.class, Node.class, Profile.class };

    private Mockery mockery;
    private Connection connection;
    private ConnectionFactory connectionfactory;
    private Session session;
    private ObjectMessage om;
    private MessageProducer mp;
    private Queue queue;
    private EventQueueService eventqueueservice;

    public static int auto_ack = Session.AUTO_ACKNOWLEDGE;

    public NotificationServiceTest() {
        super(NotificationServiceBean.class, usedBeans);
    }

    public void setUp() throws Exception {
        super.setUp();
        logger.debug("Session beans");
        mockery = new Mockery();
        connection = mockery.mock(Connection.class);
        connectionfactory = mockery.mock(ConnectionFactory.class);
        session = mockery.mock(Session.class);
        om = mockery.mock(ObjectMessage.class);
        mp = mockery.mock(MessageProducer.class);
        eventqueueservice = mockery.mock(EventQueueService.class);

        NotificationServiceBean.setConnectionFactory(connectionfactory);
        NotificationServiceBean.setQueue(queue);
    }

    public void testList() throws NotificationServiceException {
        logger.debug("testing testList(...)");
        getBeanToTest().getEntityManager().getTransaction().begin();
        NotificationService service = getBeanToTest();
        Rule[] tab2 = service.list();
        assertEquals(tab2.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");

        tab2 = service.list();
        assertEquals(tab2.length, 1);
        assertEquals(tab2[0].getSubjectre(), "subjectre");
        assertEquals(tab2[0].getObjectre(), "objectre");
        assertEquals(tab2[0].getTargetre(), "targetre");
        assertEquals(tab2[0].getQueuePath(), "/li");

        service.unregister("subjectre", "objectre", "targetre", "/li");

        tab2 = service.list();
        assertEquals(tab2.length, 0);
        mockery.assertIsSatisfied();
    }

    public void testRegisterNullParameter() throws NotificationServiceException {
        logger.debug("testing testRegisterNullParameter(...)");
        NotificationService service = getBeanToTest();
        try {
            service.register(null, "objectre", "targetre", "/li");
            service.register("subjectre", null, "targetre", "/li");
            service.register("subjectre", "objectre", null, "/li");
            service.register("subjectre", "objectre", "targetre", null);
        } catch (NotificationServiceException e) {

        }
        Rule[] tab2 = service.list();
        assertEquals(tab2.length, 0);
        mockery.assertIsSatisfied();
    }

    public void testUnregisterNullParameter() throws NotificationServiceException {
        logger.debug("testing testUnregisterNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.unregister(null, "objectre", "targetre", "/li");
            service.unregister("subjectre", null, "targetre", "/li");
            service.unregister("subjectre", "objectre", null, "/li");
            service.unregister("subjectre", "objectre", "targetre", null);
        } catch (NotificationServiceException e) {

        }
        Rule[] tab = service.list();
        assertEquals(tab.length, 1);
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }

    public void testthrowEvent() throws NotificationServiceException, JMSException, EventQueueServiceException, MembershipServiceException,
            PEPServiceException, BindingServiceException, PAPServiceException {
        logger.debug("testing testunregister(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");

        // créer une queue (mock) (penser à le faire pour les autres tests)
        mockery.checking(new Expectations() {
            {
                oneOf(eventqueueservice).createEventQueue(with(any(String.class)));

            }
        });
        eventqueueservice.createEventQueue("/rules/li");

        mockery.checking(new Expectations() {
            {
                oneOf(connectionfactory).createConnection();
                will(returnValue(connection));
                oneOf(connection).createSession(with(equal(false)), with(equal(auto_ack)));
                will(returnValue(session));
                oneOf(session).createObjectMessage();
                will(returnValue(om));
                oneOf(om).setObject(with(any(Event.class)));
                oneOf(session).createProducer(with(any(Queue.class)));
                will(returnValue(mp));
                oneOf(mp).send(om);
                oneOf(mp).close();
                oneOf(session).close();
                oneOf(connection).close();
            }
        });
        Event e = new Event("fromRessource", "ressourceType", "eventType", "");
        service.throwEvent(e);
        // penser a faire un event qui matche la regle de la queue
        final Event[] event = new Event[] { e };
        mockery.checking(new Expectations() {
            {
                oneOf(eventqueueservice).getEvents(with(any(String.class)));
                will(returnValue(event));
            }
        });
        Event[] ev = eventqueueservice.getEvents("/queues/rules/li");
        assertEquals(ev[0].getFromResource(), "fromRessource");
        assertEquals(ev[0].getResourceType(), "ressourceType");
        assertEquals(ev[0].getEventType(), "eventType");
        assertEquals(ev[0].getArgs(), "");
        mockery.assertIsSatisfied();
    }

    public void testlistByRE() throws NotificationServiceException {
        logger.debug("testing testlistByRE(...)");
        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByRE("subjectre", null, "targetre", "queuePath");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "queuePath");
        service.register("subj.*", "objectre", "targetre", "queuePath");
        
        tab = service.listByRE("subjectre", "objectre", "targetre", "queuePath");
        assertEquals(tab.length, 1);
        assertEquals(tab[0].getSubjectre(), "subjectre");
        assertEquals(tab[0].getObjectre(), "objectre");
        assertEquals(tab[0].getTargetre(), "targetre");
        assertEquals(tab[0].getQueuePath(), "queuePath");

        service.unregister("subjectre", "objectre", "targetre", "queuePath");
        
        tab = service.listByRE("subjectre", "objectre", "targetre", "queuePath");
        assertEquals(tab.length, 0);
        service.unregister("subj.*", "objectre", "targetre", "queuePath");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByRENullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByRENullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByRE(null, null, null, null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistBy() throws NotificationServiceException {
        logger.debug("testing testlistBy(...)");
        NotificationService service = getBeanToTest();
        Rule[] tab = service.listBy("subjectre", "objectre", "targetre", "queuePath");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "queuePath");
        service.register("subj.*", ".*tre", "targetre", "queuePath");
        service.register("t.*", "obje.*", "targetre", "queuePath");
        
        tab = service.listBy("subjectre", "objectre", "targetre", "queuePath");
        assertEquals(tab.length, 2);

        service.unregister("subjectre", "objectre", "targetre", "queuePath");
        service.unregister("subj.*", ".*tre", "targetre", "queuePath");
        
        tab = service.listBy("subjectre", "objectre", "targetre", "queuePath");
        assertEquals(tab.length, 0);
        service.unregister("t.*", "obje.*", "targetre", "queuePath");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByNullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByQueueNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listBy(null, null, null, null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByQueue() throws NotificationServiceException {
        logger.debug("testing testlistByQueue(...)");

        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByQueue("/li");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subjectre", "objectre", "targetre", "queuePath");

        tab = service.listByQueue("/li");
        assertEquals(tab.length, 1);
        assertEquals(tab[0].getSubjectre(), "subjectre");
        assertEquals(tab[0].getObjectre(), "objectre");
        assertEquals(tab[0].getTargetre(), "targetre");
        assertEquals(tab[0].getQueuePath(), "/li");

        service.unregister("subjectre", "objectre", "targetre", "/li");

        tab = service.listByQueue("/li");
        assertEquals(tab.length, 0);
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "queuePath");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByQueueNullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByQueueNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByQueue(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByQueueRE() throws NotificationServiceException {
        logger.debug("testing testlistByQueueRE(...)");

        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByQueueRE("/li");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subj.*", ".*tre", "targetre", "/l.*");
        service.register("subj.*", ".*tre", "targetre", "/l");

        tab = service.listByQueueRE("/li");
        assertEquals(tab.length, 1);

        service.unregister("subjectre", "objectre", "targetre", "/li");
        service.unregister("subj.*", ".*tre", "targetre", "/l.*");
        tab = service.listByQueueRE("/li");
        assertEquals(tab.length, 0);
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subj.*", ".*tre", "targetre", "/l");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByQueueRENullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByQueueRENullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByQueueRE(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistBySubjectRE() throws NotificationServiceException {
        logger.debug("testing testlistBySubjectRE(...)");

        NotificationService service = getBeanToTest();
        Rule[] tab = service.listBySubjectRE("subjectre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subject", "objectre", "targetre", "/li");

        tab = service.listBySubjectRE("subjectre");
        assertEquals(tab.length, 1);
        assertEquals(tab[0].getSubjectre(), "subjectre");
        assertEquals(tab[0].getObjectre(), "objectre");
        assertEquals(tab[0].getTargetre(), "targetre");
        assertEquals(tab[0].getQueuePath(), "/li");

        service.unregister("subjectre", "objectre", "targetre", "/li");

        tab = service.listBySubjectRE("subjectre");
        assertEquals(tab.length, 0);
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subject", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistBySubjectRENullParameter() throws NotificationServiceException {
        logger.debug("testing testlistBySubjectRENullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listBySubjectRE(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistBySubject() throws NotificationServiceException {
        logger.debug("testing testlistBySubject(...)");
        
        NotificationService service = getBeanToTest();
        Rule[] tab = service.listBySubject("subjectre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subj.*", ".*tre", "targetre", "/l");
        service.register("t.*", "obje.*", "targetre", "queuePath");
        
        tab = service.listBySubject("subjectre");
        assertEquals(tab.length, 2);

        service.unregister("subjectre", "objectre", "targetre", "/li");
        service.unregister("subj.*", ".*tre", "targetre", "/l");
        tab = service.listBySubject("subjectre");
        assertEquals(tab.length, 0);
        
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("t.*", "obje.*", "targetre", "queuePath");
        mockery.assertIsSatisfied();
    }
    
    public void testlistBySubjectNullParameter() throws NotificationServiceException {
        logger.debug("testing testlistBySubjectNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listBySubject(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByObjectRE() throws NotificationServiceException {
        logger.debug("testing testlistByObjectRE(...)");

        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByObjectRE("objectre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subjectre", "object", "targetre", "/li");

        tab = service.listByObjectRE("objectre");
        assertEquals(tab.length, 1);
        assertEquals(tab[0].getSubjectre(), "subjectre");
        assertEquals(tab[0].getObjectre(), "objectre");
        assertEquals(tab[0].getTargetre(), "targetre");
        assertEquals(tab[0].getQueuePath(), "/li");

        service.unregister("subjectre", "objectre", "targetre", "/li");

        tab = service.listByObjectRE("objectre");
        assertEquals(tab.length, 0);
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "object", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByObjectRENullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByObjectRENullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByObjectRE(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByObject() throws NotificationServiceException {
        logger.debug("testing testlistByObject(...)");
        
        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByObject("objectre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subj.*", ".*tre", "targetre", "/l");
        service.register("t.*", "t.*", "targetre", "/li");
        
        tab = service.listByObject("objectre");
        assertEquals(tab.length, 2);

        service.unregister("subjectre", "objectre", "targetre", "/li");
        service.unregister("subj.*", ".*tre", "targetre", "/l");
        tab = service.listByObject("objectre");
        assertEquals(tab.length, 0);
        
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("t.*", "t.*", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByObjectNullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByObjectNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByObject(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByTargetRE() throws NotificationServiceException {
        logger.debug("testing testlistByTargetRE(...)");

        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByTargetRE("targetre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subjectre", "objectre", "target", "/li");

        tab = service.listByTargetRE("targetre");
        assertEquals(tab.length, 1);
        assertEquals(tab[0].getSubjectre(), "subjectre");
        assertEquals(tab[0].getObjectre(), "objectre");
        assertEquals(tab[0].getTargetre(), "targetre");
        assertEquals(tab[0].getQueuePath(), "/li");

        service.unregister("subjectre", "objectre", "targetre", "/li");
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "target", "/li");
        tab = service.listByTargetRE("targetre");
        assertEquals(tab.length, 0);

        mockery.assertIsSatisfied();
    }
    
    public void testlistByTargetRENullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByTargetRENullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByTargetRE(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByTarget() throws NotificationServiceException {
        logger.debug("testing testlistByTarget(...)");
        
        NotificationService service = getBeanToTest();
        Rule[] tab = service.listByTarget("targetre");
        assertEquals(tab.length, 0);

        service.register("subjectre", "objectre", "targetre", "/li");
        service.register("subj.*", ".*tre", "tar.*", "/l");
        service.register("t.*", "t.*", "re.*", "/li");
        
        tab = service.listByTarget("targetre");
        assertEquals(tab.length, 2);

        service.unregister("subjectre", "objectre", "targetre", "/li");
        service.unregister("subj.*", ".*tre", "tar.*", "/l");
        tab = service.listByTarget("targetre");
        assertEquals(tab.length, 0);
        
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("t.*", "t.*", "re.*", "/li");
        mockery.assertIsSatisfied();
    }
    
    public void testlistByTargetNullParameter() throws NotificationServiceException {
        logger.debug("testing testlistByTargetNullParameter(...)");
        NotificationService service = getBeanToTest();
        service.register("subjectre", "objectre", "targetre", "/li");
        try {
            service.listByTarget(null);
        } catch (NotificationServiceException e) {

        }
        // Pour tester les autres méthode à partir d'une base vide
        service.unregister("subjectre", "objectre", "targetre", "/li");
        mockery.assertIsSatisfied();
    }
}
