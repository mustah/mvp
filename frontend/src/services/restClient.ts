import axios, {
  AxiosInstance,
  AxiosInterceptorManager,
  AxiosPromise,
  AxiosRequestConfig,
  AxiosResponse,
  CancelTokenSource,
} from 'axios';
import {config} from '../config/config';
import {InvalidToken} from '../exceptions/InvalidToken';
import {texts} from '../helpers/texts';
import {Dictionary, ErrorResponse} from '../types/Types';

const requestCanceled = 'REQUEST_CANCELED';

class RestClientDelegate implements AxiosInstance {

  private static getRequestId(url: string) {
    const endPoint = /[^?]*/; // regExp matching all characters before '?' or whole string when no '?' present
    return endPoint.test(url) ? url.match(endPoint)![0] : '';
  }

  defaults: AxiosRequestConfig;

  interceptors: {
    request: AxiosInterceptorManager<AxiosRequestConfig>;
    response: AxiosInterceptorManager<AxiosResponse>;
  };

  private delegate: AxiosInstance;
  private requests: Dictionary<CancelTokenSource>;

  constructor(delegate: AxiosInstance) {
    this.delegate = delegate;
    this.interceptors = delegate.interceptors;
    this.defaults = delegate.defaults;
    this.requests = {};
  }

  request<T = any>(config: AxiosRequestConfig): AxiosPromise<T> {
    return this.delegate.request(config);
  }

  get<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    const requestId = RestClientDelegate.getRequestId(url);
    const request = this.requests[requestId];
    if (request) {
      request.cancel(requestCanceled);
    }
    const source: CancelTokenSource = axios.CancelToken.source();
    this.requests[requestId] = source;
    return this.delegate.get(url, {...config, cancelToken: source.token});
  }

  delete(url: string, config?: AxiosRequestConfig): AxiosPromise {
    return this.delegate.delete(url, config);
  }

  head(url: string, config?: AxiosRequestConfig): AxiosPromise {
    return this.delegate.head(url, config);
  }

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.delegate.post(url, data, config);
  }

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.delegate.put(url, data, config);
  }

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.delegate.patch(url, data, config);
  }

}

const axiosConfig = config().axios;
export let restClient: RestClientDelegate = new RestClientDelegate(axios.create(axiosConfig));

interface Headers {
  Authorization: string;
}

interface Headers {
  Authorization: string;
}

const makeRestClient = (headers: Headers): AxiosInstance => {
  restClient = new RestClientDelegate(axios.create({...axiosConfig, headers}));
  setResponseInterceptors();
  return restClient;
};

const setResponseInterceptors = (): void => {
  restClient.interceptors.response.use((response) => response, (error) => {
    const {response} = error;
    if (response && response.data && response.data.message === texts.invalidToken) {
      return Promise.reject(new InvalidToken(response.data.message));
    } else {
      return Promise.reject(error);
    }
  });
};

export const restClientWith = (token?: string): void => {
  if (token) {
    makeRestClient({Authorization: `Bearer ${token}`});
  }
};

export const authenticate = (token: string): AxiosInstance =>
  makeRestClient({Authorization: `Basic ${token}`});

export const wasRequestCanceled = (error: ErrorResponse) => (error.message === requestCanceled);
