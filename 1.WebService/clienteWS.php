<?php
    
    echo "<h1>Exemplo de Cliente do Serviço WEB</h1><br>";

    $numeroAssento = 5;
    $letraFileira = "E";
    $codCartao = "7000000000000000";
    $dataVencimento = "2720";
    $digitoVerificador = "130";


    carregarConfiguracoesWebService();

    comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);
    //consultarTodosAssentos();

    function carregarConfiguracoesWebService() {
        $GLOBALS["portaServidor"] = 56000;
        $GLOBALS["ipServidor"] = "localhost";
    }

    function consultarTodosAssentos() {
        echo "<b>Consultando todos os assento</b><br><br>";
        $clienteWS = new SoapClient("http://".$GLOBALS["ipServidor"].":".$GLOBALS["portaServidor"]."/ticketnowws?wsdl");

        $aux = $clienteWS->consultarTudosAssentos();
        echo $aux;
    }

    function comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador) {
        echo "<b>Comprando assento ($numeroAssento,$letraFileira)</b><br><br>";
        $clienteWS = new SoapClient("http://".$GLOBALS["ipServidor"].":".$GLOBALS["portaServidor"]."/ticketnowws?wsdl");

        $aux = $clienteWS->comprarIngresso($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);
        return $aux;
    }

    function consultarAssento($numeroAssento, $letraFileira) {
        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;

        $resultadoConsulta = $clienteWS->consultarAssento($param);

        return $resultadoConsulta;
    }
    
 ?>

