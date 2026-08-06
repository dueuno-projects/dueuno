package dueuno.security

import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter

@CompileStatic
class ExternalIdAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    ExternalIdAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl)
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod())
        }
        String externalId = request.getParameter('externalId')
        externalId = (externalId != null) ? externalId.trim() : ""

        ExternalIdAuthenticationToken authRequest = ExternalIdAuthenticationToken.unauthenticated(externalId)
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request))
        return this.authenticationManager.authenticate(authRequest)
    }
}

