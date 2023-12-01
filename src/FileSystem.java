import java.text.SimpleDateFormat;
import java.util.*;

public class FileSystem
{

    private final int[] systemBlocks;
    private final Block[] dataBlocks;

    private final List<INode> inodes;
    private final List<Directory> dirs;
    private final List<User> users;

    private int currDirInodeId;
    private User currUser;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public FileSystem ()
    {
        this.systemBlocks = new int[Utils.NUM_MAX_BLOCKS];
        this.dataBlocks = new Block[Utils.NUM_MAX_BLOCKS];
        this.inodes = new ArrayList<>();
        this.users = new ArrayList<>();
        this.dirs = new ArrayList<>();

        INode.resetIdProvider();
        User.resetIdProvider();

        User user = new User("root");
        INode root = new INode("root",
                               true,
                               new ArrayList<>(),
                               0);



        inodes.add(root);

        Directory dir = new Directory();
        dir.add(".",
                root.getId());
        dir.add("..",
                root.getId());

        this.dirs.add(dir);

        this.currDirInodeId = root.getId();

        this.users.add(user);
        this.currUser = user;
    }

    public void touch (String filename)
    {
        int i = nextIndex();

        if (i == -1) {
            System.out.println("Todos blocos de dados estão ocupados.");
            return;
        }

        this.addBlock(i,
                      new Block());
        this.createInode(filename,
                         false,
                         new ArrayList<>(List.of(i)));
    }

    public void cat (String filename)
    {
        if (!canPerformAction(filename,
                              Utils.Action.READ,
                              false)) {
            return;
        }
        directory(this.currDirInodeId);

        INode inode = inode(directory(this.currDirInodeId).get(filename));

        StringBuilder content = new StringBuilder();

        for (int index : inode.getBlocks()) {
            content.append(dataBlocks[index].data);
        }

        if (inode.getIndBlock() != 0) {
            content.append(this.dataBlocks[inode.getIndBlock()].data);
        }

        System.out.println(content);
        inode.setLastAccessDate(new Date());
    }

    public void chown (String name,
                       String newOwn)
    {
        Directory d = directory(this.currDirInodeId);

        Integer id = d.get(name);

        if (id == null) {
            System.out.printf("%s não existe no diretório atual\n",
                              name);
            return;
        }

        if (!existingUser(newOwn)) {
            System.out.println(newOwn + " não existe");
            return;
        }

        INode inode = inode(id);
        if (!canPerformAction(name,
                              Utils.Action.WRITE,
                              inode.isDirectory())) {
            return;
        }

        inode.setOwner(newOwn);
    }

    public void chmod (String name,
                       String newPerms)
    {
        Directory d = directory(this.currDirInodeId);

        Integer id = d.get(name);

        if (id == null) {
            System.out.printf("%s não existe no diretório atual\n", name);
            return;
        }

        INode inode = inode(id);
        if (!canPerformAction(name, Utils.Action.WRITE, inode.isDirectory())) {
            return;
        }

        int[] permsArray = new int[newPerms.length()];
        for (int i = 0; i < newPerms.length(); i++) {
            char digitChar = newPerms.charAt(i);
            int digit = Character.getNumericValue(digitChar);
            permsArray[i] = digit;
        }

        inode.perms = permsArray;
        System.out.println("Permissões alteradas");
    }

    public void mkdir (String dirName)
    {
        INode inode = createInode(dirName,
                                  true,
                                  new ArrayList<>());

        if (inode != null) {
            Directory d = new Directory();
            d.add(".",
                          inode.getId());
            d.add("..",
                          this.currDirInodeId);
            this.dirs.add(d);
        }
    }

    public void cd (String dirName)
    {
        if (!canPerformAction(dirName,
                              Utils.Action.READ,
                              true)) {
            return;
        }

        this.currDirInodeId = directory(currDirInodeId).get(dirName);
    }

    public void ls ()
    {
        ls(null);
    }

    public void ls (String dirName)
    {
        int id;

        if (dirName != null) {
            if (!canPerformAction(dirName,
                                  Utils.Action.READ,
                                  true)) {
                return;
            }

            id = directory(this.currDirInodeId).get(dirName);
        }
        else {
            id = this.currDirInodeId;
        }

        Directory d = directory(id);

        System.out.printf("%-15s%-7s%-15s%-10s%-23s%-23s%-23s%-10s%-10s\n",
                          "Nome",
                          "Tam",
                          "Permissões",
                          "Criador",
                          "Criado em",
                          "Atualizado em",
                          "Acessado em",
                          "inode",
                          "Bloco In");
        for (String name : d.keySet()) {
            INode inode = inode(d.get(name));
            if (inode.isDirectory()) {
                System.out.printf(Utils.ANSI_BOLD + Utils.ANSI_CYAN + "%-15s" + Utils.ANSI_RESET,
                                  name);
            }
            else {
                System.out.printf("%-15s",
                                  name);
            }
            System.out.printf("%-7s%-15s%-10s%-23s%-23s%-23s%-10s%-10s\n",
                              inode.getSize(),
                              getPermsSting(inode.perms),
                              inode.getOwner(),
                              SIMPLE_DATE_FORMAT.format(inode.getCreateData()),
                              SIMPLE_DATE_FORMAT.format(inode.getLastUpdatedDate()),
                              SIMPLE_DATE_FORMAT.format(inode.getLastAccessDate()),
                              inode.getId(),
                              inode.getIndBlock());
        }
    }

    public void adduser (String username)
    {
        if (!canAdministerUsers()) {
            return;
        }

        if (existingUser(username)) {
            System.out.println("Usuário ja existente: " + username);
            return;
        }

        this.users.add(new User(username));
        System.out.println(username + " criado");
    }

    public void rmuser (String username)
    {
        if (!canAdministerUsers()) {
            return;
        }

        if (username.equals("root")) {
            System.out.println("Não é permitido deletar o usuário root");
        }

        if (!existingUser(username)) {
            System.out.println("Usuário não existe: " + username);
            return;
        }

        this.users.remove(user(username));

        System.out.println(username + " removido");
    }

    public void lsuser ()
    {
        System.out.println("Usuários: ");
        for (User user : users) {
            if (currUser.getId() == user.getId()) {
                System.out.println(Utils.ANSI_CYAN + Utils.ANSI_BOLD + user.getName()
                                                   + Utils.ANSI_RESET);
            }
            else {
                System.out.println(user.getName());
            }
        }
    }

    public void loginuser (String username)
    {
        User u = user(username);
        if (u == null) {
            System.out.println(username + " não existe");
            return;
        }

        this.currUser = u;

        System.out.println("Atuando como " + username);
    }

    public void gravar (String filename,
                        String buffer)
    {
        if (!canPerformAction(filename, Utils.Action.WRITE, false)) {
            return;
        }

        int nbytes = buffer.length();

        INode inode = inode(directory(currDirInodeId).get(filename));

        int missingDataBytes = nbytes;
        int iBlock = inode.getBlocks().size() - 1;
        int pBlock = iBlock > -1 ? this.dataBlocks[inode.getBlocks().get(iBlock)].data.length() : 0;

        while (missingDataBytes > 0) {
            if (pBlock == Utils.BYTES_BLOCK_SIZE) {
                int newIblock = nextIndex();

                if (newIblock == -1) {
                    System.out.println("Não há blocos livres.");
                    return;
                }

                if (inode.getBlocks().size() < Utils.NUM_MAX_INODE_BLOCKS) {
                    inode.getBlocks().add(newIblock);
                    iBlock++;
                    pBlock = 0;
                }
                else if (inode.getIndBlock() == 0) {
                    inode.setIndBlock(newIblock);
                }
                else {
                    System.out.println("Não é possível escrever mais dados, todos os blocos estão cheios.");
                    return;
                }
            }

            int systemIblock = inode.getBlocks().get(iBlock);
            Block b;
            if (systemIblock < this.dataBlocks.length) {
                b = this.dataBlocks[systemIblock];
                if (b == null) {
                    b = new Block();
                    this.dataBlocks[systemIblock] = new Block();
                }
            } else {
                b = new Block();
            }

            int dataBytes = Math.min(missingDataBytes, Utils.BYTES_BLOCK_SIZE - pBlock);
            b.data = b.data.substring(0, pBlock) + buffer.substring(nbytes - missingDataBytes, nbytes - missingDataBytes + dataBytes);
            inode.setSize(inode.getSize() + dataBytes);
            missingDataBytes -= dataBytes;
            pBlock += dataBytes;

            addBlock(systemIblock, b);
        }

        inode.setLastUpdatedDate(new Date());
    }

    private boolean canPerformAction (String name,
                                      Utils.Action action,
                                      boolean isDir)
    {
        Directory d = directory(this.currDirInodeId);

        Integer id = d.get(name);

        if (id == null) {
            System.out.printf("%s não existe no diretório atual\n",
                              name);
            return false;
        }

        INode inode = inode(id);

        if (inode.isDirectory() != isDir) {
            if (isDir) {
                System.out.printf("%s não é um diretório\n",
                                  name);
            }
            else {
                System.out.printf("%s não é um arquivo\n",
                                  name);
            }

            return false;
        }

        if (this.currUser.getName().equals("root")) {
            return true;
        }

        int userPermission;

        if (currUser.getName().equals(inode.getOwner())) {
            userPermission = inode.perms[0];
        }
        else {
            userPermission = inode.perms[2];
        }

        boolean allow = false;

        if (action == Utils.Action.EXECUTE && (userPermission == 1 || userPermission == 3
                        || userPermission == 5 || userPermission == 7)) {
            allow = true;
        }
        else if (action == Utils.Action.WRITE && (userPermission == 2 || userPermission == 3
                        || userPermission == 6 || userPermission == 7)) {
            allow = true;
        }
        else if (action == Utils.Action.READ && (userPermission == 4 || userPermission == 5
                        || userPermission == 6 || userPermission == 7)) {
            allow = true;
        }

        if (!allow) {
            System.out.println("Ação não permitida para o usuário");
        }

        return allow;
    }

    private boolean canAdministerUsers ()
    {
        if (!this.currUser.getName().equals("root")) {
            System.out.println("Só o usuário root pode administrar usuários");
            return false;
        }
        else {
            return true;
        }
    }

    private boolean existingUser (String username)
    {
        return this.users.stream().anyMatch(u -> u.getName().equals(username));
    }

    private User user (String username)
    {
        User user = null;

        for (User u : users) {
            if (u.getName().equals(username)) {
                user = u;
            }
        }

        return user;
    }

    private INode createInode (String name,
                               boolean isDirectory,
                               List<Integer> blocksIndexes)
    {
        Directory d = directory(this.currDirInodeId);

        if (d.get(name) != null) {
            System.out.printf("%s já existe no diretório atual.\n",
                              name);
            return null;
        }

        INode inode = new INode(this.currUser.getName(),
                                isDirectory,
                                blocksIndexes,
                                0);
        this.inodes.add(inode);
        d.add(name,
                inode.getId());

        return inode;
    }

    public void deleteInode (String name,
                              boolean isDir)
    {
        if (!canPerformAction(name,
                              Utils.Action.WRITE,
                              isDir)) {
            return;
        }
        Directory d = directory(currDirInodeId);
        int id = d.get(name);
        INode inode = inode(id);
        d.remove(name);

        int inodeIndex = -1;
        for (int i = 0; i < inodes.size(); i++) {
            if (inodes.get(i).getId() == id) {
                inodeIndex = i;
                break;
            }
        }
        inodes.remove(inodeIndex);

        if (isDir) {
            int dirIndex = -1;
            for (int i = 0; i < dirs.size(); i++) {
                if (dirs.get(i).get(".") == id) {
                    dirIndex = i;
                    break;
                }
            }
            this.dirs.remove(dirIndex);
        }

        inode.getBlocks().forEach(i -> {
            this.dataBlocks[i] = null;
            this.systemBlocks[i] = 0;
        });

        if (inode.getIndBlock() != 0) {
            this.dataBlocks[inode.getIndBlock()] = null;
            this.systemBlocks[inode.getIndBlock()] = 0;
        }
    }

    private int nextIndex ()
    {
        int temp = -1;

        for (int i = 0; i < systemBlocks.length; i++) {
            if (systemBlocks[i] == 0) {
                temp = i;
                break;
            }
        }

        return temp;
    }

    private void addBlock (int i,
                           Block block)
    {
        this.dataBlocks[i] = block;
        this.systemBlocks[i] = 1;
    }

    private Directory directory (int inode)
    {
        for (Directory directory : dirs) {
            if (directory.get(".") == inode) {
                return directory;
            }
        }

        throw new RuntimeException("Diretorio não encontrado para inode de id: " + inode);
    }

    private INode inode (Integer id)
    {
        for (INode i : inodes) {
            if ((i.getId() == id)) {
                return i;
            }
        }

        throw new RuntimeException("Inode não encontrado para id: " + id);
    }

    public User user ()
    {
        return currUser;
    }

    private String getPermsSting (int[] perms)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            result.append((perms[i] & 4) != 0 ? "r" : "-");
            result.append((perms[i] & 2) != 0 ? "w" : "-");
            result.append((perms[i] & 1) != 0 ? "x" : "-");
        }

        return result.toString();
    }

    public String path ()
    {
        StringBuilder path = new StringBuilder();
        int current = this.currDirInodeId;

        while (current != 0) {
            Directory d = this.directory(current);
            int parentId = d.get("..");
            Directory parentDir = this.dirs.stream()
                            .filter(dir -> dir.get(".") == parentId).findFirst().orElseThrow();
            int finalCurrent = current;
            String name = parentDir.keySet().stream()
                            .filter(key -> parentDir.get(key) == finalCurrent).findFirst()
                            .orElse(null);
            path.insert(0,
                        "/" + name);
            current = parentId;
        }

        return path + " ";
    }
}
