package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author Brandon Nguyen
 */
public class Repository implements Serializable {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    // Folder in the .gitlet Directory called StageArea
    public static final File STAGEAREA = join(GITLET_DIR, "StageArea");
    // Folder in the .gitlet Directory called CommitFolder that holds all the Commits as a file
    public static final File COMMITFOLDER = join(GITLET_DIR, "CommitsFolder");
    // Folder in the .gitlet Directory called BlogsFolder that stores all the blops as a file
    public static final File BLOBS = join(GITLET_DIR, "BlobsFolder");
    // The staging add Area as a Hash map
    private HashMap<String, String> STAGEADD;
    // The staging remove Area as a Hash map
    private HashMap<String, String> stageRemoval;
    // A hash map to store the key:name of the file and value: Sha1 of the commit
    private HashMap<String, String> leadingCommit;
    private String nameofCurrbranch;
    // A file inside the gitlet directory
    public static final File REPOFILE = join(GITLET_DIR, "Repo File");


    public Repository() {
        stageRemoval = new HashMap<>();
        STAGEADD = new HashMap<>();
        nameofCurrbranch = "master"; // master
        leadingCommit =  new HashMap<>();
    }
    // reads all the constructor of stageAdd,stageRemove, etc... returns a repo object
    public static Repository grabRepo() {
        return readObject(REPOFILE, Repository.class);
    }
    // to repo writes the object to the repo, and this repo is the repo that we read from fromRepo()
    public void sendbackRepo() {
        writeObject(REPOFILE, this);
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            // System.exit(0);
        }
        GITLET_DIR.mkdir(); //Gitlet repository or folder
        Commit initialCommit = new Commit("initial commit", null, null, "master");
        COMMITFOLDER.mkdir(); // Makes CommitFolder Folder
        STAGEAREA.mkdir(); // Makes Staging Area Folder
        BLOBS.mkdir(); // Makes Blobs folder
        byte[] com0 = serialize(initialCommit);
        String initID = sha1(com0); // serialized initial commit ID
        File commit0 = join(COMMITFOLDER, initID);
        writeObject(commit0, initialCommit);
        Repository reposit = new Repository(); // creates all the objects for us so we can modify it
        reposit.leadingCommit.put("master", initID); // reposit.nameofCurrbranch
        reposit.sendbackRepo(); // sends back all the information to the repository file
    }

    public static void add(String f) {
        Repository currRepo = grabRepo();
        File now = join(CWD, f);
        if (!now.exists()) {
            gitlet.Utils.message("File does not exist.");
            System.exit(0);
        }
        String fileName = f;
        byte[] currCont = readContents(now); // read all the info of the txt.
        String currContread = sha1(currCont); //gets a unique id of the Content
        String commitId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File headcommit = join(COMMITFOLDER, commitId);
        Commit head = readObject(headcommit, Commit.class);
        if (head.nowBlops().containsKey(fileName) & currRepo.stageRemoval.isEmpty()) {
            if (head.nowBlops().get(f).equals(currContread)) {
                System.exit(0);
            }
        }
        if (currRepo.stageRemoval.containsKey(fileName)) {
            currRepo.stageRemoval.remove(fileName);
            currRepo.sendbackRepo();
        } else if (currRepo.STAGEADD.containsKey(fileName)) {
            if (currRepo.STAGEADD.containsValue(currContread)) {
                System.exit(0);
            } else {
                currRepo.STAGEADD.remove(fileName);
                currRepo.STAGEADD.put(fileName, currContread);
                currRepo.sendbackRepo();
            }
        } else { //
            currRepo.STAGEADD.put(fileName, currContread);
            // currRepo.sendbackRepo();
            File newblob = join(BLOBS, currContread);
            writeObject(newblob, currCont);
            currRepo.sendbackRepo();
        }
    }

    public static void rm(String f) {
        Repository currRepo = grabRepo();
        String commitparentId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File headCommit = join(COMMITFOLDER, commitparentId);
        Commit head = readObject(headCommit, Commit.class);
        if (!currRepo.STAGEADD.containsKey(f) && !head.nowBlops().containsKey(f)) {
            message("No reason to remove the file.");
            System.exit(0);
        }
        if (currRepo.STAGEADD.containsKey(f)) {
            currRepo.STAGEADD.remove(f);
        }
        if (head.nowBlops().containsKey(f)) {
            File cwdCheck = join(CWD, f);
            if (cwdCheck.exists()) {
                String contId = sha1(readContents(cwdCheck));
                currRepo.stageRemoval.put(f, contId);
                gitlet.Utils.restrictedDelete(cwdCheck);
            } else {
                String contID = head.nowBlops().get(f);
                currRepo.stageRemoval.put(f, contID);
            }
        }
        currRepo.sendbackRepo();
    }


    public static void commit(String comMsg) {
        Repository currRepo = grabRepo();
        String commitparentId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File headCommit = join(COMMITFOLDER, commitparentId);
        Commit parent = readObject(headCommit, Commit.class);
        byte[] serializeParent = serialize(parent);
        String parentId = sha1(serializeParent); //Commit Parent Sha1
        Commit clone = new Commit(comMsg, null, parentId, currRepo.nameofCurrbranch); // changed
        if (comMsg.isBlank()) {
            message("Please enter a commit message.");
            System.exit(0);
        }
        if (currRepo.STAGEADD.isEmpty()) {
            if (currRepo.stageRemoval.isEmpty()) {
                message("No changes added to the commit.");
                System.exit(0);
            }
        }
        if (comMsg == null) {
            message("Please enter a commit message.");
            System.exit(0);
        }
        if (!currRepo.STAGEADD.isEmpty()) {
            while (!currRepo.STAGEADD.isEmpty()) {
                String names = (String) currRepo.STAGEADD.keySet().toArray()[0];
                String contents = currRepo.STAGEADD.get(names);
                if (clone.nowBlops().containsKey(names)) { //
                    clone.nowBlops().remove(names); //
                }
                clone.nowBlops().put(names, contents); //
                currRepo.STAGEADD.remove(names);
                currRepo.sendbackRepo();
            }
        }

        if (!currRepo.stageRemoval.isEmpty()) {
            while (!currRepo.stageRemoval.isEmpty()) {
                String names = (String) currRepo.stageRemoval.keySet().toArray()[0];
                if (clone.nowBlops().containsKey(names)) { ///
                    clone.nowBlops().remove(names); ///
                    currRepo.stageRemoval.remove(names);
                    currRepo.sendbackRepo();
                }
            }
        }
        byte[] newComCont = serialize(clone);
        String newComID = sha1(newComCont); // Clone commit ID
        File newCommit = join(COMMITFOLDER, newComID);
        writeObject(newCommit, clone);
        currRepo.leadingCommit.put(currRepo.nameofCurrbranch, newComID);
        currRepo.sendbackRepo();
    }
    public static void status() {
        Repository currRepo = grabRepo();
        System.out.println("=== Branches ===");
        for (int i = 0; i < currRepo.leadingCommit.size(); i++) {
            String kn = (String) currRepo.leadingCommit.keySet().stream().sorted().toArray()[i];
            String x = currRepo.nameofCurrbranch; //
            if (!kn.equals(currRepo.nameofCurrbranch)) {
                System.out.println(kn);
            } else if (kn.equals(currRepo.nameofCurrbranch)) {
                System.out.println("*" + kn);
            }
        }
        System.out.print("\n");
        System.out.println("=== Staged Files ===");
        if (!currRepo.STAGEADD.isEmpty()) {
            for (int i = 0; i < currRepo.STAGEADD.size(); i++) {
                String fk = (String) currRepo.STAGEADD.keySet().stream().sorted().toArray()[i];
                System.out.println(fk);
            }
        }
        System.out.print("\n");
        System.out.println("=== Removed Files ===");
        if (!currRepo.stageRemoval.isEmpty()) {
            for (int i = 0; i < currRepo.stageRemoval.size(); i++) {
                String fk = (String) currRepo.stageRemoval.keySet().stream().sorted().toArray()[i];
                System.out.println(fk);
            }
        }
        System.out.print("\n");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print("\n");
        System.out.println("=== Untracked Files ===");
        System.out.print("\n");
        currRepo.sendbackRepo();
    }
    public static void log() {
        Repository currRepo = grabRepo();
        String commitparentId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File headCommit = join(COMMITFOLDER, commitparentId);
        Commit headRead = readObject(headCommit, Commit.class);
        while (headRead != null) {
            Object ser = serialize(headRead);
            SimpleDateFormat myDate = new SimpleDateFormat("EEE MMM d HH:mm:ss yyy Z");
            System.out.println("===");
            System.out.println("commit " + sha1(ser));
            System.out.println("Date: " + myDate.format(headRead.timestamp));
            System.out.println(headRead.message);
            System.out.println();
            if (headRead.parent == null) {
                headRead = null;
            } else {
                String parentID = headRead.parent;
                File parent = join(COMMITFOLDER, parentID);
                headRead = readObject(parent, Commit.class);
            }
        }
    }

    public static void globalLog() {
        for (String commits:plainFilenamesIn(COMMITFOLDER)) {
            File currentcommitId = join(COMMITFOLDER, commits);
            Commit current = readObject(currentcommitId, Commit.class);
            Object ser = serialize(current);
            SimpleDateFormat myDate = new SimpleDateFormat("EEE MMM d HH:mm:ss yyy Z");
            System.out.println("===");
            System.out.println("commit " + sha1(ser));
            System.out.println("Date: " + myDate.format(current.timestamp));
            System.out.println(current.message);
            System.out.println();
        }
    }

    public static void find(String message) {
        int tracker = 0;
        for (String commits : plainFilenamesIn(COMMITFOLDER)) {
            File currentcommitId = join(COMMITFOLDER, commits);
            Commit current = readObject(currentcommitId, Commit.class);
            if (current.message.equals(message)) {
                System.out.println(commits);
                tracker += 1;
            }
        }
        if (tracker < 1) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void checkoutFile(String fileName) {
        Repository currRepo = grabRepo();
        String headCommitId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        checkoutID(headCommitId, fileName);
    }
    public static String checkoutIDhelper(String commitID) {
        for (String commitfile : plainFilenamesIn(COMMITFOLDER)) {
            int commitIDlength = commitID.length();
            String commitsha = commitfile.substring(0, commitIDlength);
            if (commitsha.equals(commitID)) {
                return commitfile;
            }
        }
        return commitID;
    }

    public static void checkoutID(String commitID, String fileName) {
        if (commitID.length() < UID_LENGTH) {
            commitID = checkoutIDhelper(commitID);
        }
        File headCommit = join(COMMITFOLDER, commitID);
        if (!headCommit.exists()) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        Commit head = readObject(headCommit, Commit.class);
        if (head.nowBlops().containsKey(fileName)) {
            String commitContents = head.nowBlops().get(fileName);
            File currFile = join(BLOBS, commitContents);
            byte[] contents = readObject(currFile, byte[].class);
            File cwdCheck = join(CWD, fileName);
            if (cwdCheck.exists()) {
                byte[] oldfileCont = readContents(cwdCheck);
                if (!oldfileCont.equals(contents)) {
                    writeContents(cwdCheck, contents);
                } else if (oldfileCont.equals(contents)) {
                    System.exit(0);
                }
            } else {
                File filetoCwd = join(CWD, fileName);
                writeContents(filetoCwd, contents);
            }
        } else if (!head.nowBlops().containsKey(fileName)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
    }
    public static void checkoutBranch(String branchName) {
        Repository currRepo = grabRepo();
        if (!currRepo.leadingCommit.containsKey(branchName)) {
            gitlet.Utils.message("No such branch exists.");
            System.exit(0);
        }
        if (currRepo.nameofCurrbranch.equals(branchName)) {
            gitlet.Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }
        String headBranchId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File headBranch = join(COMMITFOLDER, headBranchId);
        Commit branch = readObject(headBranch, Commit.class);
        String newbranchId = currRepo.leadingCommit.get(branchName);
        File newBranch = join(COMMITFOLDER, newbranchId);
        Commit newCommit = readObject(newBranch, Commit.class);
        for (String file: plainFilenamesIn(CWD)) {
            if (!branch.nowBlops().containsKey(file)
                    && newCommit.nowBlops().containsKey(file)
                        && !currRepo.STAGEADD.containsKey(file)) {
                message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        for (String f: plainFilenamesIn(CWD)) {
            if (!newCommit.nowBlops().containsKey(f) && branch.nowBlops().containsKey(f)) {
                File deleteF = join(CWD, f);
                gitlet.Utils.restrictedDelete(deleteF);
            }
        }
        for (int i = 0; i < newCommit.nowBlops().size(); i++) {
            String fileName = (String) newCommit.nowBlops().keySet().toArray()[i];
            checkoutID(newbranchId, fileName);
        }
        currRepo.nameofCurrbranch = branchName;
        currRepo.STAGEADD.clear();
        currRepo.stageRemoval.clear();
        currRepo.sendbackRepo();


    }
    public static void removeBranch(String bname) {
        Repository currRepo = grabRepo();
        if (bname.equals(currRepo.nameofCurrbranch)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }
        if (!currRepo.leadingCommit.containsKey(bname)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        currRepo.leadingCommit.remove(bname);
        currRepo.sendbackRepo();
    }
    public static void branch(String name) {
        Repository currRepo = grabRepo();
        if (currRepo.leadingCommit.containsKey(name)) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        currRepo.leadingCommit.put(name, currRepo.leadingCommit.get(currRepo.nameofCurrbranch));
        currRepo.sendbackRepo();
    }

    public static void reset(String commitID) {
        Repository currRepo = grabRepo();
        if (commitID.length() < UID_LENGTH) {
            commitID = checkoutIDhelper(commitID);
        }
        File headCommit = join(COMMITFOLDER, commitID);
        if (!headCommit.exists()) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        String headBranchId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File currBranch = join(COMMITFOLDER, headBranchId);
        Commit branch = readObject(currBranch, Commit.class); /** current head branch commit */
        File comBranch = join(COMMITFOLDER, commitID);
        Commit newCommit = readObject(comBranch, Commit.class); /** given commit */
        for (String file: plainFilenamesIn(CWD)) {
            if (!branch.nowBlops().containsKey(file)
                    && newCommit.nowBlops().containsKey(file)
                    && !currRepo.STAGEADD.containsKey(file)) {
                message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        for (String f: plainFilenamesIn(CWD)) {
            if (!newCommit.nowBlops().containsKey(f) && branch.nowBlops().containsKey(f)) {
                File deleteF = join(CWD, f);
                gitlet.Utils.restrictedDelete(deleteF);
            }
        }

        for (int i = 0; i < newCommit.nowBlops().size(); i++) {
            String fileName = (String) newCommit.nowBlops().keySet().toArray()[i];
            checkoutID(commitID, fileName);
        }
        String currName = newCommit.getBranch();
        currRepo.nameofCurrbranch = currName;
        currRepo.leadingCommit.put(currName, commitID);
        currRepo.STAGEADD.clear();
        currRepo.stageRemoval.clear();
        currRepo.sendbackRepo();
    }
    public static String splitPointfinder(Commit curr, Commit given) {
        Repository currRepo = grabRepo();
        String x = (curr.parent);
        String y = (given.parent);
        if (curr.parent == null) {
            return "No split point";

        } else if (curr.parent.equals(given.parent)) {
            File headCommit = join(COMMITFOLDER, curr.parent);
            // Commit parent = readObject(headCommit, Commit.class);
            String splitCommitid = curr.parent;
            return splitCommitid;
        } else {
            String currCommitidparent = currRepo.leadingCommit.get(curr.parent);
            File currCommitparentfile = join(COMMITFOLDER, currCommitidparent);
            Commit currCommitparent = readObject(currCommitparentfile, Commit.class);
            String givenCommitidparent = currRepo.leadingCommit.get(given.parent);
            File givenCommitparentfile = join(COMMITFOLDER, givenCommitidparent);
            Commit givenCommitparent = readObject(givenCommitparentfile, Commit.class);
            return splitPointfinder(currCommitparent, givenCommitparent);
        }
    }
    public static void mergerError(Commit curr, Commit given, String filename) {
        Repository currRepo = grabRepo();
        File cwdFile = join(CWD, filename);
        System.out.println("Encountered a merge conflict.");
        String currContentsid = curr.nowBlops().get(filename);
        String givenContentsid = given.nowBlops().get(filename);
        File currCont = join(BLOBS, currContentsid);
        File givenCont = join(BLOBS, givenContentsid);
        byte[] currContbyte = readObject(currCont, byte[].class);
        byte[] givenContbyte = readObject(givenCont, byte[].class);
        writeContents(cwdFile, "<<<<<<< HEAD\n", currContbyte, "=======\n",
                givenContbyte, ">>>>>>>\n");
        Repository.add(filename);
        currRepo.STAGEADD.put(filename, null);
        currRepo.sendbackRepo();

    }
    public static void mergerErrortwo(Commit curr, Commit given, String filename) {
        Repository currRepo = grabRepo();
        File cwdFile = join(CWD, filename);
        System.out.println("Encountered a merge conflict.");
        String currContentsid = curr.nowBlops().get(filename);
        File currCont = join(BLOBS, currContentsid);
        byte[] currContbyte = readObject(currCont, byte[].class);
        writeContents(cwdFile, "<<<<<<< HEAD\n", currContbyte, "=======\n",
                "", ">>>>>>>\n");
        Repository.add(filename);
        currRepo.STAGEADD.put(filename, null);
        currRepo.sendbackRepo();
    }
    public static void mergerErrorthree(Commit curr, Commit given, String filename) {
        Repository currRepo = grabRepo();
        File cwdFile = join(CWD, filename);
        System.out.println("Encountered a merge conflict.");
        String givenContentsid = given.nowBlops().get(filename);
        File givenCont = join(BLOBS, givenContentsid);
        byte[] givenContbyte = readObject(givenCont, byte[].class);
        writeContents(cwdFile, "<<<<<<< HEAD\n", "", "=======\n",
                givenContbyte, ">>>>>>>\n");
        Repository.add(filename);
        currRepo.STAGEADD.put(filename, null);
        currRepo.sendbackRepo();
    }

    public static void checkerone(Commit curr, Commit given, Commit split) {
        Repository cRepo = grabRepo();
        for (int i = 0; i < split.nowBlops().size(); i++) {
            String splitfileName = (String) split.nowBlops().keySet().toArray()[i];
            String splitFilecont = split.nowBlops().get(splitfileName);
            if (given.nowBlops().containsKey(splitfileName)
                    && curr.nowBlops().containsKey(splitfileName)) {
                if (!given.nowBlops().get(splitfileName).equals(splitFilecont)
                        && !curr.nowBlops().get(splitfileName).equals(splitFilecont)) {
                    mergerError(curr, given, splitfileName);
                }
                if (curr.nowBlops().get(splitfileName).equals(splitFilecont)
                        // curr cont == split cont & !given cont == split cont
                        && !given.nowBlops().get(splitfileName).equals(splitFilecont)) {
                    checkoutID(cRepo.leadingCommit.get(given.getBranch()), splitfileName); //maybe
                    Repository.add(splitfileName);
                } else if (given.nowBlops().get(splitfileName).equals(splitFilecont)
                        && !curr.nowBlops().get(splitfileName).equals(splitFilecont)) {
                    continue;
                }
            }
            if (!given.nowBlops().containsKey(splitfileName)
                    && !curr.nowBlops().containsKey(splitfileName)) {
                continue;
            }
            if (curr.nowBlops().containsKey(splitfileName)
                    && !given.nowBlops().containsKey(splitfileName)) {
                Repository.rm(splitfileName);
            }
            if (!curr.nowBlops().containsKey(splitfileName)
                    && given.nowBlops().containsKey(splitfileName)) {
                continue;
            }
            if (!given.nowBlops().containsKey(splitfileName)
                    && !curr.nowBlops().get(splitfileName).equals(splitFilecont)) {
                mergerErrortwo(curr, given, splitfileName);
            }
            if (!curr.nowBlops().containsKey(splitfileName)
                    && !given.nowBlops().get(splitfileName).equals(splitFilecont)) {
                mergerErrorthree(curr, given, splitFilecont);
            }
        }
    }
    public static void chckertwo(Commit curr, Commit given, Commit split) {
        // Repository currRepo = grabRepo();
        for (int i = 0; i < curr.nowBlops().size(); i++) {
            String currfileName = (String) curr.nowBlops().keySet().toArray()[i];
            String currFilecont = curr.nowBlops().get(currfileName);
            if (!split.nowBlops().containsKey(currfileName)
                    && !given.nowBlops().containsKey(currfileName)) {
                continue;
            } else if (!split.nowBlops().containsKey(currfileName)
                    && !given.nowBlops().get(currfileName).equals(currFilecont)) {
                mergerError(curr, given, currfileName);
            }
        }
    }
    public static void chckerthree(Commit curr, Commit given, Commit split) {
        Repository currRepo = grabRepo();
        String commitId = currRepo.leadingCommit.get(given.getBranch());
        for (int i = 0; i < given.nowBlops().size(); i++) {
            String givfileName = (String) given.nowBlops().keySet().toArray()[i];
            String givFilecont = curr.nowBlops().get(givfileName);
            if (!split.nowBlops().containsKey(givfileName)
                    && !curr.nowBlops().containsKey(givfileName)) {
                checkoutID(commitId, givfileName);
                currRepo.STAGEADD.put(givfileName, givFilecont);
            }
        }
    }


    public static void merge(String branchName) {
        Repository currRepo = grabRepo();
        if (!currRepo.leadingCommit.containsKey(branchName)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        String headBranchId = currRepo.leadingCommit.get(currRepo.nameofCurrbranch);
        File currBranchget = join(COMMITFOLDER, headBranchId);
        Commit currBranch = readObject(currBranchget, Commit.class);
        /** current head branch commit */
        String givenId = currRepo.leadingCommit.get(branchName);
        File comBranch = join(COMMITFOLDER, givenId);
        Commit givCommit = readObject(comBranch, Commit.class); /** given commit */
        if (!currRepo.STAGEADD.isEmpty() || !currRepo.stageRemoval.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        for (String file: plainFilenamesIn(CWD)) {
            if (!currBranch.nowBlops().containsKey(file)
                    && givCommit.nowBlops().containsKey(file)
                    && !currRepo.STAGEADD.containsKey(file)) {
                message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            } else {
                continue;
            }
        }
        if (currRepo.nameofCurrbranch.equals(branchName)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        String splitCommitid = splitPointfinder(currBranch, givCommit);
        File splitCommitname = join(COMMITFOLDER, splitCommitid);
        Commit splitCommit = readObject(splitCommitname, Commit.class);
        if (currRepo.leadingCommit.get(branchName).equals(splitCommitid)) {
            Utils.message("Given branch is ancestor of the current branch.");
            System.exit(0);
        }
        if (currRepo.leadingCommit.get(currRepo.nameofCurrbranch).equals(splitCommitid)) {
            checkoutBranch(headBranchId);
            Utils.message("Current branch fast-forwarded.");
            System.exit(0);
        }
        checkerone(currBranch, givCommit, splitCommit);
        chckertwo(currBranch, givCommit, splitCommit);
        chckerthree(currBranch, givCommit, splitCommit);
        String messageforCommit = "Merged " + branchName + " into "
                + currRepo.nameofCurrbranch + ".";
        Repository.commit(messageforCommit);
    }
}
