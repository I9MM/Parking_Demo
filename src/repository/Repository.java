package repository;

import java.io.*;
import java.util.List;

public interface Repository<T> {
    void save(T entity) throws IOException;
    void saveAll(List<T> entities) throws IOException;
    List<T> loadAll() throws IOException, ClassNotFoundException;
    void delete(T entity) throws IOException;
    void clear() throws IOException;
}
