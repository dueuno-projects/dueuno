package custom

import dueuno.elements.core.Page
import dueuno.elements.components.Header
import dueuno.elements.contents.ContentBlank

class CustomPage extends Page {

    Header header
    ContentBlank content

    CustomPage(Map args) {
        super(args)

        viewPath = '/custom/'

        header = createComponent(Header)
        content = createComponent(ContentBlank, 'content')
    }

}
