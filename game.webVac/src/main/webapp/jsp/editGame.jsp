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
		<form action="editGame" method="post">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			<div align="center">
				Game ${gameModel.gameName} for ${gameModel.gameBeanName}
				<table>
					<c:forEach items="${gameModel.gameSetup}" var="setting">
						<tr>
							<td>${setting.name}</td>
							<td>
								
								<c:choose>
									<c:when test="${empty setting.listValues}">
										<input type="text" name="${setting.name}" value="${setting.currentValue}">
									</c:when> 
									<c:otherwise>
										<select name="${setting.name}">
											<option value="" <c:if test="${empty setting.currentValue}">selected="selected"</c:if> >None</option>
											<c:forEach items="${setting.listValues}" var="listValue">
												<option value="${listValue}" <c:if test="${listValue eq setting.currentValue}">selected="selected"</c:if> >${listValue}</option>
											</c:forEach>
										</select>
									</c:otherwise>
								</c:choose> 
							</td>
						</tr>
					</c:forEach>
				</table>
				<br>
				<input type="hidden" name="gameId" value="${gameModel.gameId}">
				<input type="hidden" name="gameName" value="${gameModel.gameName}">
				<input type="hidden" name="gameBeanName" value="${gameModel.gameBeanName}">
				<input type="hidden" name="parentBeanName" value="${gameModel.parentBeanName}">
				<input type="submit" name="saveGame" value="Save Game">
			</div>		
		</form>
	</body>
</html>
