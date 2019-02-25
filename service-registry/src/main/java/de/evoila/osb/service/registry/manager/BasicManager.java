package de.evoila.osb.service.registry.manager;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public class BasicManager<T extends Identifiable> {

    private CrudRepository<T, String> repository;

    public BasicManager(CrudRepository<T, String> repository) {
        this.repository = repository;
    }

    public Optional<T> get(String id) {
        return repository.findById(id);
    }

    public Iterable<T> getAll() {
        return repository.findAll();
    }

    /**
     * Adds an object of the type T to the storage, if non already exists with the same id.
     *
     * @param t object of type T to add to the storage, its id will be used for the existence check
     * @return An empty {@linkplain Optional} if there is already an object with the same id or an {@linkplain Optional} with the updated saved object.
     */
    public Optional<T> add(T t) {
        if (exists(t.getId()))
            return Optional.<T>empty();
        return Optional.<T>of(repository.save(t));
    }

    /**
     * Updates an object of the type T in the storage, if it exists.
     *
     * @param t object of type T to update in the storage, its id will be used for the existence check
     * @return An empty {@linkplain Optional} if there is no object with the same id or an {@linkplain Optional} with the updated saved object.
     */
    public Optional<T> update(T t) {
        if (exists(t))
            return Optional.<T>of(repository.save(t));
        return Optional.<T>empty();
    }

    public void remove(T t) {
        if (t != null)
            repository.delete(t);
    }

    public void remove(String id) {
        if (exists(id))
            repository.deleteById(id);
    }

    public void removeMultiple(Iterable<? extends T> iterable) {
        repository.deleteAll(iterable);
    }

    /**
     * Also removes all weak entities or entities that should not exist without a relation, that are tied to this managers class T.
     * ! Note: The BasicManager does not support cascading removals since its class T's structure is not known, therefore a regular {@linkplain #remove(Identifiable)} is performed.
     * ! -> Classes that extend the BasicManager and have information about the class T's structure can implement this feature.
     *
     * @param t identifiable object to remove cascadingly
     */
    public void removeCascading(T t) {
        remove(t);
    }

    /**
     * Also removes all weak entities or entities that should not exist without a relation, that are tied to this managers class T.
     * ! Note: The BasicManager does not support cascading removals since its class T's structure is not known, therefore a regular {@linkplain #remove(String)} is performed.
     * ! -> Classes that extend the BasicManager and have information about the class T's structure can implement this feature.
     * @param id id of the object to remove cascadingly
     */
    public void removeCascading(String id) {
        remove(id);
    }

    public void clear() {
        repository.deleteAll();
    }

    /**
     * Also removes all weak entities or entities that should not exist without a relation, that are tied to this managers class T.
     * ! Note: The BasicManager does not support cascading removals since its class T's structure is not known, therefore a regular {@linkplain #clear} is performed.
     * ! -> Classes that extend the BasicManager and have information about the class T's structure can implement this feature.
     */
    public void clearCascading() {
        clear();
    }

    public boolean exists(String id) {
        return repository.existsById(id);
    }

    public boolean exists(T t) {
        if (t != null && t.getId() != null || !t.getId().isEmpty())
            return exists(t.getId());
        return false;
    }

    public long count() {
        return repository.count();
    }

}
