<?php

class InscritoObserver {
    public $email;
    public $nome;

    public function __construct($nome, $email) {
        $this->nome = $nome;
        $this->email = $email;
    }
    
    public function update(string $titulo) {
        print_r("Enviando email: O filme ". $titulo." chegou!");
    }
}

?>