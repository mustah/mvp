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
import {idGenerator} from '../helpers/idGenerator';
import {texts} from '../helpers/texts';
import {Dictionary, ErrorResponse} from '../types/Types';

const requestCanceled = 'REQUEST_CANCELED';
const timeoutOf = 'timeout of';

class RestClientDelegate implements AxiosInstance {

  private static getRequestId(url: string) {
    const endPoint = /[^?]*/; // regExp matching all characters before '?' or whole string when no
                              // '?' present
    return endPoint.test(url) ? url.match(endPoint)![0] : '';
  }

  defaults: AxiosRequestConfig;

  interceptors: {
    request: AxiosInterceptorManager<AxiosRequestConfig>;
    response: AxiosInterceptorManager<AxiosResponse>;
  };

  private readonly delegate: AxiosInstance;

  private readonly requests: Dictionary<CancelTokenSource>;

  constructor(delegate: AxiosInstance) {
    this.delegate = delegate;
    this.interceptors = delegate.interceptors;
    this.defaults = delegate.defaults;
    this.requests = {};
    this.setResponseInterceptors();
  }

  request<T = any>(config: AxiosRequestConfig): AxiosPromise<T> {
    return this.delegate.request(config);
  }

  get<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.doGet(RestClientDelegate.getRequestId(url), url, config);
  }

  getParallel<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.doGet(url, url, config);
  }

  getForced<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    return this.doGet(idGenerator.uuid().toString(), url, config);
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

  private doGet<T = any>(requestId: string, url: string, config?: AxiosRequestConfig): AxiosPromise<T> {
    const request = this.requests[requestId];
    if (request) {
      request.cancel(requestCanceled);
    }
    const source: CancelTokenSource = axios.CancelToken.source();
    this.requests[requestId] = source;
    return this.delegate.get(url, {...config, cancelToken: source.token});
  }

  private setResponseInterceptors(): void {
    this.interceptors.response.use((response) => response, (error) => {
      const {response} = error;
      if (response && response.data && response.data.message === texts.invalidToken) {
        return Promise.reject(new InvalidToken(response.data.message));
      } else {
        return Promise.reject(error);
      }
    });
  }

}

const axiosConfig = config().axios;

interface AxiosInstanceWrapper extends AxiosInstance {
  getParallel<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T>;
  getForced<T = any>(url: string, config?: AxiosRequestConfig): AxiosPromise<T>;
}

export let restClient: AxiosInstanceWrapper = new RestClientDelegate(axios.create(axiosConfig));

interface Headers {
  Authorization: string;
}

const makeRestClient = (headers: Headers): AxiosInstance => {
  restClient = new RestClientDelegate(axios.create({...axiosConfig, headers}));
  return restClient;
};

export const restClientWith = (token?: string): void => {
  if (token) {
    makeRestClient({Authorization: `Bearer ${token}`});
  }
};

export const authenticate = (token: string): AxiosInstance =>
  makeRestClient({Authorization: `Basic ${token}`});

export const wasRequestCanceled = (error: ErrorResponse): boolean =>
  error.message === requestCanceled;

export const isTimeoutError = (error: ErrorResponse): boolean =>
  error.message.startsWith(timeoutOf);
