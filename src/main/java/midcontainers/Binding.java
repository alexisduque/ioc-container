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
 * Denotes a container binding between an interface, an implementation, a qualifier and an allocation policy.
 *
 * An interface can be bound to the several implementations, including the same several times, as long as
 * different qualifiers are being used. The qualifier acts as a discriminant and can be <code>null</code>.
 */
public final class Binding {

    /**
     * Container instances allocation policies.
     * <ul>
     * <li><code>SINGLETON</code>: a single instance is allocated on a per-binding key basis
     * (interface + qualifier)</li>
     * <li><code>NEW</code>: a new instance is created for every injection point.</li>
     * </ul>
     */
    public static enum Policy {
        SINGLETON, NEW
    }

    /**
     * A binding key comprising an interface class and a qualifier.
     */
    public static final class Key {
        private final Class<?> interfaceClass;
        private final String qualifier;

        public Key(Class<?> interfaceClass, String qualifier) {
            this.interfaceClass = interfaceClass;
            this.qualifier = qualifier;
        }

        public Class<?> getInterfaceClass() {
            return interfaceClass;
        }

        public String getQualifier() {
            return qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return interfaceClass.equals(key.interfaceClass) && !(qualifier != null ? !qualifier.equals(key.qualifier) : key.qualifier != null);

        }

        @Override
        public int hashCode() {
            int result = interfaceClass.hashCode();
            result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "interfaceClass=" + interfaceClass +
                    ", qualifier='" + qualifier + '\'' +
                    '}';
        }
    }

    private final Class<?> interfaceClass;
    private final Class<?> implementationClass;
    private final String qualifier;
    private final Policy policy;

    public Binding(Class<?> interfaceClass, Class<?> implementationClass, String qualifier, Policy policy) {
        this.interfaceClass = interfaceClass;
        this.implementationClass = implementationClass;
        this.qualifier = qualifier;
        this.policy = policy;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Class<?> getImplementationClass() {
        return implementationClass;
    }

    public String getQualifier() {
        return qualifier;
    }

    public Policy getPolicy() {
        return policy;
    }

    public Key getKey() {
        return new Key(interfaceClass, qualifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Binding binding = (Binding) o;

        if (!implementationClass.equals(binding.implementationClass)) return false;
        if (!interfaceClass.equals(binding.interfaceClass)) return false;
        return policy == binding.policy && !(qualifier != null ? !qualifier.equals(binding.qualifier) : binding.qualifier != null);

    }

    @Override
    public int hashCode() {
        int result = interfaceClass.hashCode();
        result = 31 * result + implementationClass.hashCode();
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + policy.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Binding{" +
                "interfaceClass=" + interfaceClass +
                ", implementationClass=" + implementationClass +
                ", qualifier='" + qualifier + '\'' +
                ", policy=" + policy +
                '}';
    }
}
