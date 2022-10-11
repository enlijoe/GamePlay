<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<html>
	<head>
		<link rel="stylesheet" href="basic.css">
	</head>
	<body>
		<div align="center">New Game</div>
		<form action="new" method="post">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			<input width="25" type="text" name="gameName">
			<br>
			Game Bean Name<br>
			<select name="gameBeanName">
				<c:forEach items="${gameBeanNames}" var="gameBeanName">
					<option name="${gameBeanName}">${gameBeanName}</option>
				</c:forEach>
			</select><br>
			<input type="submit" name="createGame" value="Create Game">
			<br>
		</form>
	</body>
</html>