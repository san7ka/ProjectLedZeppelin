<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Redirect to welcome page
    // For ROOT context, getContextPath() returns empty string, so redirect is to "/welcome"
    String contextPath = request.getContextPath();
    String redirectUrl = contextPath.isEmpty() ? "/welcome" : contextPath + "/welcome";
    response.sendRedirect(redirectUrl);
%>