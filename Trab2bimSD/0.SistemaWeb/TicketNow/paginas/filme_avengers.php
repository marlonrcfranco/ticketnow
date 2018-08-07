<?php namespace TicketNow;?>
<html>
<head>
<meta charset="utf-8">
<title>Avengers</title>
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

        <div class="titulo-filme"><h1>Avengers</h1></div>

        <div class="coluna-esquerda">
            <div class"capa-filme">
                <img src="../img/filme_avenger.jpg">
            </div>
        </div>

        <div class="coluna-direita">
                <div class="titulo-descricao"><h2>Leia a Sinopse<h2></div>
                <p>Loki (Tom Hiddleston) retorna à Terra enviado pelos chitauri, uma raça alienígena que pretende dominar os humanos. Com a promessa de que será o soberano do planeta, ele rouba o cubo cósmico dentro de instalações da S.H.I.E.L.D. e, com isso, adquire grandes poderes. Loki os usa para controlar o dr. Erik Selvig (Stellan Skarsgard) e Clint Barton/Gavião Arqueiro (Jeremy Renner), que passam a trabalhar para ele. No intuito de contê-los, Nick Fury (Samuel L. Jackson) convoca um grupo de pessoas com grandes habilidades, mas que jamais haviam trabalhado juntas: Tony Stark/Homem de Ferro (Robert Downey Jr.), Steve Rogers/Capitão América (Chris Evans), Thor (Chris Hemsworth), Bruce Banner/Hulk (Mark Ruffalo) e Natasha Romanoff/Viúva Negra (Scarlett Johansson). Só que, apesar do grande perigo que a Terra corre, não é tão simples assim conter o ego e os interesses de cada um deles para que possam agir em grupo.</p>
                <br>
                <div class="titulo-descricao"><h2>Elenco<h2></div>
                <p><b>Robert Downey, Jr.:</b> Tony Stark / Homem de Ferro:</p>
                <p><b>Chris Evans:</b> Steve Rogers / Capitão América</p>
                <p><b>Mark Ruffalo:</b> Bruce Banner / Hulk</p>
                <p><b>Chris Hemsworth:</b> Thor</p>

                <br>
                
                <a class="btn" href="avisarQuandoDisponivel.php?filme=Avenger">Avise-me quando estiver disponivel</a>
            
                <button class="btn" type="submit" id="btnDisponibilizar" onclick="ajaxPost('../scripts/disponibilizarFilme.php', '#resultado-diponibilizar')"> Disponibilizar o Filme</button>
                <div id="resultado-diponibilizar"></div>
        </div>


		
        

	    <?php include "rodape.php"?>
    </div>		
    
</div>		
</body>
</html>