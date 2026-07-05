// Works only in 'async' functions
// Use: await sleep(i * 1000);
function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function getOS() {
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

function enableSimpleBar($element) {
    new SimpleBar($element);
}

function capitalize(val) {
    return val.charAt(0).toUpperCase() + val.slice(1);
}

function isEmpty(obj) {
    if (obj == null) return true;
    if (obj.length > 0) return false;
    if (obj.length === 0) return true;
    if (typeof obj !== "object") return true;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) return false;
    }
    return true;
}
