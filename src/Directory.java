import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Directory {
    private final Map<String, Integer> nameInode;

    public Directory() {
        nameInode = new HashMap<>();
    }

    public void add(String name, int index) {
        nameInode.put(name, index);
    }

    public Integer get(String name) {
        return nameInode.get(name);
    }

    public void remove(String name) {
        nameInode.remove(name);
    }

    public Set<String> keySet () {
        return nameInode.keySet();
    }
}
