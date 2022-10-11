<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>


<html>
	<head>
		<link rel="stylesheet" href="basic.css">
	</head>
	<body>
		<table>
		<tr><td>Name</td><td>Bean Name</td><td>value</td></tr>
			<c:forEach items="${devices}" var="device">
				<tr>
					<td>${device.name}</td>
					<td>${device.beanName}</td>
					<td>${device.value}</td>
				</tr>
			</c:forEach>
		</table>		
	</body>
</html>
