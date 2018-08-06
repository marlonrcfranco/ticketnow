<html>
<head>
<meta charset="utf-8">
<title>Fique por dentro</title>
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
            
		<h1 class="titulo"><span class="cor">Fique por dentro</span> </h1>
           Quando o filme <?php echo $_GET["filme"];?> estiver disponível, te mandaremos um e-mail
            <div class="base-formulario">	
                <form name="form_compra" id="form_compra" onsubmit="return false;">

                    <label>Nome</label>
                        <input name="txt_nome" value="" type="text" placeholder="Insira um nome">

                    <label>Email</label>
                        <input name="txt_email" value="" type="text" placeholder="Insira um email">

                    <button class="btn" type="submit" id="btnAvisar" onclick="ajaxPost('../scripts/inscreverNoFeed.php', '#resultado-aviso')"> Avise-me</button>

                </form>
                <div id="resultado-aviso"></div>
            </div>	



		</div>
        
        <?php include "rodape.php"?>
	
    </div>		
</div>		
</body>
</html>