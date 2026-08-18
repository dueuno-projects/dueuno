package test

import dueuno.commons.utils.LogUtils
import dueuno.elements.ElementsController
import dueuno.elements.components.Button
import dueuno.elements.components.Form
import dueuno.elements.components.Grid
import dueuno.elements.components.Separator
import dueuno.elements.components.Timer
import dueuno.elements.contents.ContentForm
import dueuno.elements.controls.NumberField
import dueuno.elements.controls.TextField
import dueuno.elements.core.Transition
import dueuno.elements.style.TextDefault
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Slf4j
@Secured(['ROLE_USER', /* other ROLE_... */])
class TimerController implements ElementsController {

    TimerService timerService

    @PostConstruct
    void init() {
        // Executes only once when the application starts
    }

    def handleException(Exception e) {
        // Display a popup message instead of the "Error" screen
        log.error LogUtils.logStackTrace(e)
        display exception: e
    }

    def index() {
        def c = createContent(ContentForm)
        c.header.removeNextButton()
        c.form.addField(
            class: Button,
            id: 'show',
            action: 'show',
            displayLabel: false,
            cols: 3,
        )
        c.form.addField(
            class: Button,
            id: 'showModal',
            action: 'show',
            modal: true,
            fullscreen: true,
            displayLabel: false,
            cols: 3,
        )

        def form = c.addComponent(Form, 'fTransition')
        form.with {
            addField(
                class: Separator,
                id: 'delay',
            )
            addField(
                class: Button,
                id: 'b1',
                action: 'onConfirm',
                displayLabel: false,
            )
            addField(
                class: TextField,
                id: 'f1',
                cols: 2,
            )
            addField(
                class: TextField,
                id: 'f2',
                cols: 2,
            )
            addField(
                class: TextField,
                id: 'f3',
                cols: 2,
            )
            addField(
                class: TextField,
                id: 'f4',
                cols: 2,
            )
            addField(
                class: TextField,
                id: 'f5',
                cols: 2,
            )
            addField(
                class: TextField,
                id: 'f6',
                cols: 2,
            )
        }
        display content: c
    }

    def show() {
        def c = createContent()

        c.header.addBackButton()
        c.header.nextButton.with {
            removeDefaultAction()
            addDefaultAction(action: 'enable', text: TextDefault.ENABLED, icon: 'fa-play')
            addTailAction(action: 'disable', text: TextDefault.DISABLED, icon: 'fa-pause')
        }

        c.addComponent(Timer, 'timer1', [
            interval          : 1300,
            action            : 'reload1',
            enabled           : false,
            executeImmediately: true,
        ])

        c.addComponent(Timer, 'timer2', [
            interval          : 2000,
            action            : 'reload2',
            enabled           : false,
            executeImmediately: true,
        ])

        c.addComponent(Grid).with {
            addColumn(sm: 9).with {
                addComponent(Form, 'formNum').with {
                    addField(
                        class: NumberField,
                        id: 'number',
                    )
                }
                addComponent(timerService.createGrid(7))
            }
            addColumn(sm: 3).with {
                addComponent(Form, 'formHead')
                addComponent(timerService.createTable(0))
            }
        }

        display content: c
    }

    def enable() {
        def t = createTransition()
        t.set('timer1', 'enabled', true)
        t.set('timer2', 'enabled', true)
        t.loading(false)
        display transition: t
    }

    def disable() {
        def t = createTransition()
        t.set('timer1', 'enabled', false)
        t.set('timer2', 'enabled', false)
        t.loading(false)
        display transition: t
    }

    def reload1() {
        def t = createTransition()

        Integer num = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss")).toInteger() % 30

        t.addComponent(timerService.createGrid(num))
        t.replace('grid1', 'grid1')

        t.setValue('number', num)
        t.call('messagebox', 'hide')  // IN CASO DI ERRORE

        display transition: t
    }

    def reload2() {
        def t = createTransition()

        Integer num = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ss")).toInteger() % 20
        if (num == 0) num = 1

        t.addComponent(timerService.createTable(num))
        t.replace('table', 'table')

        display transition: t
    }

    def detail() {
        def t = createTransition()
        Integer id = params.id.toInteger()
        t.addComponent(timerService.createHeadForm(id))
        t.addComponent(timerService.createTable(id * 3))
        t.replace('formHead', 'formHead')
        t.replace('table', 'table')
        t.loading(false)
        display transition: t
    }

    def onConfirm() {
        Transition t = createTransition()
        t.loading(false)
        t.setValue('f1', 'A')
        t.delay(500)
        t.setValue('f2', 'B')
        t.delay(500)
        t.setValue('f3', 'C')
        t.delay(500)
        t.setValue('f4', 'D')
        t.delay(500)
        t.setValue('f5', 'E')
        t.delay(500)
        t.setValue('f6', 'F')
        t.delay(1000)
        t.setValue('f1', null)
        t.setValue('f2', null)
        t.setValue('f3', null)
        t.setValue('f4', null)
        t.setValue('f5', null)
        t.setValue('f6', null)
        display transition: t
    }
}
