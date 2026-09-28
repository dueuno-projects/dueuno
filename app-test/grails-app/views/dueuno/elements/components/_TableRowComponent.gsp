<%--
================
ATTENTION PLEASE
================

When modifying this template, please update also:
 - `_TableRow.gsp`
--%>
<tr class="component-table-row ${c.textStyle} ${c.cssClass}"
    style="${c.backgroundColor ? '--bs-table-striped-bg: ' + c.backgroundColor + '; ' : ''}${c.cssStyleColors}"
    data-21-component="${c.className}"
    data-21-id="${c.id}"
    data-21-properties="${c.propertiesAsJSON}"
    data-21-events="${c.eventsAsJSON}"
>
    <%-- Selection --%>
    <g:if test="${c.table.groupActions.hasActions()}">
        <g:if test="${c.isHeader}">
            <th class="component-table-selection-header ${c.verticalAlign}">
                <g:if test="${!c.table.readonly}">
                    <render:component instance="${c.selected}" />
                </g:if>
            </th>
        </g:if>
        <g:elseif test="${c.isFooter}">
            <td class="component-table-selection-footer ${c.verticalAlign}"></td>
        </g:elseif>
        <g:else>
            <td class="component-table-selection ${c.verticalAlign}">
                <g:if test="${c.hasSelection}">
                    <render:component instance="${c.selected}" />
                    <span class="rowKeys d-none">${raw(c.keysAsJSON)}</span>
                </g:if>
            </td>
        </g:else>
    </g:if>

    <%-- Actions --%>
    <g:if test="${c.table.rowActions && !c.table.readonly}">
        <g:if test="${c.isHeader}">
            <th class="component-table-actions-header ${c.verticalAlign}">
                <g:if test="${c.table && c.table.groupActions.hasActions() && !c.table.readonly}">
                    <render:component instance="${c.table.groupActions}" properties="[cssClass: 'd-none']" />
                </g:if>
                <span><render:message code="component.table.actions" /></span>
            </th>
        </g:if>
        <g:elseif test="${c.isFooter}">
            <td class="component-table-actions-footer ${c.verticalAlign}">
                <render:message code="${c.values.actions}" />
            </td>
        </g:elseif>
        <g:else>
            <td class="component-table-actions ${c.verticalAlign}">
                <render:component instance="${c.actions}" />
            </td>
        </g:else>
    </g:if>

    <%-- Columns --%>
    <g:each var="column" in="${c.table.columns}">
        <g:set var="cell" value="${c.cells[column]}" />

        <g:if test="${!cell.isColumnSpanned()}">
            <g:if test="${cell.row.isHeader}">
                <th colspan="${cell.colspan}"
                    class="component-table-header ${cell.column} ${cell.id} ${cell.textAlign} ${cell.verticalAlign}"
                    data-21-component="${cell.getClassName()}"
                    data-21-id="${cell.getId()}"
                    data-21-properties="${cell.propertiesAsJSON}"
                    data-21-events="${cell.eventsAsJSON}"
                ><render:component instance="${cell.component}" /></th>
            </g:if>

            <g:else>
                <td class="component-table-cell ${cell.column} ${cell.id} ${cell.textAlign} ${cell.verticalAlign} ${cell.cssClass}"
                    colspan="${cell.colspan}"
                    data-21-component="${cell.getClassName()}"
                    data-21-id="${cell.getId()}"
                    data-21-properties="${cell.propertiesAsJSON}"
                    data-21-events="${cell.eventsAsJSON}"
                    ${cell.cssStyleColors ? raw('style="' + cell.cssStyleColors + '"') : ''}
                ><div ${cell.label ? '' : raw('class="input-group control-focus-group"')}
                      ${c.table.widths[column] ? raw('style="width: ' + c.table.widths[column] + 'px"') : ''}>
                    <render:component instance="${cell.component}" />
                </div>
                </td>
            </g:else>
        </g:if>
    </g:each>

    <%-- Submit Values --%>
    <g:if test="${c.isHeader}">
        <th class="d-none"></th>
    </g:if>
    <g:elseif test="${c.isFooter}">
        <td class="d-none"></td>
    </g:elseif>
    <g:else>
        <td class="component-table-values d-none">
            <g:each var="field" in="${c.submit}">
                <render:component instance="${field.value}" />
            </g:each>
        </td>
    </g:else>
</tr>
