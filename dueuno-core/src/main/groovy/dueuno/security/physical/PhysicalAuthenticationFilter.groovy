package dueuno.security.physical

import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

@CompileStatic
class PhysicalAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    PhysicalAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl)
    }

    @Override
    Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod())
        }
        String physicalId = request.getParameter('physicalId')
        physicalId = (physicalId != null) ? physicalId.trim() : ""

        PreAuthenticatedAuthenticationToken authRequest = new PreAuthenticatedAuthenticationToken(physicalId, '')
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request))
        return this.authenticationManager.authenticate(authRequest)
    }
}
