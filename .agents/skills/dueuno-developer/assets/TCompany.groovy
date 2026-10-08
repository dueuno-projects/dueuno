package template

import grails.gorm.MultiTenant
import org.grails.datastore.gorm.GormEntity

import java.time.LocalDateTime

class TCompany implements GormEntity, MultiTenant<TCompany> {

    Long id
    LocalDateTime dateCreated
    LocalDateTime lastUpdated
    String usernameCreated
    String usernameUpdated

    String name
    String vat
    String address

}
