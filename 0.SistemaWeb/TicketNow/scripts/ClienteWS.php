<?php
    
    class ClienteWS {
        private $clienteWS;

        public function __construct() {
            $this->carregarConfiguracoesWebService();
            
            $this->clienteWS = new SoapClient("http://".$GLOBALS["ipServidor"].":".$GLOBALS["portaServidor"]."/ticketnowws?wsdl");
            echo "Cliente do WS conectado<br>";
        }

        private function carregarConfiguracoesWebService() {
            $GLOBALS["portaServidor"] = 56000;
            $GLOBALS["ipServidor"] = "localhost";   
        }

        public function consultarTodosAssentos() {
            return $this->clienteWS->consultarTudosAssentos();
        }

        public function comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador) {                
            return $this->clienteWS->comprarIngresso($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);
        }

        public function consultarAssento($numeroAssento, $letraFileira) {
            return$this->clienteWS->consultarAssento($numeroAssento, $letraFileira);
        }

    }



 ?>

