import axios from 'axios';

export const restClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
});
