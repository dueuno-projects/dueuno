console.log("CUSTOM JavaScript loaded.")

function incrementNumberfieldJS() {
    changeNumberfieldJS(1);
}

function decrementNumberfieldJS() {
    changeNumberfieldJS(-1);
}

function changeNumberfieldJS(amount) {
    let $numberfield = Elements.getElement('numberfieldJS');
    let value = Number(NumberField.getValue($numberfield).value ?? 0);
    NumberField.setValue($numberfield, TypedValue.number(Math.max(0, value + amount)));
}
