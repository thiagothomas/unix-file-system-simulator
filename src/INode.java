import java.util.Date;
import java.util.List;

public class INode {

    private static int idProvider = 0;
    private final int id;

    private final Date createData = new Date();
    private Date lastUpdatedDate = new Date();
    private Date lastAccessDate = new Date();
    public int[] perms = new int[]{7,0,0};
    private int size = 0;
    private final List<Integer> blocks;
    private int indBlock;
    private String owner;
    private final boolean isDirectory;

    public INode(String owner, boolean isDirectory, List<Integer> blocks, int indBlock) {
        this.id = idProvider++;
        this.owner = owner;
        this.isDirectory = isDirectory;
        this.blocks = blocks;
        this.indBlock = indBlock;
    }

    public int getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public Date getCreateData() {
        return createData;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Integer> getBlocks() {
        return blocks;
    }

    public int getIndBlock() {
        return indBlock;
    }

    public void setIndBlock(int indBlock) {
        this.indBlock = indBlock;
    }

    public static void resetIdProvider() {
        idProvider = 0;
    }
}
