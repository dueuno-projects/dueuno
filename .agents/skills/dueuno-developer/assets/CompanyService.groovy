package template

import dueuno.tenant.AuditOperation
import dueuno.tenant.AuditService
import grails.gorm.DetachedCriteria
import grails.gorm.multitenancy.CurrentTenant
import grails.gorm.transactions.Transactional
import groovy.contracts.Requires
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct

@Slf4j
@CurrentTenant
@CompileStatic
class CompanyService {

    SecurityService securityService
    AuditService auditService

    @PostConstruct
    void init() {
        // Executes only once when the application starts
    }

    @CompileDynamic
    private DetachedCriteria<TCompany> buildQuery(Map filterParams, Boolean readOnly = false) {
        def query = TCompany.where {}

        if (filterParams.containsKey('id')) query = query.where { id == filterParams.id }

        if (filterParams.find) {
            String search = filterParams.find.replaceAll('\\*', '%')
            query = query.where {
                true
                        || name =~ "%${search}%"
            }
        }

        // Add additional filters here

        query.readOnly(readOnly)
        return query
    }

    private Map getFetchAll() {
        // Add any relationship here (Eg. references to other DomainObjects or hasMany)
        return [
                'relationshipName': 'join',

                // hasMany relationships
                'hasManyRelationship': 'join',
        ]
    }

    private Map getFetch() {
        // Add only single-sided relationships here (Eg. references to other Domain Objects)
        // DO NOT add hasMany relationships, you are going to have troubles with pagination
        return [
                'relationshipName': 'join',
        ]
    }

    TCompany get(Serializable id) {
        return find(id: id)
    }

    TCompany find(Map filterParams) {
        return buildQuery(filterParams).get(fetch: fetchAll)
    }

    List<TCompany> list(Map filterParams = [:], Map fetchParams = [:]) {
        if (!fetchParams.sort) fetchParams.sort = [dateCreated: 'asc']
        if (!fetchParams.fetch) fetchParams.fetch = fetch

        def query = buildQuery(filterParams)
        return query.list(fetchParams)
    }

    Number count(Map filterParams = [:]) {
        def query = buildQuery(filterParams)
        return query.count()
    }

    @Transactional
    TCompany create(Map args = [:]) {
        if (args.usernameCreated == null) args.usernameCreated = securityService.currentUsername
        if (args.failOnError == null) args.failOnError = false

        TCompany obj = new TCompany(args)
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @Transactional
    @CompileDynamic
    @Requires({ args.id })
    TCompany update(Map args = [:]) {
        if (args.usernameUpdated == null) args.usernameUpdated = securityService.currentUsername
        if (args.failOnError == null) args.failOnError = false

        TCompany obj = get(args.id)
        obj.properties = args
        obj.markDirty()
        obj.save(flush: true, failOnError: args.failOnError)
        return obj
    }

    @Transactional
    @Requires({ id })
    void delete(Serializable id) {
        TCompany obj = get(id)
        obj.delete(flush: true)
        auditService.log(AuditOperation.DELETE, obj)
    }
}
