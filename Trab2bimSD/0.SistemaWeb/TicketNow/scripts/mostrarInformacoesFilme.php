<?php

namespace TicketNow;
require_once "../Database/logicas/LogicaFactory.php";

$oLogicaFactory = LogicaFactory::getInstance();
$oFilme = $oLogicaFactory->criarLogica("filme");

print_r($oFilme->getFilmeByTitulo("Titanic"));


?>