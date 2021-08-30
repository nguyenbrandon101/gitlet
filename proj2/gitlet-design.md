# Gitlet Design Document

**Name**: Brandon

## Classes and Data Structures
### Persistence
#### We set up a persistence called Repository
* Here we set a persistence for stage Add and stage removal which are hashmaps in which they key and values of these are "Hello.txt" and the sha1 of the contents
* we also set a persistence of the Head pointer in which it will tell us where the head commit is at
* the leading commit will be a hash map in which the key will be the head pointer and the value will be the sha1 of head commit contents 
* we also created methods called grabrepo and sendbackrepo in which they will readObject and writeobject of that persistence
* to call each persistence we will just need to call the grabrepo method in which we can then modify the methods within this persistence.

### Class Respitory
#### Init
* makes initial commit 
* Make  Staging folder
* Make a commit folder 
    * inside has a master branch hashmap where the key is nameOfcurrbranch and the value is the sha1 of the commit contents
    * Commit:
        1. Each is a file in which it is named by the sha1 of the commit contents
        2. Inside the commit is the metadata and a hasmap
        3. The hashmap inside contains the key = String file txt and value = sha1 of the commit contents

* Make a blobs folder
    * each blob is a file in which the name of the file is the sha1 contents of the txt file.
    * inside the file is the file contents as a byte array

#### Add
* Checks if the file exist or not
* Checks if file is in the stage remove area, if is, removes it
* Checks if the master pointer is pointing at a initial commit, if not, check the parents blops and see of its the same contents as the current blops.
    * if it is: system.out(0)
* checks if the file is in the stage add area or not

#### Commit
* Clones the parent commit
* checks the staging add area and see if the file contains the same contents or not
* checks the staging remove area and removes the file

#### Log
* Starting at the head commit, displays the information 
    * displays:
        * commit sha1
        * the date
        * the commit msg
    

### Checkout
    ## checkout file name 
        * using the head commit, puts it in working directory and overwriting its older version.
    ## checkout commit id, file name
        * using the commit id, we check that commit instead of the head commit and put it in the working directory

### Global-log
* iterate through each commit and prints out the sha1 of the commit, date, and message.

### Find 
* iterate through the commit folder and see if the argument message is the same in any of the commits.
* has a tracker to see if any of the commits has the same message.
* if the tracker is 0 that means no commit has that message in which we will print out the message.

### rm
* Checks the stage add area and see if the file exists, if it does remove it 
* then we would put it into the stage remove area
* Check if the commit has the file name, if it does, put it into the stage remov area
* checks the CWD if the file exists in there, if it does, we delete that too.
### Class Commit
#### Commit
* a commit constructor 
* each instance has a msg, unique id, time stamp, and a parent pointer
* has a hash map that contains the file name and also the blops it belongs to




