<?php

require_once __DIR__ . '/include/DbHandler.php';

	/**
 * Creating new task in db
 * method POST
 * params - name
 * url - /tasks/
 */
if (isset($_GET['taskId'])) {
    
    $response = array();
    $taskId = $_GET['taskId'];

    $db = new DbHandler();

    $task = $db->getTask($taskId);
    if ($task != null) {
        $response["error"] = false;
         // task node
        $response["task"] = array();
        array_push($response["task"], $task);    
    } else {
        $response["error"] = true;
        $response["message"] = "Failed to get task detail. Please try again";
    }            
    // echo json response
    echo json_encode($response);
}
?>