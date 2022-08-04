<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>


<html>
	<body style="font-size: 1in;" >
		<c:if test="${not empty errors}">
			<ul style="color: red">
				<c:forEach items="${errors}" var="error">
					<li>${error}
				</c:forEach>
			</ul>
		</c:if>
		
	<form action="${selectedUrl}" method="post">
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<div>Game</div><br> 
		<select style="font-size: 1in" name="gameName">
			<c:forEach items="${gameNames}" var="game">
				<option  value="${game.name}">${game.name}</option>
			</c:forEach>
		</select>
		<br>
		<input style="font-size: 1in" type="submit" name="selectGame" value="Select">	
	</form>
	</body>
</html>
