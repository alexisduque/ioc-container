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

package midcontainers.local;

import midcontainers.Binding;
import midcontainers.Container;
import midcontainers.ContainerException;
import midcontainers.components.Echo;
import midcontainers.components.EchoClient;
import midcontainers.components.SomeEcho;
import midcontainers.components.SomeEchoClient;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static midcontainers.Binding.Policy.NEW;
import static midcontainers.Binding.Policy.SINGLETON;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LocalContainerTest {

    // Local container .............................................................................................. //

     @Test
     public void check_definition_handling() {
         Container container = new LocalContainer();
         container.define("foo", "bar");
     
         assertThat((String) container.definitionValue("foo"), is("bar"));
         try {
             container.definitionValue("n/a");
             fail("A ContainerException should have be thrown on a missing definition value");
         } catch (ContainerException ignored) {
         }
     }

     @Test
     public void check_bindings() {
         Container container = new LocalContainer();
         container
                 .declare(new Binding(List.class, LinkedList.class, null, NEW))
                 .declare(new Binding(List.class, LinkedList.class, "unique", SINGLETON))
                 .declare(new Binding(Set.class, TreeSet.class, null, SINGLETON));
     
         try {
             container.obtainReference(String.class);
             fail("A ContainerException should have been thrown because there is no binding for String.class");
         } catch (ContainerException ignored) {
         }
     
         List l1 = container.obtainReference(List.class);
         List l2 = container.obtainReference(List.class);
         assertThat(l1, notNullValue());
         assertThat(l1, not(sameInstance(l2)));
     
         l1 = container.obtainReference(List.class, "unique");
         l2 = container.obtainReference(List.class, "unique");
         assertThat(l1, notNullValue());
         assertThat(l2, notNullValue());
         assertThat(l1, sameInstance(l2));
     
         Set s1 = container.obtainReference(Set.class);
         Set s2 = container.obtainReference(Set.class);
         assertThat(s1, notNullValue());
         assertThat(s1, sameInstance(s2));
     }

     @Test
         public void check_injection() {
             Container container = new LocalContainer();
             container
                     .declare(new Binding(Echo.class, SomeEcho.class, null, NEW))
                     .declare(new Binding(EchoClient.class, SomeEchoClient.class, null, NEW))
                     .define("prefix", "[ ")
                     .define("suffix", " ]")
                     .define("message", "hello");
    
             EchoClient client = container.obtainReference(EchoClient.class);
             assertThat(client.run(), is("[ hello ]"));
         }

    // Appears with delegation support .............................................................................. //

    // @Test
    //     public void check_delegation() {
    //         Container mainContainer = new LocalContainer();
    //         mainContainer
    //                 .declare(new Binding(EchoClient.class, SomeEchoClient.class, null, NEW))
    //                 .define("message", "hello");
    // 
    //         Container delegate = new LocalContainer();
    //         delegate
    //                 .declare(new Binding(Echo.class, SomeEcho.class, null, NEW))
    //                 .define("prefix", "[ ")
    //                 .define("suffix", " ]");
    // 
    //         mainContainer.delegateTo(delegate);
    //         assertThat(mainContainer.hasValueDefinedFor("message"), is(true));
    //         assertThat(mainContainer.hasValueDefinedFor("prefix"), is(false));
    //         assertThat(delegate.hasValueDefinedFor("message"), is(false));
    //         assertThat(delegate.hasValueDefinedFor("prefix"), is(true));
    //         assertThat(mainContainer.hasReferenceDeclaredFor(Echo.class), is(false));
    //         assertThat(delegate.hasReferenceDeclaredFor(Echo.class), is(true));
    // 
    //         try {
    //             delegate.obtainReference(EchoClient.class);
    //             fail("The delegate should not have a binding to EchoClient");
    //         } catch (ContainerException ignored) {
    //         }
    // 
    //         EchoClient client = mainContainer.obtainReference(EchoClient.class);
    //         assertThat(client.run(), is("[ hello ]"));
    //     }
}
