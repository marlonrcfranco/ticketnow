<?php

include_once 'lFilme.php';
include_once 'lFilmeSubscriber.php';
include_once 'lSubscriber.php';


class LogicaFactory {
    private static $instance = NULL;

    private function __construct() {}

    static function getInstance() {
        if(self::$instance == NULL)
            self::$instance = new LogicaFactory();

        return self::$instance;
    }


    public function criarLogica($nomeLogica) {
        if(strtolower($nomeLogica) == "filme")
            return new lFilme();
        
        if(strtolower($nomeLogica) == "filmesubscriber")
            return new lFilmeSubscriber();
        
        if(strtolower($nomeLogica) == "subscriber")
            return new lSubscriber();
    }

}


?>