package test

import dueuno.elements.ElementsController
import dueuno.elements.components.TableRow
import dueuno.elements.contents.ContentTable
import dueuno.types.Money

class CartController implements ElementsController {

    def index() {
        def c = createContent(ContentTable)

        c.table.columns = [
            'code',
            'product',
            'quantity',
            'price',
        ]

        c.table.body.eachRow { TableRow row, Map values ->
            println values
        }

        List listBody = [
            [1001, "Pasta", 2, new Money(2.3)],
            [2001, "Milk", 4, new Money(3)],
            [3001, "Coffee", 3, new Money(5)],
            [4001, "Nutella", 1, new Money(7)],
        ]

        List mapBody = [
            [code: 1001, product: "Pasta", quantity: 2, price: new Money(2.3)],
            [code: 2001, product: "Milk", quantity: 4, price: new Money(3)],
            [code: 3001, product: "Coffee", quantity: 3, price: new Money(5)],
            [code: 4001, product: "Nutella", quantity: 1, price: new Money(7)],
        ]

        List objectBody = [
            new CartItem(code: 1001, product: "Pasta", quantity: 2, price: new Money(2.3)),
            new CartItem(code: 2001, product: "Milk", quantity: 4, price: new Money(3)),
            new CartItem(code: 3001, product: "Coffee", quantity: 3, price: new Money(5)),
            new CartItem(code: 4001, product: "Nutella", quantity: 1, price: new Money(7)),
        ]

        c.table.body = objectBody

        display content: c
    }

}

class CartItem {
    Integer code
    String product
    Integer quantity
    Money price
}
