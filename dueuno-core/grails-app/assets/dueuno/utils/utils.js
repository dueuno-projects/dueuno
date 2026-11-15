/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class Utils {

    static getUriString(s) {
        s = s || document.location.search;
        return s.split("?")[0];
    }

    static getQueryParameters(s) {
        s = s || document.location.search;
        s = s.replace(/(^\?)/,'');
        s = s.split("?");
        s = s[s.length - 1];
        return s.split("&").map(function(n){return n = n.split("="),this[n[0]] = n[1],this}.bind({}))[0];
    }

    // Works only in 'async' functions
    // Use: await Utils.sleep(i * 1000);
    static sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    static getOS() {
        const ua = navigator.userAgent.toLowerCase();

        // iPhone, iPad, iPod e iPadOS 13+
        if (
            ua.includes('iphone') ||
            ua.includes('ipad') ||
            ua.includes('ipod') ||
            (ua.includes('mac') && 'ontouchend' in document)
        ) {
            return 'iOS';
        }

        const operatingSystems = [
            'Windows',
            'Android',
            'Unix',
            'Mac',
            'Linux',
            'BlackBerry'
        ];

        return operatingSystems.find(os =>
            ua.includes(os.toLowerCase())
        );
    }

    static mountSimpleBar($element) {
        new SimpleBar($element);
    }

    static unmountSimpleBar($element) {
        let simpleBar = SimpleBar.instances.get($element);
        if (simpleBar) {
            simpleBar.unMount();
        }
    }

    static capitalize(val) {
        return val.charAt(0).toUpperCase() + val.slice(1);
    }

    static isEmpty(obj) {
        if (obj == null) return true;
        if (obj.length > 0) return false;
        if (obj.length === 0) return true;
        if (typeof obj !== "object") return true;
        for (let key in obj) {
            if (obj.hasOwnProperty(key)) return false;
        }
        return true;
    }

}
