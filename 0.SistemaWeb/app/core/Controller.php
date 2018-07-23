<?php
namespace app\core;

class Controller{
     public function load($viewName, $viewDados=array()){
       extract($viewDados); 
       include "app/views/" . $viewName .".php";
   }
}
