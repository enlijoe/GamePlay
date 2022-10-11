<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix = "form" uri ="http://www.springframework.org/tags/form"%>

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
		<form:form	modelAttribute="data" action="update" method="post">
			<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			<table>
				<tr><td width="40%"><lable for="insideDevice">In Device</lable></td><td width="60%">
					<select name=data.insideDevice"" id="insideDevice">
						<option value="" <c:if test="${empty data.insideDevice}">selected</c:if>>None</option>
						<c:forEach items="${deviceNameList}" var="deviceName">
							<option value="${deviceName}" <c:if test="${deviceName eq data.insideDevice}">selected</c:if>>${deviceName}</option>
						</c:forEach>
					</select>
				</td></tr>
				<tr><td><lable for="outsideDevice">Out Device</lable></td><td>
					<select name=data.outsideDevice"" id="outsideDevice">
						<option value="" <c:if test="${empty data.outsideDevice}">selected</c:if>>None</option>
						<c:forEach items="${deviceNameList}" var="deviceName">
							<option value="${deviceName}" <c:if test="${deviceName eq data.outsideDevice}">selected</c:if>>${deviceName}</option>
						</c:forEach>
					</select>
				</td></tr>
				
				<tr><td><lable for="startingVolume">Start Volume</lable></td><td><input type="text" id="startingVolume" name="data.startingVolume" value="${data.startingVolume}" size="4"></td></tr>
				<tr><td><lable for="tubeDiameter">Tube Diameter</lable></td><td><input type="text" id="tubeDiameter" name="data.tubeDiameter" value="${data.tubeDiameter}" size="3"></td></tr>
				<tr><td><lable for="waterInsideStoppingPressure">Stopping Pressure</lable></td><td><input type="text" id="waterInsideStoppingPressure" name="data.waterInsideStoppingPressure" value="${data.waterInsideStoppingPressure}" size="6"></td></tr>
				<tr><td><lable for="waterInsideStartingPressure">Starting Pressure</lable></td><td><input type="text" id="waterInsideStartingPressure" name="data.waterInsideStartingPressure" value="${data.waterInsideStartingPressure}" size="6"></td></tr>
				<tr><td>Volume Flowed</td><td>${data.volumeFlowed}</td></tr>
				<tr><td>Average Flow</td><td>${data.averageFlowRate}</td></tr>
				<tr><td colspan="2">
					<table border="1">
						<tr><td width="25%"></td><td width="25%">Min</td><td width="25%">Cur</td><td width="25%">Max</td></tr>
						<tr><td>Inside</td><td>${data.waterInsideMinPressure}</td><td>${data.waterInsideCurrentPressure}</td><td>${data.waterInsideMinPressure}</td></tr>
						<tr><td>Outsid</td><td>${data.waterOutsideMinPressure}</td><td>${data.waterOutsideCurrentPressure}</td><td>${data.waterOutsideMaxPressure}</td></tr>
					</table>
				</td></tr>
			</table>
			<table>
				<tr>
					<td width="80%"><img alt="Data Graph" src="/calibrate/graph.jpg"></td>
					<td width="20%"><img alt="Current Inside Pressure" src="/calibrate/pressure.jpg"></td>
				</tr>
				<tr>
					<td colspan="2"><lable for="graphWidth">Graph Width</lable><input type="text" id="graphWidth" name="data.graphWidth" size="3" value="${data.graphWidth}"></td>
				</tr>
			</table>
			<table>
				<tr>
					<td><input type="submit" name="startMonitor" value="Monitor Start"></td>
					<td><input type="submit" name="stopMonitor" value="Monitor Stop"></td>
				</tr>
				<tr>
					<td><input type="submit" name="startFlow" value="Flow Start"></td>
					<td><input type="submit" name="stopFlow" value="Flow Stop"></td>
				</tr>
				<tr>
					<td><input type="submit" name="update" value="Update"></td>
				</tr>
			</table>
		</form:form>
	</body>
</html>