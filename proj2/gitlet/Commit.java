package gitlet;
import java.io.File;
import java.util.HashMap;


import java.io.Serializable;
import java.util.Date;

import static gitlet.Repository.COMMITFOLDER;
import static gitlet.Utils.join;
import static gitlet.Utils.readObject;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author Brandon Nguyen
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    protected String message;
    protected Date timestamp; // change to Date?
    protected String prev;
    protected String parent;
    protected HashMap<String, String> blops;
    protected String branch;

    public HashMap<String, String> nowBlops() {
        return this.blops;
    }
    public String getBranch() {
        return this.branch;
    }
    public Commit(String message, String prev, String parent, String branch) {
        this.parent = parent;
        this.prev = prev;
        this.message = message;
        this.branch = branch;
        this.timestamp = new Date();
        this.blops = parentblops();
    }
    public HashMap<String, String> parentblops() {
        if (this.message.equals("initial commit")) {
            return new HashMap<String, String>();
        } else {
            File parentcomfile = join(COMMITFOLDER, parent);
            Commit parentCommit = readObject(parentcomfile, Commit.class);
            return parentCommit.blops; // return the blops HashMap of the parent
        }
    }
}
