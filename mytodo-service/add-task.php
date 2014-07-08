<?php

require_once __DIR__ . '/include/DbHandler.php';

	/**
 * Creating new task in db
 * method POST
 * params - name
 * url - /tasks/
 */
if (isset($_POST['username']) && isset($_POST['name']) && isset($_POST['description']) && isset($_POST['reminderDate'])
    && isset($_POST['createdDate']) && isset($_POST['updatedDate'])) {
    
    $response = array();
    $username = $_POST['username'];
    $name = $_POST['name'];
    $description = $_POST['description'];
    $reminderDate = $_POST['reminderDate'];
    $createdDate = $_POST['createdDate'];
    $updatedDate = $_POST['updatedDate'];
    
    $db = new DbHandler();

    $user = $db->getUserByEmail($username);
    if($user != null) {
            // creating new task
        $taskId = $db->createTask($user["userId"], $name, $description, $reminderDate, $createdDate, $updatedDate);
    
        if ($taskId != NULL) {
            $task = $db->getTask($taskId);
            $response["error"] = false;
             // task node
            $response["task"] = array();
            array_push($response["task"], $task);
            
        } else {
            $response["error"] = true;
            $response["message"] = "Failed to create task. Please try again";
        }
     } else {
            $response["error"] = true;
            $response["message"] = "Failed to create task. Please try again";
        }
                
    // echo json response
    echo json_encode($response);
}
?>