<?php

require_once __DIR__ . '/include/DbHandler.php';

	/**
 * Creating new task in db
 * method POST
 * params - name
 * url - /tasks/
 */
if (isset($_POST['username'])) {
    
    $response = array();
    $username = $_POST['username'];
    $db = new DbHandler();
    
    $user = $db->getUserByEmail($username);
    if($user != null) {
        if(isset($_POST['updatedDate']) && $_POST['updatedDate'] != "") {
            $updatedDate = $_POST['updatedDate'];
            $result = $db->getTasksByUserIdAndUpdatedDate($user["userId"], $updatedDate);
        } else {
            $result = $db->getTasksByUserId($user["userId"]);    
        }
    
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
    }else {
        $response["error"] = true;
        $response["message"] = "Failed to get task detail. Please try again";
    }
    // echo json response
    echo json_encode($response);
}
?>