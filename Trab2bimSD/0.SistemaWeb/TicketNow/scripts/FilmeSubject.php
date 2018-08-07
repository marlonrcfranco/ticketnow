<?php
    namespace TicketNow;

    include_once "../Database/logicas/lFilmeSubscriber.php";
    include_once "InscritoObserver.php";

    class FilmeSubject {
        private $favoritePatterns = NULL;
        private $observers = array();
        public $nomeFilme;

        function __construct($nomeFilme) {
            $this->nomeFilme = $nomeFilme;
        }

        function attach($observer_in) {
            $oFilmeSubscriber = new lFilmeSubscriber();
            $oFilmeSubscriber->titulo = $this->nomeFilme;
            $oFilmeSubscriber->email = $observer_in->email;
            $oFilmeSubscriber->insertFilmeSubscriber();
        }

        function detach($observer_in) {
            $oFilmeSubscriber = new lFilmeSubscriber();
            $oFilmeSubscriber = $oFilmeSubscriber->getFilmeSubscriberByEmail($observer_in->email)[0];
            $oFilmeSubscriber->excluirFilmeSubscriber($oFilmeSubscriber->codigo);
        }
        
        function notify() {
            $oFilmeSubscriber = new lFilmeSubscriber();
            $ListaFilmeSubscriber = $oFilmeSubscriber->getFilmeSubscriberByTitulo($this->nomeFilme);

            foreach($ListaFilmeSubscriber as $obs) {
                $observer = new InscritoObserver($obs->email);
                $observer->update($this->nomeFilme);
            }
        }
    }

?>