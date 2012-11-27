package midcontainers.remote;

import midcontainers.Binding;
import midcontainers.components.*;
import midcontainers.local.LocalContainer;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static midcontainers.Binding.Policy.NEW;
import static midcontainers.Binding.Policy.SINGLETON;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RemoteContainerTest {

    @Test
    public void smoke_test() {
        RemoteContainerServer server = new RemoteContainerServer(1981);
        server.start();
        server.stop();
    }

    @Test
    public void check_unsupported_operations() {
        RemoteContainerServer server = new RemoteContainerServer(1982);
        server.start();
        RemoteContainerClient client = new RemoteContainerClient("127.0.0.1", 1982);

        try {
            client.declare(new Binding(List.class, LinkedList.class, null, NEW));
            fail("The operation should not be supported");
        } catch (UnsupportedOperationException ignored) {
        }

        try {
            client.define("foo", "bar");
            fail("The operation should not be supported");
        } catch (UnsupportedOperationException ignored) {
        }

        try {
            client.delegateTo(new LocalContainer());
            fail("The operation should not be supported");
        } catch (UnsupportedOperationException ignored) {
        }

        server.stop();
    }

    @Test
    public void check_query_operations() {
        RemoteContainerServer server = new RemoteContainerServer(1983);
        server
                .define("hello", "world")
                .declare(new Binding(List.class, LinkedList.class, null, NEW));

        server.start();
        RemoteContainerClient client = new RemoteContainerClient("127.0.0.1", 1983);

        boolean query = client.hasValueDefinedFor("hello");
        assertThat(query, is(true));
        query = client.hasValueDefinedFor("foo");
        assertThat(query, is(false));

        query = client.hasReferenceDeclaredFor(List.class, null);
        assertThat(query, is(true));
        query = client.hasReferenceDeclaredFor(Set.class, null);
        assertThat(query, is(false));

        assertThat((String) client.definitionValue("hello"), is("world"));

        server.stop();
    }

    @Test
    public void check_remote_operations() {
        RemoteContainerServer server = new RemoteContainerServer(1984);
        server
                .declare(new Binding(Echo.class, SomeEcho.class, null, NEW))
                .declare(new Binding(Echo.class, SomeEcho.class, "shared", SINGLETON))
                .define("prefix", "[ ")
                .define("suffix", " ]");
        server.start();

        RemoteContainerClient client = new RemoteContainerClient("127.0.0.1", 1984);

        Echo echo = client.obtainReference(Echo.class);
        assertThat(echo, notNullValue());
        assertThat(echo, not(instanceOf(SomeEcho.class)));

        assertThat(echo.echo("hello"), is("[ hello ]"));

        Echo echoShared1 = client.obtainReference(Echo.class, "shared");
        Echo echoShared2 = client.obtainReference(Echo.class, "shared");
        assertThat(echoShared1, not(sameInstance(echoShared2)));
        assertThat(echoShared1.echo("hello"), is("[ hello ]"));
        assertThat(echoShared2.echo("hello"), is("[ hello ]"));

        server.declare(new Binding(Counter.class, SomeCounter.class, null, SINGLETON));
        server.declare(new Binding(Counter.class, SomeCounter.class, "private", NEW));

        Counter c1 = client.obtainReference(Counter.class);
        Counter c2 = client.obtainReference(Counter.class);
        c1.increment();
        c1.increment();
        c2.increment();
        assertThat(c1.get(), is(3));
        assertThat(c2.get(), is(3));

        c1 = client.obtainReference(Counter.class, "private");
        c2 = client.obtainReference(Counter.class, "private");
        c1.increment();
        c1.increment();
        c2.increment();
        assertThat(c1.get(), is(2));
        assertThat(c2.get(), is(1));

        server.stop();
    }

    @Test
    public void check_delegation_to_remote() {
        RemoteContainerServer server = new RemoteContainerServer(1985);
        server
                .declare(new Binding(Counter.class, SomeCounter.class, "shared", SINGLETON))
                .define("prefix", "[ ")
                .define("suffix", " ]");
        server.start();

        LocalContainer mainContainer = new LocalContainer();
        mainContainer
                .declare(new Binding(EchoCounterClient.class, SomeEchoCounterClient.class, null, NEW))
                .declare(new Binding(Echo.class, SomeEcho.class, null, NEW))
                .delegateTo(new RemoteContainerClient("127.0.0.1", 1985));

        EchoCounterClient client = mainContainer.obtainReference(EchoCounterClient.class);
        assertThat(client, notNullValue());
        assertThat(client.echoNextIncrement(), is("[ 1 ]"));
        assertThat(client.echoNextIncrement(), is("[ 2 ]"));
        assertThat(client.echoNextIncrement(), is("[ 3 ]"));

        server.stop();
    }
}