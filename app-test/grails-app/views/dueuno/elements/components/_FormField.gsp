<div class="component-form-field ${c.cols} ${c.help ? 'help' : ''} ${c.cssClass}"
     data-21-component="${c.className}"
     data-21-id="${c.id}"
     data-21-properties="${c.propertiesAsJSON}"
     data-21-events="${c.eventsAsJSON}"
>
    <g:if test="${c.component.getClassName() != 'HiddenField'}">
        <label for="${c.component.getId()}" class="form-label text-truncate overflow-x-hidden pe-1 ${dev.displayHints() == 'true' ? 'dev' : ''} ${c.displayLabel ? '' : 'd-none'}">
            <g:if test="${dev.displayHints() == 'true'}">
                <span>${c.message(c.label, c.labelArgs)}&nbsp;(${c.component.getId()}, ${c.id})</span><i class="fa-solid fa-asterisk fa-2xs ps-1 ${c.nullable ? 'd-none' : ''}"></i>
            </g:if>
            <g:elseif test="${c.message(c.label)}">
                <span>${c.message(c.label, c.labelArgs)}</span><i class="fa-solid fa-asterisk fa-2xs ps-1 ${c.nullable ? 'd-none' : ''}"></i>
            </g:elseif>
            <g:else>
                <span>&nbsp;</span>
            </g:else>
        </label>
    </g:if>

    <div class="input-group control-focus-group ${c.highlight ? 'field-highlight' : ''} ${c.displayLabel ? '' : 'mt-2'}"
         ${raw(c.component.getContainerAttributes())}
         ${c.rows ? raw('style="' + c.rows + '"') : ''}
    >
        <render:component id="${c.component.getId()}" instance="${c.component}" properties="[:]" />
        <g:if test="${c.help}">
            <button class="component-help btn btn-secondary" type="button"
                    data-bs-toggle="collapse" data-bs-target="#${c.id}-help">
                <i class="fa-solid fa-circle-question"></i>
            </button>
        </g:if>
    </div>

    <g:if test="${c.component.getClassName() != 'HiddenField'}">
        <g:if test="${c.help}">
            <div id="${c.id}-help" class="help-message collapse${c.helpCollapsed ? '' : ' show'}">
                <div><render:message code="${c.help}" args="${c.helpArgs}"/></div>
            </div>
        </g:if>
        <div class="error-message d-none">
            <div><i class="fa-solid fa-circle-exclamation"></i><span class="ps-1">Error!</span></div>
        </div>
    </g:if>

</div>

