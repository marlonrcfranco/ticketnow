<?php

include_once 'LogicaFactory.php';

echo "inicializando a fabrica<br>";
$oLogicaFactory = LogicaFactory::getInstance();

echo "Criando um lFilme<br>";
$lFilme = $oLogicaFactory->criarLogica("Filme");

echo "Criando nova tabela de Filmes";
$lFilme->createTableFilme();

?>