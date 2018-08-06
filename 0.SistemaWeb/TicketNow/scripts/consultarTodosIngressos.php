
<?php

    require_once "ClienteWS.php";

    $clienteWS = new ClienteWS();

    $assentosDisponiveis = $clienteWS->consultarTodosAssentos();
    //$clienteWS->comprarAssento(10,"C", "78", "2020", "150");
  
    $assentosDisponiveis = explode(":", $assentosDisponiveis);

  
    $matriz = array();
    
    for($linha = 0; $linha < 10; $linha++) {
      for($coluna = 0; $coluna < 5; $coluna++) {
        $matriz[$linha][$coluna] = 0;
      }
    }


    for($i = 0; $i < 10; $i += 1) {
      foreach($assentosDisponiveis as $assentoLivre) {
        
        if($i == 9) {
          if($assentoLivre[0] == 1 & $assentoLivre[2] == 'A')
            $matriz[$i][0] = 1;
          
          if($assentoLivre[0] == 1 & $assentoLivre[2] == 'B')
            $matriz[$i][1] = 1;

          if($assentoLivre[0] == 1 & $assentoLivre[2] == 'C')
            $matriz[$i][2] = 1;

          if($assentoLivre[0] == 1 & $assentoLivre[2] == 'D')
            $matriz[$i][3] = 1;

          if($assentoLivre[0] == 1 & $assentoLivre[2] == 'E')
            $matriz[$i][4] = 1;
        }
          

        if($assentoLivre[0] == $i+1 & $assentoLivre[1] == 'A')
          $matriz[$i][0] = 1;
        
        if($assentoLivre[0] == $i+1 & $assentoLivre[1] == 'B')
          $matriz[$i][1] = 1;

        if($assentoLivre[0] == $i+1 & $assentoLivre[1] == 'C')
          $matriz[$i][2] = 1;

        if($assentoLivre[0] == $i+1 & $assentoLivre[1] == 'D')
          $matriz[$i][3] = 1;

        if($assentoLivre[0] == $i+1 & $assentoLivre[1] == 'E')
          $matriz[$i][4] = 1;

      }
    }



    //print_r($matriz);

  echo "<table border='1'>

      <th> </th>
      <th>A</th>
      <th>B</th>
      <th>C</th>
      <th>D</th>
      <th>E</th>

      </tr>";
      $i = 1;
      for($linha = 0; $linha < 10; $linha += 1) {
        echo "<tr>";
        echo "<td width='10%'>" . $i . "</td>";
        for($coluna = 0; $coluna < 5; $coluna += 1) {
          if($matriz[$linha][$coluna] == 1)
            echo "<td bgcolor='#00FF00' width='10%'></td>";
          else
            echo "<td bgcolor='#FF0000' width='10%'></td>";
        }
        echo "</tr>";
        $i++;
      }


  echo "</table>";

?>
