package de.evoila.osb.service.registry.manager;


import java.util.*;

import static org.junit.Assert.*;

public class ManagerTestService {

    public static void dropAll(BasicManager manager) {
        manager.clear();
    }

    /**
     * Tests the existence of the given object in the storage.
     * @param manager manager object to use for storage operations
     * @param randomObject the object to use
     * @param alreadyAdded whether the randomObject was already added to the storage and has the correct id
     * @param <T> identifiable class to use for the test and for the manager
     */
    public static <T extends Identifiable> void get(BasicManager<T> manager, T randomObject, boolean alreadyAdded) {
        assertFalse("Expected null when getting a specific object before saving it.", !alreadyAdded && manager.exists(randomObject));
        Optional<T> savedObject = alreadyAdded ? Optional.<T>of(randomObject) : manager.add(randomObject);
        assertOptionalIsPresent(savedObject);
        T receivedObject = manager.get(savedObject.get().getId()).get();
        assertTrue("Expected an equal object when getting a specific object.", receivedObject.equals(savedObject.get()));
    }

    /**
     * Tests the existence of the given list of objects in the storage.
     * @param manager manager object to use for storage operations
     * @param randomObjects list of objects to use
     * @param alreadyAdded whether the randomObjects were already added to the storage and have the correct id
     * @param <T> identifiable class to use for the test and for the manager
     */
    public static <T extends Identifiable> void getAll(BasicManager<T> manager, List<T> randomObjects, boolean alreadyAdded) {
        assertTrue("No objects given to add, therefore this test will be useless.", randomObjects != null && randomObjects.size() > 0);

        Iterator<T> iterator = manager.getAll().iterator();
        assertFalse("Expected an empty list.", iterator.hasNext());
        try {
            iterator.next();
            fail("Expected the iterator to throw an exception when accessing its first item.");
        } catch (NoSuchElementException ex) {
        }

        List<T> missingObjects = new LinkedList<>();
        for (T randomObject : randomObjects) {
            Optional<T> savedObject = alreadyAdded ? Optional.<T>of(randomObject) : manager.add(randomObject);
            assertOptionalIsPresent(savedObject);
            missingObjects.add(savedObject.get());
        }

        Iterable<T> receivedObjects = manager.getAll();
        for (T receivedObject : receivedObjects) {
            missingObjects.remove(receivedObject);
        }
        assertTrue("There are "+missingObjects.size()+" objects that could not be matched with an object from the storage. " +
                "This could be caused by an error in the equals method of the object or by a previous failed add operation of the object.",
                missingObjects.isEmpty());
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
