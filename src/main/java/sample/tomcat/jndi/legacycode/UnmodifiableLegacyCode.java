package sample.tomcat.jndi.legacycode;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Let's assume this is a code in a provided library that is really needed to be reused
 * and can't be reimplemented at the moment. We might not have access to sources
 * or can't change it for any other reasons.
 */
public class UnmodifiableLegacyCode {
    private static String JNDI_QUEUE_NAME = "java:comp/env/jms/queue/MyQueue";

    /**
     * This method will do successful JNDI lookup only if current thread or classloader is
     * bound to Tomcat's App context and its JNDI.
     */
    public static Queue getMessageQueue() throws NamingException {
        return (Queue) new InitialContext().lookup(JNDI_QUEUE_NAME);
    }

    public static Object getMessageQueueInAnotherThread() {
        final CompletableFuture<Object> res = new CompletableFuture<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    res.complete(new InitialContext().lookup(JNDI_QUEUE_NAME));
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();

        try {
            return res.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
