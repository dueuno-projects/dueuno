package dueuno.application

import dueuno.elements.ElementsController

class MonitoringController implements ElementsController {

    def index() {
        redirect uri: '/monitoring'
    }

}
