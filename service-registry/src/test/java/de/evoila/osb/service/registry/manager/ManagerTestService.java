package de.evoila.osb.service.registry.manager;


import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.*;

public class ManagerTestService {

    public static void dropAll(BasicManager manager) {
        manager.clear();
    }

    public static <T extends Identifiable> void get(BasicManager<T> manager, T randomObject, boolean alreadyAdded) {
        assertFalse("Expected null when getting a specific object before saving it.", !alreadyAdded && manager.exists(randomObject));
        Optional<T> savedObject = alreadyAdded ? Optional.<T>of(randomObject) : manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        T receivedObject = manager.get(savedObject.get().getId()).get();
        assertTrue("Expected an equal object when getting a specific object.", receivedObject.equals(savedObject.get()));
    }

    public static <T extends Identifiable> void getAll(BasicManager<T> manager, T random1, T random2) {
        Iterator<T> iterator = manager.getAll().iterator();
        assertFalse("Expected an empty list.", iterator.hasNext());
        try {
            iterator.next();
            fail("Expected the iterator to throw an exception when accessing its first item.");
        } catch (NoSuchElementException ex) {
        }

        Optional<T> savedObject1 = manager.add(random1);
        assertOptionalIsPresent(savedObject1);
        Optional<T> savedObject2 = manager.add(random2);
        assertOptionalIsPresent(savedObject2);

        iterator = manager.getAll().iterator();
        assertTrue("Expected the iterator to have an item", iterator.hasNext());
        T receivedObject = iterator.next();
        assertTrue("First received item is not as expected.", receivedObject.equals(savedObject1.get()) || receivedObject.equals(savedObject2.get()));
        receivedObject = iterator.next();
        assertTrue("Second received item is not as expected.", receivedObject.equals(savedObject1.get()) || receivedObject.equals(savedObject2.get()));
    }

    public static <T extends Identifiable> void add(BasicManager<T> manager, T randomObject) {
        assertFalse("Expected empty database at start of test.", manager.getAll().iterator().hasNext());
        Optional<T> savedObject = manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        assertTrue("Expected non-empty database after adding an object", manager.getAll().iterator().hasNext());
        assertTrue("Expected an equal object.", manager.getAll().iterator().next().equals(savedObject.get()));
    }

    public static <T extends Identifiable> void update(BasicManager<T> manager, T randomObject, T alteredObject) {
        assertFalse("Expected empty database at start of test.", manager.getAll().iterator().hasNext());
        Optional<T> savedObject = manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        alteredObject.setId(savedObject.get().getId());
        manager.update(alteredObject);
        assertFalse("Expected received object to be different than random object.", manager.get(alteredObject.getId()).get().equals(savedObject.get()));
        assertTrue("Expected received object to be same than altered object.", manager.get(alteredObject.getId()).get().equals(alteredObject));
    }

    public static <T extends Identifiable> void remove(BasicManager<T> manager, T randomObject) {
        Optional<T> savedObject = manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        assertTrue("Expected non-empty database before deleting via id.", manager.getAll().iterator().hasNext());
        manager.remove(savedObject.get().getId());
        assertFalse("Expected empty database after deleting via id.", manager.getAll().iterator().hasNext());

        savedObject = manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        assertTrue("Expected non-empty database before deleting via object.", manager.getAll().iterator().hasNext());
        manager.remove(savedObject.get().getId());
        assertFalse("Expected empty database after deleting via object.", manager.getAll().iterator().hasNext());
    }

    public static void assertOptionalIsPresent(Optional optional) {
        assertTrue("Failed to save the object. May be due to an already existing equal object.", optional.isPresent());
    }
}
