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

package midcontainers.distsession;

import midcontainers.Binding;
import midcontainers.components.Counter;
//import midcontainers.components.InSessionCounter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DistributedSessionContainerTest {

    private static final int SLEEP_DELAY = 250;
//
//    @Test
//    public void check_local_session() {
//        DistributedSessionContainer container = new DistributedSessionContainer("228.5.6.7", 8666);
//        container.start();
//
//        Session session = container.obtainReference(Session.class);
//        assertThat(session.get("foo"), nullValue());
//        session.set("foo", "bar");
//        assertThat((String) session.get("foo"), is("bar"));
//        session.delete("foo");
//        assertThat(session.get("foo"), nullValue());
//
//        container.stop();
//    }
//
//    @Test
//    public void check_distributed_session() throws InterruptedException {
//        DistributedSessionContainer container1 = new DistributedSessionContainer("228.5.6.7", 8666);
//        DistributedSessionContainer container2 = new DistributedSessionContainer("228.5.6.7", 8666);
//        container1.start();
//        container2.start();
//
//        Session session1 = container1.obtainReference(Session.class);
//        Session session2 = container2.obtainReference(Session.class);
//
//        session1.set("hello", "world");
//        session1.set("foo", "bar");
//
//        Thread.sleep(SLEEP_DELAY);
//
//        assertThat((String) session2.get("hello"), is("world"));
//        assertThat((String) session2.get("foo"), is("bar"));
//
//        session2.delete("foo");
//
//        Thread.sleep(SLEEP_DELAY);
//
//        assertThat((String) session1.get("foo"), nullValue());
//
//        container1.stop();
//        container2.stop();
//    }
//
//    @Test
//    public void check_assembly() throws InterruptedException {
//        DistributedSessionContainer distContainer1 = new DistributedSessionContainer("228.5.6.7", 8666);
//        DistributedSessionContainer distContainer2 = new DistributedSessionContainer("228.5.6.7", 8666);
//
//        distContainer1.start();
//        distContainer2.start();
//
//        distContainer1
//                .define("counter.key", "my:counter")
//                .declare(new Binding(Counter.class, InSessionCounter.class, null, Binding.Policy.NEW));
//
//        Counter c1 = distContainer1.obtainReference(Counter.class);
//        Counter c2 = distContainer1.obtainReference(Counter.class);
//        Session session2 = distContainer2.obtainReference(Session.class);
//
//        assertThat(c1, not(sameInstance(c2)));
//
//        c1.increment();
//        c2.increment();
//
//        Thread.sleep(SLEEP_DELAY);
//
//        assertThat(c1.get(), is(2));
//        assertThat(c2.get(), is(2));
//        assertThat((Integer) session2.get("my:counter"), is(2));
//
//        distContainer1.stop();
//        distContainer2.stop();
//    }
//
//    @Test
//    public void check_join_group_sync() throws InterruptedException {
//        DistributedSessionContainer container1 = new DistributedSessionContainer("228.5.6.7", 8666);
//        DistributedSessionContainer container2 = new DistributedSessionContainer("228.5.6.7", 8666);
//        container1.start();
//        container2.start();
//
//        Session session1 = container1.obtainReference(Session.class);
//        Session session2 = container2.obtainReference(Session.class);
//
//        session1.set("hello", "world");
//        session1.set("foo", "bar");
//
//        Thread.sleep(SLEEP_DELAY);
//
//        assertThat((String) session2.get("hello"), is("world"));
//        assertThat((String) session2.get("foo"), is("bar"));
//
//        DistributedSessionContainer container3 = new DistributedSessionContainer("228.5.6.7", 8666);
//        container3.start();
//
//        Thread.sleep(SLEEP_DELAY);
//
//        Session session3 = container3.obtainReference(Session.class);
//
//        assertThat((String) session3.get("hello"), is("world"));
//        assertThat((String) session3.get("foo"), is("bar"));
//
//        session3.delete("foo");
//
//        Thread.sleep(SLEEP_DELAY);
//
//        assertThat(session1.get("foo"), nullValue());
//        assertThat(session2.get("foo"), nullValue());
//        assertThat(session3.get("foo"), nullValue());
//
//        container1.stop();
//        container2.stop();
//        container3.stop();
//    }
}