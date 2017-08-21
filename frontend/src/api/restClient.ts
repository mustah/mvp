import axios from 'axios';

export const restClient = axios.create({
  baseURL: 'http://localhost:3000',
  timeout: 30000,
});
