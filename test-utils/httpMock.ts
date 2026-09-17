export interface MockHttpResponseOptions {
    status?: number;
    url?: string;
    method?: string;
    data?: any;
}

export function mockHttpResponse<T>(data: T, opts: MockHttpResponseOptions = {}): any {
    const {status = 200, url = '', method = 'get', data: requestData} = opts;
    return {
        data,
        status,
        statusText: 'OK',
        headers: {},
        config: {url, method, data: requestData},
    };
}
