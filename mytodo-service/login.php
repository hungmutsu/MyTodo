<?php

/**
 * @author GallerySoft.info
 * @copyright 2014
 */
 
require_once __DIR__ . '/include/DbHandler.php';



/**
 * User Login
 * url - /login
 * method - POST
 * params - email, password
 */
if (isset($_POST['username']) && isset($_POST['password'])) {
           
            // reading post params
            $email = $_POST['username'];
            $password = $_POST['password'];
            
            $response = array();

            $db = new DbHandler();
            // check for correct email and password
            if ($db->checkLogin($email, $password)) {
                // get the user by email
                $user = $db->getUserByEmail($email);

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
            } else {
                // user credentials are wrong
                $response['error'] = true;
                $response['message'] = 'Login failed. Incorrect credentials';
            }

            // echo json response
            echo json_encode($response);
        }