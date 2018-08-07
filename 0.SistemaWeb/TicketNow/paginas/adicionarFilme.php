<html>
<head>
<meta charset="utf-8">
<title>Adicionando um novo dilme</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script type="text/javascript" src="../js/post.js"></script>
</head>

<body>
<div class="conteudo">	
	<div class="base-central">	
			
		<?php include "cabecalho.php"?>
        
        <div class="base-home">
            
		<h1 class="titulo"><span class="cor">Adicionando</span> filme</h1>
            <div class="base-formulario">	
                <form name="form_compra" id="form_compra" onsubmit="return false;">
                    <label>Titulo</label>
                        <input name="txt_titulo" value="" type="text" placeholder="EX: Titanic">

                    <label>Descrição</label>
                        <input name="txt_descricao" value="" type="text" placeholder="EX: O filme conta a história de um casal que se matam no final">

                    <div class="col">
                        <label>Gênero</label>
                            <input name="txt_genero" value="" type="text" placeholder="EX: Aventura">	
                    </div>

                     <div class="col">
                        <label>Diretor</label>
                            <input name="txt_diretor" value="" type="text" placeholder="EX: Silvio Abravanel">	
                    </div>
                    
                    <div class="col"> 
                        <label>Ano</label>
                            <input maxlength="4" name="txt_ano" value="" type="text" placeholder="EX: 2018">
                    </div>

                    <div class="col">
                        <label>Avaliação</label>
                            <input maxlength="4" name="txt_avaliacao" value="" type="text" placeholder="EX: 4.95">
                    </div>

                    <label>Elenco</label>
                            <input name="txt_elenco" value="" type="text" placeholder="EX: Michael Jackson, Selton Mello, Valesca Popozuda">	

                    <button class="btn" type="submit" id="btnAdicionarFilme" onclick="ajaxPost('../scripts/adicionarFilme.php', '#resultado-adicao')"> Adicionar Filme</button>

                </form>
                <div id="resultado-adicao"></div>
            </div>	



		</div>
        
        <?php include "rodape.php"?>
	
    </div>		
</div>		
</body>
</html>