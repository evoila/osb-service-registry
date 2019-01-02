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
        if (get(t.getId()).isPresent())
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
        Optional<T> existing = get(t.getId());
        if (existing.isPresent())
            return Optional.<T>of(repository.save(t));
        return Optional.<T>empty();
    }

    public void remove(T t) {
        remove(t.getId());
    }

    public void remove(String id) {
        Optional<T> t = get(id);
        if (t.isPresent())
            repository.delete(t.get());
    }

    public void clear() {
        repository.deleteAll();
    }

}
