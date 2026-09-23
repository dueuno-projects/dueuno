<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Modal trailing focus stop

Run in Chrome with a modal containing editable fields. Repeat with the close
button visible and hidden.

1. Focus the last visible control and press TAB. Verify that the browser toolbar
   does not receive focus and no extra element becomes visible in the modal.
2. Press Shift+TAB. Verify that focus returns to the last visible control.
3. Press TAB twice from the last visible control. Verify that normal browser
   navigation resumes on the second press, including moving to browser chrome
   when there are no subsequent page tab stops.
4. Update the modal content through a transition and repeat these checks.
5. Close and reopen the modal and repeat these checks. There must still be only
   one extra tab stop.

For the asynchronous focus regression, use a modal with `timeStart` followed by
`timeEnd` as its last visible control, and an `onChange` action on `timeEnd` that
returns a transition calling `setFocus('timeStart')`.

1. Edit `timeEnd` and press TAB once.
2. Wait for the action response, then type a digit.
3. Verify that the digit edits `timeStart` and does not interact with browser
   chrome. Repeat with a delayed server response to check that the intermediate
   tab stop keeps keyboard focus in the document while the request is pending.
