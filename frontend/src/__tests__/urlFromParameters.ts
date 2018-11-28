import {EncodedUriParameters} from '../types/Types';

export const urlFromParameters = (parameters: EncodedUriParameters): URL =>
  new URL(`https://blabla.com/?${parameters}`);
