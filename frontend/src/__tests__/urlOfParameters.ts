import {EncodedUriParameters} from '../types/Types';

export const urlOfParameters = (parameters: EncodedUriParameters): URL =>
  new URL(`https://blabla.com/?${parameters}`);
