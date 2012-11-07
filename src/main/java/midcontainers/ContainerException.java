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
 * Generic exception for container-related exceptions.
 *
 * It is more than often used to wrap all types of checked exceptions
 * into unchecked exceptions.
 *
 * @author Julien Ponge
 */
public class ContainerException extends RuntimeException {

    public ContainerException() {
    }

    public ContainerException(String s) {
        super(s);
    }

    public ContainerException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ContainerException(Throwable throwable) {
        super(throwable);
    }
}
