<html>
<head>
<meta charset="utf-8">
<title>Comprar Ingresso</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" type="text/css" href="../css/style.css">
</head>

<body>
<div class="conteudo">	
	<div class="base-central">	
			
		<?php include "cabecalho.php"?>
        
        <div class="base-home">
            
		<h1 class="titulo"><span class="cor">Novo</span> cadastro</h1>
            <div class="base-formulario">	
                <form action="#" method="POST">
                    <label>Nome</label>
                        <input name="txt_nome" value="" type="text" placeholder="Insira umm nome">
                    <label>Email</label>
                        <input name="txt_email" value="" type="text" placeholder="Insira um email">
                    <label>Endereço</label>
                        <input name="txt_endereco" value="" type="text" placeholder="Insira seu endereço">	
                    <div class="col">
                        <label>Telefone</label>
                        <input name="txt_fone" value="" type="text" placeholder="Insira seu telefone">
                    </div>	

                    <div class="col">
                        <label>Bairro</label>
                        <input name="txt_fone" value="" type="text" placeholder="Insira seu bairro">
                    </div>

                    <div class="col">
                        <label>CEP</label>
                        <input name="txt_fone" value="" type="text" placeholder="Insira seu CEP">
                    </div>	

                    <div class="col">
                        <label>CPF</label>
                        <input name="txt_fone" value="" type="text" placeholder="Insira seu CPF">
                    </div>

                    <input type="hidden" name="acao" value="Cadastrar">
                    <input type="hidden" name="id" value="">
                    <input type="submit" value="Cadastrar" class="btn">
                    <input type="reset" name="Reset" id="button" value="Limpar" class="btn limpar">
                </form>
            </div>	



		</div>
        
        <?php include "rodape.php"?>
	
    </div>		
</div>		
</body>
</html>