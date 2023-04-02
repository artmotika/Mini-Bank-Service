# software-transactional-memory-artmotika  
software-transactional-memory-artmotika created by GitHub Classroom  

Приложение Банк поддерживает методы:  

create account - создают аккаунт, не ожидая авторизации  
  POST method /reg/{login}
  и header("password", base64password)

get account - выводит данные аккаунта, ожидая авторизации  
  GET method /get/account/{login}
  и header("password", base64password)

get balance - выводит баланс аккаунта, ожидая авторизации   
  GET method /get/balance/{login}
  и header("password", base64password)

udate account - меняет логин и пароль аккаунта на новые, ожидая авторизации  
  PUT method /update/account/{login}/{newLogin}
  и header("password", base64password) и и header("newPassword", base64newPassword)

transfer - переводит деньги с аккаунта на другой аккаунт, ожидая авторизации   
  PUT method transfer/{login}/{to}/{amount}  
  и header("password", base64password)

take credit - берет кредит в банке, ожидая авторизации   
  PUT method takecredit/{login}/{amount}  
  и header("password", base64password)

withdraw credit - снимает деньги, ожидая авторизации   
  PUT method withdrawcredit/{login}/{amount}  
  и header("password", base64password)
