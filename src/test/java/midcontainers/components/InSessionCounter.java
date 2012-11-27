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

package midcontainers.components;

import midcontainers.Named;
import midcontainers.distsession.Session;

public class InSessionCounter implements Counter {

    private final Session session;
    private final String sessionKey;

    public InSessionCounter(Session session, @Named("counter.key") String sessionKey) {
        this.session = session;
        this.sessionKey = sessionKey;
    }

    @Override
    public int get() {
        Integer value = (Integer) session.get(sessionKey);
        if (value == null) {
            session.set(sessionKey, 0);
            return 0;
        } else {
            return value;
        }
    }

    @Override
    public int increment() {
        int value = get() + 1;
        session.set(sessionKey, value);
        return value;
    }
}
