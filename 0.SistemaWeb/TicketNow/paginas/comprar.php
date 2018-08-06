<html>
<head>
<meta charset="utf-8">
<title>Comprar Ingresso</title>
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
            
		<h1 class="titulo"><span class="cor">Comprando</span> ingresso</h1>
           <h2><?php echo $_GET["filme"];?></h2>
            <div class="base-formulario">	
                <form name="form_compra" id="form_compra" onsubmit="return false;">
                    <label>Numero do Assento</label>
                        <input style="width: 65px;" maxlength="4" name="txt_assento" value="" type="text" placeholder="EX: 10,A">

                    <label>Nome</label>
                        <input name="txt_nome" value="" type="text" placeholder="Insira um nome">

                    <label>Email</label>
                        <input name="txt_email" value="" type="text" placeholder="Insira um email">

                    <label>Cartao de Crédito</label>
                        <input maxlength="16" name="txt_numero_cartao" value="" type="text" placeholder="Insira o número do seu cartão">	
                    
                    <div class="col"> 
                        <label>Validade</label>
                            <input maxlength="4" name="txt_validade_cartao" value="" type="text" placeholder="Insira a validade do cartão">
                    </div>

                    <div class="col">
                        <label>Digito</label>
                            <input maxlength="3" name="txt_digito_cartao" value="" type="text" placeholder="Insira o digito verificador">
                    </div>
                    <button class="btn" type="submit" id="btnComprarIngresso" onclick="ajaxPost('../scripts/comprarIngresso.php', '#resultado-compra')"> Comprar Ingresso</button>

                </form>
                <div id="resultado-compra"></div>
            </div>	



		</div>
        
        <?php include "rodape.php"?>
	
    </div>		
</div>		
</body>
</html>