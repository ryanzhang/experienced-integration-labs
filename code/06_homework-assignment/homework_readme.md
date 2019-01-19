# Business Scenario 
This project facilitates synchronization of master patient records across different healthcare providers. It is important to have consistent patient information across these multiple providers so that patients may receive consistent care. For that to occur, their personal and medical information needs to be shared. Also, any updates to the patient record need to flow across the providers to maintain accuracy and currency.

It is assumed you already have Java 1.8 and Maven 3+ installed.

## Architecture
3 microservices compose this application. 
* inbound
* xlate
* outbound

Each microservice can be run seperatedly.

## inbound microservice
It runs a REST server and sends the message to a broker

## xlate
It translates the message from the Person format to the NextGate format

## Outbound microservice
It reads the final message from the broker and sends it to the NextGate sever

## How to Run
### 1) Start AMQ7 or Artemis

Make sure that the following queue are created in the etc/broker.xml

         <address name="q.empi.deim.in">
            <anycast>
               <queue name="q.empi.deim.in" />
            </anycast>
         </address>
         <address name="q.empi.nextgate.out">
            <anycast>
               <queue name="q.empi.nextgate.out" />
            </anycast>
         </address>
         <address name="q.empi.nextgate.dlq">
            <anycast>
               <queue name="q.empi.nextgate.dlq" />
            </anycast>
         </address>

Make sure that amqp protocol and 5276 port is up.

 <!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
         <acceptor name="amqp">tcp://0.0.0.0:5672?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300</acceptor>

Start AMQ7

    ./artemis-service run

Check

    /lab/amq/brokers/amq7/bin >>>sudo netstat -tupln|grep 5672
    tcp6       0      0 :::5672                 :::*                    LISTEN      15629/java

### 2) Run the inbound service
    
        mvn spring-boot:run

In a new terminal, test a rest post method with curl command:
        
    curl -k http://127.0.0.1:8080/rest/service/demos -X POST  -H "Content-Type: text/xml" --data-binary "@/workspace/ai_advanced_labs/experienced-integration-labs/code/06_homework-assignment/core/inbound/src/test/data/PatientDemographics.xml" 
    
It should return if no error return:

    <reply id="p:localid">Done!</reply>

In the inbound service terminal:

    07:53:43.716 [main] INFO  o.a.c.m.ManagedManagementStrategy - JMX is enabled
    07:53:43.953 [main] INFO  o.a.camel.spring.SpringCamelContext - StreamCaching is not in use. If using streams then its recommended to enable stream caching. See more details at http://camel.apache.org/stream-caching.html
    07:53:44.140 [main] INFO  o.a.camel.spring.SpringCamelContext - Route: handleRest started and consuming from: direct://inbox
    07:53:44.141 [main] INFO  o.a.camel.spring.SpringCamelContext - Route: addPerson started and consuming from: direct://deim_internal
    07:53:44.143 [main] INFO  o.a.camel.spring.SpringCamelContext - Route: 83790b76-0372-420b-974f-125b31cb82bf started and consuming from: servlet:/service/demos?httpMethodRestrict=POST
    07:53:44.143 [main] INFO  o.a.camel.spring.SpringCamelContext - Total 3 routes, of which 3 are started
    07:53:44.143 [main] INFO  o.a.camel.spring.SpringCamelContext - Apache Camel 2.21.0.fuse-720050-redhat-00001 (CamelContext: MyCamel) started in 0.428 seconds
    07:53:44.180 [main] INFO  o.s.b.c.e.u.UndertowEmbeddedServletContainer - Undertow started on port(s) 8081 (http)
    07:53:44.184 [main] INFO  o.s.c.s.DefaultLifecycleProcessor - Starting beans in phase 0
    07:53:44.191 [main] INFO  o.s.b.a.e.jmx.EndpointMBeanExporter - Located managed bean 'healthEndpoint': registering with JMX server as MBean [org.springframework.boot:type=Endpoint,name=healthEndpoint]
    07:53:44.234 [main] INFO  o.s.b.c.e.u.UndertowEmbeddedServletContainer - Undertow started on port(s) 8080 (http)
    07:53:44.237 [main] INFO  c.r.usecase.springboot.Application - Started Application in 5.705 seconds (JVM running for 12.462)
    07:54:06.283 [XNIO-3 task-1] INFO  o.a.c.c.s.CamelHttpTransportServlet - Initialized CamelHttpTransportServlet[name=CamelServlet, contextPath=]
    07:54:06.325 [XNIO-3 task-1] INFO  handleRest - Xml received
    07:54:06.334 [XNIO-3 task-1] INFO  o.a.c.converter.jaxp.StaxConverter - Created XMLInputFactory: com.sun.xml.internal.stream.XMLInputFactoryImpl@394197e0. DOMSource/DOMResult may have issues with com.sun.xml.internal.stream.XMLInputFactoryImpl@394197e0. We suggest using Woodstox.
    07:54:06.435 [XNIO-3 task-1] INFO  addPerson - Start to set customerId headers=p:localid
    07:54:06.436 [XNIO-3 task-1] INFO  addPerson - Marshalling object to xml
    07:54:06.451 [XNIO-3 task-1] INFO  addPerson - Add to amq queue
    07:54:06.785 [AmqpProvider :(1):[amqp://localhost:5672]] INFO  o.a.q.jms.sasl.SaslMechanismFinder - Best match for SASL auth was: SASL-PLAIN
    07:54:06.829 [AmqpProvider :(1):[amqp://localhost:5672]] INFO  org.apache.qpid.jms.JmsConnection - Connection ID:2904d1a3-92f1-4b71-9bf1-8b5503dec2d4:1 connected to remote Broker: amqp://localhost:5672
    07:54:06.956 [XNIO-3 task-1] INFO  addPerson - Set body

As you can see, the xml has been proceeded by rest and return the Done acknowledge.

### 3) Run the xlate service (In a seperate terminal)
mvn spring-boot:run

### 4) Run the outbound service (In a seperate terminal)
mvn camel:run

### 5) Final Test (In a seperate terminal):
curl -X http://localhost:9098/rest/demos -@test/data/PatientDemographics.xml

### 6) It should return the result if there is no error happen:


~~~
mvn clean install
~~~
This will build the pom file and install it in your local .m2 directory.


