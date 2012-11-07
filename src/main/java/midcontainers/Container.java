/*
 * Copyright (C) 2011 Julien Ponge, Institut National des Sciences Appliquées de Lyon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package midcontainers;

/**
 * Definition of a container interface.
 * <p/>
 * A container performs components assembly, also called "dependency injection".
 * It allows component bindings to be declared, with each binding consisting of
 * a key based on the interface + a string qualifier, an implementation and an
 * instance allocation policy.
 * <p/>
 * A container also allows the definition of arbitrary objects associated with
 * a name as a string.
 * <p/>
 * Component instantiation and dependency injection is performed through their
 * constructors. Constructors are iterated until the first one can be satisfied
 * with respect to its dependencies. You must thus be cautious regarding the
 * presence of implementations having several constructors, and especially
 * those having default / no-argument constructors.
 * <p/>
 * The special <code>@Named</code> annotation allows constructor parameters
 * to be given a qualifier. This is optional in the case of components, and
 * mandatory for defined values.
 * <p/>
 * Each constructor argument is first tried against a container-defined binding.
 * If none is found and the <code>@Named</code> annotation is present, a value
 * is looked up. If none is found, or the <code>@Named</code> annotation is
 * missing, the constructor is discarded. The process shall continue until no
 * suitable constructor exists to make an assembly.
 * <p/>
 * The following is an example of a component implementation definition with
 * the injection of an unqualified component and a defined value:
 * <p/>
 * <pre class="prettyprint">
 * public class SomeEchoClient implements EchoClient {
 *     private final Echo echo;
 *     private final String message;
 *
 *     public SomeEchoClient(Echo echo, @Named("message") String message) {
 *         this.echo = echo;
 *         this.message = message;
 *     }
 *
 *     public String run() {
 *         return echo.echo(message);
 *     }
 * }
 * </pre>
 * <p/>
 * A sample usage would be similar to the following unit-test excerpt:
 * <p/>
 * <pre class="prettyprint">
 * Container container = new LocalContainer();
 * container
 *     .declare(new Binding(Echo.class, SomeEcho.class, null, NEW))
 *     .declare(new Binding(EchoClient.class, SomeEchoClient.class, null, NEW))
 *     .define("prefix", "[ ")
 *     .define("suffix", " ]")
 *     .define("message", "hello");
 *
 * EchoClient client = container.obtainReference(EchoClient.class);
 * assertThat(client.run(), is("[ hello ]"));
 * </pre>
 * <p/>
 * Containers may also support delegation to other containers. When a
 * component or defined value cannot be obtained from a container,
 * it can forward the lookup to its delegates. The delegation mechanism
 * is read-only, that is, a container cannot declare bindings and values
 * to a delegate.
 *
 * @see midcontainers.Binding
 * @see midcontainers.Named
 * @author Julien Ponge
 */
public interface Container {

    // Local container .............................................................................................. //

    /**
     * Declare a component binding.
     *
     * @param binding the binding definition
     * @return this container
     */
    Container declare(Binding binding);

    /**
     * Define a value.
     *
     * @param name  the value name
     * @param value the value
     * @return this container
     */
    Container define(String name, Object value);

    /**
     * Obtain a reference to a component based on an interface and a <code>null</code> qualifier.
     * This is the same as <code>obtainReference(interfaceClass, null)</code>.
     *
     * @param interfaceClass the component interface class
     * @param <T>            the type of the component interface
     * @return the component implementation
     */
    <T> T obtainReference(Class<T> interfaceClass);

    /**
     * Obtain a reference to a component based on a full binding.
     *
     * @param interfaceClass the component interface class
     * @param qualifier      the binding qualifier, which may be <code>null</code>
     * @param <T>            the type of the component interface
     * @return the component implementation
     */
    <T> T obtainReference(Class<T> interfaceClass, String qualifier);

    /**
     * Obtain a defined value.
     *
     * @param name the value name
     * @return the value
     */
    Object definitionValue(String name);

    // Appears with delegation support .............................................................................. //

    /**
     * Checks whether a component is available locally for an interface with a <code>null</code> qualifier.
     *
     * @param interfaceClass the interface class
     * @return <code>true</code> if a component is bound, <code>false</code> otherwise
     */
    boolean hasReferenceDeclaredFor(Class<?> interfaceClass);

    /**
     * Checks whether a component is available locally for an interface with a qualifier.
     *
     * @param interfaceClass the interface class
     * @param qualifier      the qualifier
     * @return <code>true</code> if a component is bound, <code>false</code> otherwise
     */
    boolean hasReferenceDeclaredFor(Class<?> interfaceClass, String qualifier);

    /**
     * Checks whether a value is available locally.
     *
     * @param name the value name
     * @return <code>true</code> if a value is defined, <code>false</code> otherwise
     */
    boolean hasValueDefinedFor(String name);

    /**
     * Add a delegation link from this container to another one.
     *
     * @param container the delegate container
     * @return this container
     */
    Container delegateTo(Container container);
}
