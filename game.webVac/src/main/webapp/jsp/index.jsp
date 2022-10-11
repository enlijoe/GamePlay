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
		<a href="/calibrate">Calibrate Water</a><br>
		<A href="/run">Run a Game</A><BR>
		<A href="/modify">Modify a Game</A><BR>
		<A href="/copy">Copy a Game</A><BR>
		<A href="/delete">Delete a Game</A><BR>
		<A href="/new">New Game</A><BR>
		<A href="/pressure">Pressure Sensors</A><BR>
		<A href="/resetDB">Reset DB</A><BR>
	</body>
</html>
