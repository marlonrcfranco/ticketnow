<?php

include_once 'lFilmeSubscriber.php';

function writeln($line_in) {
    echo $line_in."<br/>";
}

class Subscriber {
    public $email;
    public function __construct(string $email) {
        $this->email = $email;
    }
    public function update(string $titulo) {
      print_r("Enviando email: O filme ". $titulo." chegou!");
    }
}

class Filme {
    private $favoritePatterns = NULL;
    private $observers = array();
    public $titulo;
    function __construct(string $titulo) {
        $this->titulo = $titulo;
    }
    function attach(Subscriber $observer_in) {
      $oFilmeSubscriber = new lFilmeSubscriber();
      $oFilmeSubscriber->titulo = $this->titulo;
      $oFilmeSubscriber->email = $observer_in->email;
      $oFilmeSubscriber->insertFilmeSubscriber();
    }
    function detach(Subscriber $observer_in) {
      $oFilmeSubscriber = new lFilmeSubscriber();
      $oFilmeSubscriber = $oFilmeSubscriber->getFilmeSubscriberByEmail($observer_in->email)[0];
      $oFilmeSubscriber->excluirFilmeSubscriber($oFilmeSubscriber->codigo);
    }
    function notify() {
      $oFilmeSubscriber = new lFilmeSubscriber();
      $ListaFilmeSubscriber = $oFilmeSubscriber->getFilmeSubscriberByTitulo($this->titulo);
      foreach($ListaFilmeSubscriber as $obs) {
          $observer = new Subscriber($obs->email);
          $observer->update($this->titulo);
      }
    }
}

?>