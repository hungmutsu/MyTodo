<?php

/**
 * @author GallerySoft.info
 * @copyright 2014
 */
 
require_once __DIR__ . '/include/DbHandler.php';



/**
 * ----------- METHODS WITHOUT AUTHENTICATION ---------------------------------
 */
/**
 * User Registration
 * url - /register
 * method - POST
 * params - name, email, password
 */
if (isset($_POST['username']) && isset($_POST['password']) && isset($_POST['name'])) {

    $response = array();

    // reading post params
    
    $username = $_POST['username'];
    $password = $_POST['password'];
    $name = $_POST['name'];

    $db = new DbHandler();
    $res = $db->createUser($username, $password, $name);

    if ($res == USER_CREATED_SUCCESSFULLY) {
    
    // get the user by email
            $user = $db->getUserByEmail($username);

            if ($user != NULL) {
                $response["error"] = false;
                
                 // user node
                $response["user"] = array();
    
                array_push($response["user"], $user);
                
            } else {
                // unknown error occurred
                $response['error'] = true;
                $response['message'] = "An error occurred. Please try again";
            }
                
    } else if ($res == USER_CREATE_FAILED) {
        $response["error"] = true;
        $response["message"] = "Oops! An error occurred while registereing";
    } else if ($res == USER_ALREADY_EXISTED) {
        $response["error"] = true;
        $response["message"] = "Sorry, this username already existed";
    }
    // echo json response
    echo json_encode($response);
}
