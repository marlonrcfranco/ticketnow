<?php namespace TicketNow;?>
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

        <div class="titulo-filme"><h1>Titanic</h1></div>

        <div class="coluna-esquerda">
            <div class"capa-filme">
                <img src="../img/filme_titanic.jpg">
            </div>
        </div>

        <div class="coluna-direita">
                <div class="titulo-descricao"><h2>Leia a Sinopse<h2></div>
                <p>Jack Dawson (Leonardo DiCaprio) é um jovem aventureiro que, na mesa de jogo, ganha uma passagem para a primeira viagem do transatlântico Titanic. Trata-se de um luxuoso e imponente navio, anunciado na época como inafundável, que parte para os Estados Unidos. Nele está também Rose DeWitt Bukater (Kate Winslet), a jovem noiva de Caledon Hockley (Billy Zane). Rose está descontente com sua vida, já que sente-se sufocada pelos costumes da elite e não ama Caledon. Entretanto, ela precisa se casar com ele para manter o bom nome da família, que está falida. Um dia, desesperada, Rose ameaça se atirar do Titanic, mas Jack consegue demovê-la da ideia. Pelo ato ele é convidado a jantar na primeira classe, onde começa a se tornar mais próximo de Rose. Logo eles se apaixonam, despertando a fúria de Caledon. A situação fica ainda mais complicada quando o Titanic se choca com um iceberg, provocando algo que ninguém imaginava ser possível: o naufrágio do navio.</p>
                <br>
                <div class="titulo-descricao"><h2>Elenco<h2></div>
                <p><b>Leonardo DiCaprio:</b> Jack Dawson</p>
                <p><b>Kate Winslet:</b> Rose Bukater</p>
                <p><b>Billy Zane:</b> Cal Hockley</p>
                <p><b>Kathy Bates:</b> Molly Brown</p>

                <br>
                <div align="center" class="coluna-direita-dir">
                    <a class="btn" href="comprar.php?filme=Titanic">Comprar Ingresso</a>
                </div>
                <div align="center" class="coluna-direita-esq">
                    <button class="btn" type="submit" id="btnConsultarTodosIngressos" onclick="ajaxPost('../scripts/consultarTodosIngressos.php', '#resultado-consulta')"> Ver assentos disponíveis </button>
                </div>
                    
            <div id="resultado-consulta"></div>
            <div id="resultado-informacoes"></div>
            </div>
        </div>

        

		
        

	    <?php include "rodape.php"?>
    </div>		
    
</div>		
</body>
</html>