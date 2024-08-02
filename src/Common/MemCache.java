package Common;

import java.util.HashMap;
import java.util.Map;

public class MemCache<T> extends Cache<T> {
    private Map<Integer, T> cacheMap = new HashMap<>();;

    @Override
    public void resetCache() {
        cacheMap.clear();
    }

    @Override
    public void updateItem(Integer id, T item) {
        cacheMap.put(id, item);
    }

    @Override
    public T getItem(int id) {
        return cacheMap.get(id);
    }

    @Override
    public boolean hasItem(int id) {
        return cacheMap.containsKey(id);
    }

    @Override
    public void removeItem(int id) {
        cacheMap.remove(id);
    }

    @Override
    public void shutdown() {
        //Nothing to do for this one
    }

    @Override
    public Map<Integer, T> getMap() {
        return cacheMap;
    }

}
