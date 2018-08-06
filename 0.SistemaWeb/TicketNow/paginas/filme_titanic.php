<html>
<head>
<meta charset="utf-8">
<title>mjailton</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/filme.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript" src="../js/post.js"></script>
</head>

<body>
<div class="conteudo">	
    <?php include "cabecalho.php"?>
   
	<div class="base-central">


        <div class="coluna-esquerda">
            <div class"capa-filme">
                <img src="../img/filme_titanic.jpg">
            </div>
        </div>

        <div class="coluna-direita">
                <?php
                    include_once "../../Database/logicas/LogicaFactory.php";
                    $oLogicaFactory = LogicaFactory::getInstance();
                    //$oFilme = $oLogicaFactory->criarLogica("filme");

                    //print_r($oFilme->getFilmeByTitulo("Teste, o Filme parte 1"));
                ?>
                <div class="col">
                    <a class="btn" href="comprar.php?filme=Titanic">Comprar Ingresso</a>
                </div>

                <div class="col">
				    <button class="btn" type="submit" id="btnConsultarTodosIngressos" onclick="ajaxPost('../scripts/consultarTodosIngressos.php', '#resultado-consulta')"> Consultar Ingressos </button>
                </div>

            <div id="resultado-consulta"></div>
        </div>

        

		
        

	    <?php include "rodape.php"?>
    </div>		
    
</div>		
</body>
</html>