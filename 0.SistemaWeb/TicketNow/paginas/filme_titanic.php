<html>
<head>
<meta charset="utf-8">
<title>mjailton</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" type="text/css" href="../css/style.css">
<link rel="stylesheet" type="text/css" href="../css/filme.css">
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
            <h3>Titanic: O Retorno de Jack</h3>

            <form action="../scripts/consultarTodosIngressos.php" method="POST">
                    <label>Nome</label>
                    <input type="submit" name="btnConsultarTodosIngressos" id="button" value="Consultar Ingressos" class="btn">
            </form>
        </div>

        

		
        

	    <?php include "rodape.php"?>
    </div>		
    
</div>		
</body>
</html>