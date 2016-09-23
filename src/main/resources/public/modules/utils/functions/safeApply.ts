/**
 * Created by ledunoiss on 20/09/2016.
 */
export function  safeApply(that, fn) {
    var phase = that.$root.$$phase;
    if(phase === '$apply' || phase === '$digest') {
        if(fn && (typeof(fn) === 'function')) fn();
    } else that.$apply(fn);
}