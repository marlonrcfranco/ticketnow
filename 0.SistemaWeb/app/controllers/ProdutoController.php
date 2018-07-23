<?php
namespace app\controllers;
use app\core\Controller;

class ProdutoController extends Controller{
    
   public function index(){
       echo "Método index";
   } 
   
   public function ver(){
       $this->load("v_produto");
   }
   public function lista($valor=10){
       echo "<br>estou listando os PRODUTOS $valor<br>";
   } 
   
   
}
