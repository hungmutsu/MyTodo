<?php

require_once __DIR__ . '/include/DbHandler.php';

	/**
 * Creating new task in db
 * method POST
 * params - name
 * url - /tasks/
 */
if (isset($_POST['userId']) && isset($_POST['name']) && isset($_POST['description']) && isset($_POST['reminderDate'])) {
    
    $response = array();
    $userId = $_POST['userId'];
    $name = $_POST['name'];
    $description = $_POST['description'];
    $reminderDate = $_POST['reminderDate'];

    $db = new DbHandler();

    // creating new task
    $taskId = $db->createTask($userId, $name, $description, $reminderDate);

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
    // echo json response
    echo json_encode($response);
}
?>