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

import java.io.Serializable;

/**
 * Interface to a distributed session.
 * <p/>
 * A distributed session is a special component that is available from
 * instances of <code>DistributedSessionContainer</code>. As such, it can
 * be injected into components.
 * <p/>
 * The container should replicate updates to a session store to the other
 * distributed instances. Session values must obviously be serializable.
 *
 * @author Julien Ponge
 * @see java.io.Serializable
 * @see midcontainers.distsession.DistributedSessionContainer
 */
public interface Session {
    
    /**
     * Fetch a session value.
     *
     * @param key key of the value
     * @return the value, or <code>null</code> if none was found
     */
    Serializable get(String key);

    /**
     * Remove a value
     *
     * @param key the key of the value to be removed
     */
    void delete(String key);

    /**
     * Define or update a session value
     *
     * @param key   the value key
     * @param value the value
     */
    void set(String key, Serializable value);
}