<?php

require_once __DIR__ . '/include/DbHandler.php';

	/**
 * Creating new task in db
 * method POST
 * params - name
 * url - /tasks/
 */
if (isset($_GET['userId'])) {
    
    $response = array();
    $userId = $_GET['userId'];

    $db = new DbHandler();

    $result = $db->getAllUserTasks($userId);
    if ($result != null) {
        $response["error"] = false;
         // task node
        $response["task"] = array();
        // looping through result and preparing tasks array
        while ($task = $result->fetch_assoc()) {
            array_push($response["task"], $task);
        }   
    } else {
        $response["error"] = true;
        $response["message"] = "Failed to get task detail. Please try again";
    }            
    // echo json response
    echo json_encode($response);
}
?>