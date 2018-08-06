<?php
$to      = 'v-lucena@outlook.com';
$subject = 'the subject';
$message = 'hello';
$headers = 'From: v-lucena@outlook.com' . "\r\n" .
    'Reply-To: v-lucena@outlook.com' . "\r\n" .
    'X-Mailer: PHP/' . phpversion();

;

// send email
if(mail($to, $subject, $message, $headers))
    echo "deu certo<br>";
else
    echo "deu errado<br>";
?>