<?php

namespace app\models;
use app\core\Model;

class M_ConsultaAssento extends Model{
    public function __construct() {
        parent::__construct();
    }

        public function mostrarAssentosDisponiveis(){
        $sql = "SELECT * FROM  (nome_da_tabela) ";
        $qry = $this->db->query($sql);
        
        return $qry->fetchAll();
    }

}
