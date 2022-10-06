package org.apache.camel.example.amqp;

import java.util.Scanner;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.builder.RouteBuilder;

public final class CamelAMQPExample {
    public static void main(String[] args) throws Exception {
        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addComponent("esbBroker", CamelConfiguration.esbBroker("esbBroker"));
        camelContext.addRoutes(createBasicRoute());

        // start is not blocking
        camelContext.start();

        System.out.println("Press a key to exit");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
        System.out.println("Exiting");

        camelContext.close();
        System.exit(0);
    }

    public static RouteBuilder createBasicRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                // Consuming from AMQP queue
                from("esbBroker:queue:perftest")
                .bean("org.apache.camel.example.amqp.BusinessLogic", "printPayload")
                .end();
            }
        };
    }
}
