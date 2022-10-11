<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>


<html>
	<head>
		<link rel="stylesheet" href="basic.css">
	</head>
	<body>
		<c:if test="${not empty errors}">
			<ul class="error">
				<c:forEach items="${errors}" var="error">
					<li>${error}
				</c:forEach>
			</ul>
		</c:if>
		
	<form action="${selectedUrl}" method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<div>Game</div><br> 
		<select name="gameName">
			<c:forEach items="${gameNames}" var="game">
				<option  value="${game.name}">${game.name}</option>
			</c:forEach>
		</select>
		<br>
		<input type="submit" name="selectGame" value="Select">	
	</form>
	</body>
</html>
