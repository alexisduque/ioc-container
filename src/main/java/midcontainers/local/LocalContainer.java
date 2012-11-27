package midcontainers.local;

import midcontainers.Binding;
import midcontainers.Container;
import midcontainers.ContainerException;
import midcontainers.Named;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Constructor;
import java.lang.annotation.Annotation;

public class LocalContainer implements Container {
    
    private final Map<String, Object> definitions = new HashMap<String, Object>();
    private final Map<Binding.Key, Binding> bindings = new HashMap<Binding.Key, Binding>();
    protected final Map<Binding.Key, Object> singletons = new HashMap<Binding.Key, Object>();
    private final List<Container> delegates = new LinkedList<Container>();


    public Container define(String name, Object value) {
        this.definitions.put(name, value);
        return this;
    }

    public Object definitionValue(String name) {
        Object value = this.definitions.get(name);
        if (value != null) return value;
        for (Container delegate : this.delegates) {
            value = delegate.definitionValue(name);
            if (value != null) return value;
        }
        throw new ContainerException("No value for this name");
    }

    public Container declare(Binding binding) {
        this.bindings.put(binding.getKey(), binding);
        return this;
    }

    public <T> T obtainReference(Class<T> interfaceClass) {
        return this.obtainReference(interfaceClass, null);
    }

    public <T> T obtainReference(Class<T> interfaceClass, String qualifier) {
        
       
        Binding binding = this.bindings.get(new Binding.Key(interfaceClass, qualifier));
        // Try delegate if no binding found
        if (binding == null) {
            for (Container delegate : delegates) {
                if (delegate.hasReferenceDeclaredFor(interfaceClass, qualifier)) {
                    T reference = delegate.obtainReference(interfaceClass, qualifier);
                    return reference;
                }
            }
       
        throw new ContainerException("No binding for this Interface");
        }

        //Binding found

        
        if (binding.getPolicy() == Binding.Policy.SINGLETON && this.singletons.get(binding.getKey()) != null)
        return (T) this.singletons.get(binding.getKey());

        
        Class implementationClass = binding.getImplementationClass();
        for (Constructor constructor : implementationClass.getConstructors()) {
            try {
                
                // Try injecting constructor values
                Annotation[][] annotations = constructor.getParameterAnnotations();
                Class<?>[] argsTypes = constructor.getParameterTypes();
                Object[] args = new Object[argsTypes.length];

                for (int i=0 ; i < argsTypes.length ; i++) {
                    String annotationName = this.qualifierNameFor(annotations[i]);
                    try {
                        args[i] = this.obtainReference(argsTypes[i], annotationName);
                    } catch (ContainerException e) {
                        args[i] = this.definitionValue(annotationName);
                    }
                }
                T reference = (T) constructor.newInstance(args);
                if (binding.getPolicy() == Binding.Policy.SINGLETON)
                this.singletons.put(binding.getKey(), reference);
                return reference;
            } catch (Exception ignored) { System.out.println(ignored.getMessage()); }
        }

        // Can't instanciate
        throw new ContainerException("Failed to obtain reference for this interface");
    }


    public boolean hasReferenceDeclaredFor(Class<?> interfaceClass) {
        return this.hasReferenceDeclaredFor(interfaceClass, null);
    }

    public boolean hasReferenceDeclaredFor(Class<?> interfaceClass, String qualifier) {
        return (this.bindings.get(new Binding.Key(interfaceClass, qualifier)) != null);
    }

    public boolean hasValueDefinedFor(String name) {
        return (this.definitions.get(name) != null);
    }

    public Container delegateTo(Container container) {
        this.delegates.add(container);
        return this;
    }

    private String qualifierNameFor(Annotation[] parameterAnnotations) {
        for (Annotation annotation : parameterAnnotations) {
            if (annotation instanceof Named) {
            return ((Named) annotation).value();
            }
        }
        return null;
    }
}