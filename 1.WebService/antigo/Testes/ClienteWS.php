<?php
    

    // identificar se o cliente quer comprar ou consultar

    /*
    $numeroAssento;
    $letraFileira;

    se ( comprar ) {
        $codCartao;
        $dataVencimento;
        $digitoVerificador;
    }
    */

    $numeroAssento = 10;
    $letraFileira = "A";
    $codCartao = "7000000000000000";
    $dataVencimento = "2720";
    $digitoVerificador = "130";

    $portaWebService = "9997";
    //$portaWebService = getPortaWebService();

    $wsdl   = "http://127.0.0.1:$portaWebService/ticketnow?wsdl";
    $clienteWS = new SoapClient($wsdl, array('trace'=>1));  // The trace param will show you errors stack
    $GLOBALS["clienteWS"] = $clienteWS;

    print_r(var_dump($clienteWS->__getFunctions()));
    comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador);

    function comprarAssento($numeroAssento, $letraFileira, $codCartao, $dataVencimento, $digitoVerificador) {
        echo "Comprando assento";

        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;
        $param["codCartao"] = $codCartao;
        $param["dataVencimento"] = $dataVencimento;
        $param["digitoVerificador"] = $digitoVerificador;

        $parametros = array(
            "numeroAssento" => $numeroAssento,
            "letraFileira" => $letraFileira,
            "codCartao" => $codCartao,
            "dataVencimento" => $dataVencimento,
            "digitoVerificador" => $digitoVerificador    
        );

        echo $GLOBALS["clienteWS"]->comprarIngresso($parametros);

    }

    function consultarAssento($numeroAssento, $letraFileira) {
        echo "Consultando assento";

        $param = array();
        $param["numeroAssento"] = $numeroAssento;
        $param["letraFileira"] = $letraFileira;

        $GLOBALS["clienteWS"]->consultarAssento($param);

        return $resultadoConsulta;
    }


    
 ?>

