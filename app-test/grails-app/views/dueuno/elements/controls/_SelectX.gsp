<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to you under the Apache License, Version 2.0.
--%>
<div
    class="control-select-x ${c.actions.hasActions() ? 'has-actions' : ''} ${c.textStyle} ${c.cssClass}"
    style="${c.cssStyleColors}"
    name="${c.id}"
    data-21-control="${c.className}"
    data-21-id="${c.id}"
    data-21-properties="${c.propertiesAsJSON}"
    data-21-events="${c.eventsAsJSON}"
    data-21-value="${c.valueAsJSON}"
></div>

<g:if test="${c.actions.hasActions()}"><!--
<g:each var="action" in="${c.actions.defaultAction}">
    --><render:component instance="${action.link}" properties="[cssClass: 'btn btn-secondary', readonly: c.readonly]" /><!--
</g:each>
<g:each var="action" in="${c.actions.getMenuActions()}">
    --><render:component instance="${action.link}" properties="[cssClass: 'btn btn-secondary', readonly: c.readonly]" /><!--
</g:each>
--></g:if>
