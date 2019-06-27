import axios, {
  AxiosInstance,
  AxiosInterceptorManager,
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

class RestClient {

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

  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.doGet(RestClient.getRequestId(url), url, config);
  }

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.delegate.post(url, data, config);
  }

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.delegate.put(url, data, config);
  }

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.delegate.delete(url, config);
  }

  getParallel<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.doGet(url, url, config);
  }

  getForced<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.doGet(idGenerator.uuid().toString(), url, config);
  }

  getDelegate(): AxiosInstance {
    return this.delegate;
  }

  private doGet<T = any>(requestId: string, url: string, config?: AxiosRequestConfig): Promise<T> {
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

export let restClient: RestClient = new RestClient(axios.create(axiosConfig));

interface Headers {
  Authorization: string;
}

const makeRestClient = (headers: Headers): RestClient => {
  restClient = new RestClient(axios.create({...axiosConfig, headers}));
  return restClient;
};

export const restClientWith = (token?: string): void => {
  if (token) {
    makeRestClient({Authorization: `Bearer ${token}`});
  }
};

export const authenticate = (token: string): RestClient =>
  makeRestClient({Authorization: `Basic ${token}`});

export const wasRequestCanceled = (error: ErrorResponse): boolean =>
  error.message === requestCanceled;

export const isTimeoutError = (error: ErrorResponse): boolean =>
  error.message.startsWith(timeoutOf);
