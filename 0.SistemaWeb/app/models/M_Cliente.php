<?php

namespace app\models;
use app\core\Model;

class M_Cliente extends Model{
    public function __construct() {
        parent::__construct();
    }
    
    public function lista(){
        $sql = "SELECT * FROM  cliente ";
        $qry = $this->db->query($sql);
        
        return $qry->fetchAll();
    }
}
