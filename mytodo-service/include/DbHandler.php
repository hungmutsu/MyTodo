<?php

/**
 * Class to handle all db operations
 * This class will have CRUD methods for database tables
 *
 * @author Ravi Tamada
 * @link URL Tutorial link
 */
class DbHandler {

    private $conn;

    function __construct() {
        require_once dirname(__FILE__) . '/DbConnect.php';
        // opening db connection
        $db = new DbConnect();
        $this->conn = $db->connect();
    }

    /* ------------- `users` table method ------------------ */

    /**
     * Creating new user
     * @param String $name User full name
     * @param String $email User login email id
     * @param String $password User login password
     */
    public function createUser($username, $password, $name) {
        $response = array();

        // First check if user already existed in db
        if (!$this->isUserExists($username)) {
           
            // insert query
            $stmt = $this->conn->prepare("INSERT INTO users(username, password, fullname) values(?, ?, ?)");
            $stmt->bind_param($username, $password, $name);

            $result = $stmt->execute();

            $stmt->close();

            // Check for successful insertion
            if ($result) {
                // User successfully inserted
                return USER_CREATED_SUCCESSFULLY;
            } else {
                // Failed to create user
                return USER_CREATE_FAILED;
            }
        } else {
            // User with same email already existed in the db
            return USER_ALREADY_EXISTED;
        }

        return $response;
    }

    /**
     * Checking user login
     * @param String $email User login email id
     * @param String $password User login password
     * @return boolean User login status success/fail
     */
    public function checkLogin($email, $password) {
        // fetching user by email
        $stmt = $this->conn->prepare("SELECT userId FROM users WHERE username = ? and password = ?");

        $stmt->bind_param("ss", $email, $password);

        $stmt->execute();

        $stmt->store_result();

        if ($stmt->num_rows > 0) {
            // Found user with the email
            // Now verify the password

            $stmt->fetch();

            $stmt->close();

            return TRUE;
        } else {
            $stmt->close();

            // user not existed with the email
            return FALSE;
        }
    }

    /**
     * Checking for duplicate user by email address
     * @param String $email email to check in db
     * @return boolean
     */
    private function isUserExists($email) {
        $stmt = $this->conn->prepare("SELECT userId from users WHERE username = ?");
        $stmt->bind_param("s", $email);
        $stmt->execute();
        $stmt->store_result();
        $num_rows = $stmt->num_rows;
        $stmt->close();
        return $num_rows > 0;
    }

    /**
     * Fetching user by email
     * @param String $email User email id
     */
    public function getUserByEmail($email) {
        $stmt = $this->conn->prepare("SELECT userId, username, password, fullname FROM users WHERE username = ?");
        $stmt->bind_param("s", $email);
        if ($stmt->execute()) {
            // $user = $stmt->get_result()->fetch_assoc();
            $stmt->bind_result($uid, $username, $password, $fullname);
            $stmt->fetch();
            $user = array();
            $user["userId"] = $uid;
            $user["username"] = $username;
            $user["password"] = $password;
            $user["fullname"] = $fullname;
            $stmt->close();
            return $user;
        } else {
            return NULL;
        }
    }

   
    /* ------------- `tasks` table method ------------------ */

    /**
     * Creating new task
     * @param String $user_id user id to whom task belongs to
     * @param String $task task text
     */
    public function createTask($user_id, $name, $description, $reminderDate, $createdDate, $updatedDate) {
        $dateTime = new DateTime("now");
        if($createdDate == NULL || $createdDate == "") {
            $createdDate = $dateTime->format("Y-m-d H:i:s");    
        }
        if($updatedDate == NULL)
        {
            $updatedDate = $dateTime->format("Y-m-d H:i:s");
        }
        
        $stmt = $this->conn->prepare("INSERT INTO tasks(userId, name, description, reminderDate, createdDate,updatedDate) VALUES(?,?,?,?,?,?)");
        $stmt->bind_param("isssss", $user_id, $name, $description, $reminderDate, $createdDate, $updatedDate);
        $result = $stmt->execute();
        $stmt->close();

        if ($result) {
            // task row created
            return $this->conn->insert_id;
        } else {
            // task failed to create
            return NULL;
        }
    }

    /**
     * Fetching single task
     * @param String $task_id id of the task
     */
    public function getTask($task_id) {
        $stmt = $this->conn->prepare("SELECT taskId, userId, name, description, reminderDate, createdDate, updatedDate FROM tasks WHERE taskId = ?");
        $stmt->bind_param("i", $task_id);
        if ($stmt->execute()) {
            $res = array();
            $stmt->bind_result($tid, $uid, $name, $description, $reminderDate, $createDate, $updateDate);
            // TODO
            // $task = $stmt->get_result()->fetch_assoc();
            $stmt->fetch();
            $res["taskId"] = $tid;
            $res["userId"] = $uid;
            $res["name"] = $name;
            $res["description"] = $description;
            $res["reminderDate"] = $reminderDate;
            $res["createdDate"] = $createDate;
            $res["updatedDate"] = $updateDate;
            $stmt->close();
            return $res;
        } else {
            return NULL;
        }
    }

    /**
     * Fetching all user tasks
     * @param String $userId id of the user
     */
    public function getTasksByUserId($userId) {
        $stmt = $this->conn->prepare("SELECT * FROM tasks WHERE userId = ?");
        $stmt->bind_param("i", $userId);
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }

    public function getTasksByUserIdAndUpdatedDate ($userId, $updatedDate) {
        if($updatedDate == NULL || $updatedDate == ""){
            $updatedDate = new DateTime("now");
        } else {
            $updatedDate = new DateTime($updatedDate);
        }
        
        $update = $updatedDate->format("Y-m-d H:i:s");
        $stmt = $this->conn->prepare("SELECT * FROM tasks WHERE userId = ? and updatedDate > ?");
        $stmt->bind_param("is", $userId, $update);
        $stmt->execute();
        $tasks = $stmt->get_result();
        $stmt->close();
        return $tasks;
    }
    
    /**
     * Updating task
     * @param String $task_id id of the task
     * @param String $task task text
     * @param String $status task status
     */
    public function updateTask($task_id, $name, $description, $reminderDate, $updatedDate ) {
        
        if($updatedDate == NULL || $updatedDate == ""){
            $updatedDate = new DateTime("now");
        } else {
            $updatedDate = new DateTime($updatedDate);
        }
        if($reminderDate == NULL || $reminderDate == "") {
            $reminderDate = new DateTime("now");
        } else {
            $reminderDate = new DateTime($reminderDate);
        }
        
        $reminderDateFormat = $reminderDate->format("Y-m-d H:i:s");
        $updatedDateFormat = $updatedDate->format("Y-m-d H:i:s");
        
        $stmt = $this->conn->prepare("UPDATE tasks SET name = ?, description = ?, reminderDate = ? and updatedDate = ? WHERE taskId = ?");
        $stmt->bind_param("ssssi", $name, $description, $reminderDateFormat, $updatedDateFormat, $task_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }

    /**
     * Deleting a task
     * @param String $task_id id of the task to delete
     */
    public function deleteTask($task_id) {
        $stmt = $this->conn->prepare("DELETE FROM tasks taskId = ?");
        $stmt->bind_param("i", $task_id);
        $stmt->execute();
        $num_affected_rows = $stmt->affected_rows;
        $stmt->close();
        return $num_affected_rows > 0;
    }
}

?>
