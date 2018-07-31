<?php
    $soap_cliente = new SoapClient("http://localhost:8080/WebService/ticketnow?wsdl");
    $param = array('num_fileira' => '1', 'num_corredor' => 'A');
    $result = $soap_cliente->consultar($param);
    if($result == 1)
        echo "Consultando preco";
 ?>

